/**
 * Copyright 2022 Karl Kegel
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package modicio.core

import ModelElement.composeSingletonIdentity
import modicio.core.datamappings.{ModelElementData, RuleData}
import modicio.core.rules.{AssociationRule, AttributeRule}
import modicio.core.util.Observer
import modicio.core.values.ConcreteValue
import modicio.verification.{DefinitionVerifier, ModelVerifier}

import scala.collection.mutable
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
 * <p> The ModelElement is the central class of the CoDI type-hierarchy. It forms the component of the type-composite tree.
 * <p> The abstract ModelElement contains all concrete functionality to handle associations and rules. Methods regarding extensions
 * are abstract and must be implemented by the [[Node Node]]. Other abstract members must be extended or overwritten by
 * its child class [[Node Node]].
 * <p> Apart from the constructor arguments, the ModelElement possess a [[Definition Definition]], a [[Registry Registry]], a
 * [[DefinitionVerifier DefinitionVerifier]] and a [[ModelVerifier ModelVerifier]].Those classes are
 * not always required but should be set directly after object construction using their respective set-methods.
 *
 * @param name       name of the ModelElement, name and identity form a unique pair.
 * @param identity   identity of the ModelElement
 * @param isTemplate if the template can be instantiated directly or only used as part of an extension hierarchy / isAbstract
 * @param timeIdentity TODO documentation
 */
abstract class ModelElement(val name: String, val identity: String, val isTemplate: Boolean, protected var timeIdentity: TimeIdentity) {


  private final var definitionOption: Option[Definition] = None
  private final var registryOption: Option[Registry] = None

  protected final var modelVerifier: Option[ModelVerifier] = None
  protected final var definitionVerifier: Option[DefinitionVerifier] = None

  private[modicio] val associations: mutable.Set[TypeHandle] = mutable.Set()

  /**
   * <p> Private observer object representing a callback that is called if the [[Definition Definition]]
   * changes.
   */
  private final object _definitionObserver extends Observer {
    override def onChange(): Future[Unit] = {
      //TODO commit here if in some kind of automatic mode
      Future.successful((): Unit)
    }
  }

  /**
   * <p>Abstract method to get if the concrete implementation is a [[Node Node]].
   * Present in case of later other ModelElement extensions.
   *
   * @return Boolean- true if Node
   */
  def isNode: Boolean

  /**
   * <p> Abstract method to get the set of parents i.e. the set of extensions.
   * <p> See the concrete implementations for more information.
   * <p> A concrete implementation may require the ModelElement to be unfolded!
   *
   * @return Set[ModelElement] - set of parent/extended ModelElements
   */
  def getParents: Set[ModelElement]

  /**
   * <p> Add a [[Rule Rule]] to this ModelElement.
   * <p> See the concrete implementations for more information.
   * <p> A concrete implementation may require the ModelElement to be unfolded!
   * <p> A concrete implementation may change the fold state of the ModelElement
   *
   * @param rule the Rule to add
   */
  def applyRule(rule: Rule): Unit

  /**
   * <p> Remove a [[Rule Rule]] from this ModelElement.
   * <p> See the concrete implementations for more information.
   * <p> A concrete implementation may require the ModelElement to be unfolded!
   * <p> A concrete implementation may change the fold state of the ModelElement
   *
   * @param rule the Rule to remove
   */
  def removeRule(rule: Rule): Unit

  /**
   * <p> Abstract framework-private method to produce a data representation of this ModelElement.
   * <p> See the concrete implementations for more information.
   * <p> A concrete implementation may require the ModelElement to be unfolded!
   * FIXME doc
   *
   * @return (ModelElementData, Set[RuleData]) - tuple of [[ModelElementData ModelElementData]] and
   *         [[RuleData RuleData]]
   */
  private[modicio] def toData: (ModelElementData, Set[RuleData], TimeIdentity)

  /**
   * <p> Trigger the persistence process for this ModelElement. Child classes may overwrite the behaviour of this method.
   * <p> A concrete implementation may require the ModelElement to be unfolded!
   * <p> See overwriting implementations for more information.
   *
   * @return Future[Unit] - after the persistence process was completed
   */
  def commit(): Future[Any] = {
    if(definition.isVolatile) {
      registry.setType(this.createHandle) map (
        newTimeIdentity => timeIdentity = newTimeIdentity) map (_ =>
        definition.cleanVolatile())
    } else {
      Future.successful()
    }
  }

  /**
   * TODO documentation
   * @return
   */
  def getTimeIdentity: TimeIdentity = timeIdentity

  /**
   * TODO documentation
   * @param timeIdentity
   */
  def setTimeIdentity(timeIdentity: TimeIdentity): Unit = this.timeIdentity = timeIdentity

  /**
   * <p> Fork this ModelElement. This operation creates a copy of ModelElement and [[Definition Definition]] with
   * a new identity propagated through the model.
   * <p> Note: this operations does only work for this particular ModelElement in general case. If called on a Node,
   * the refined implementation can change this behaviour.
   * <p> The forked ModelElement is added to the registry immediately.
   * <p> TODO this operation is lazy, it returns before the ModelElement is stored in the registry.
   *      This operation should return a Future instead.
   *
   * @param identity the new identity the forked ModelElement receives
   * @return ModelElement - the forked ModelElement
   */
  def fork(identity: String): ModelElement = {
    val newNode = new Node(name, identity, isTemplate, TimeIdentity.fork(timeIdentity))
    newNode.setRegistry(registry)
    newNode.setDefinition(definition.fork(identity))
    registry.setType(newNode.createHandle)
    newNode
  }

  /**
   * <p> Unfolds all associations of the ModelElement.
   * <p> This call is not propagated to the associated ModelElements, they remain folded!
   * <p> By default, this operation is used by the [[TypeHandle TypeHandle]] only.
   * <p> A [[Registry Registry]] must be available to this ModelElement because not available data is lazily reloaded.
   * <p> Note that only reference modelElements are resolved. In an instantiated case (identity != #), the reference specifications must not match with different instance identities so those
   * should always be fetched via the respective InstanceHandle.
   *
   * @return Future[Unit] - after the operation terminates. The ModelElement should not be accessed before this operation terminates.
   *         Exception in error cases.
   */
  def unfold(): Future[Any] = {
    associations.clear()
    // resolve associations
    val associationRules: Set[AssociationRule] = definition.getAssociationRules
    if (associationRules.isEmpty) {
      Future.successful((): Unit)
    } else {
      if (associationRules.nonEmpty && registryOption.isEmpty) {
        throw new Exception("Undefined registry in modelElement to unfold")
      }
      val mapOfFutures = associationRules.map(associationRule => registry.getType(associationRule.targetName, ModelElement.REFERENCE_IDENTITY))
      val associationsFuture = Future.sequence(mapOfFutures)

      associationsFuture map (handleOptions => {
        //FIXME here we do nothing if a target is not found
        associations.addAll(handleOptions.filter(_.isDefined).map(_.get))
      })
    }
  }

  /**
   * <p> Undo the unfold() operation. This clears the internal set of unfolded associations.
   */
  def fold(): Unit = {
    associations.clear()
  }

  /**
   * FIXME this is a dummy implementation and needs to be moved to a verification or reasoning module in the future.
   *  No verification is done here, if a non-existent association is given, it will be accepted!
   * @return
   */
  def isConcrete: Boolean = {
    //TODO check if every attribute and association with mult 1 has one final value
    //val associationRules = get

    val associationRules = deepAssociationRuleSet
    val attributeRules = deepAttributeRuleSet
    val values = deepValueSet
    val associationValues = values.filter(_.isAssociationValue)
    val attributeValues = values.filter(_.isAttributeValue)

    var isConcrete: Boolean = true

    //each association must have a fixed int-value multiplicity.
    //the number of values fulfilling this association must match the multiplicity
    associationRules.foreach(associationRule => {
      val relation = associationRule.associationName
      if(associationRule.hasIntMultiplicity){
        val multiplicity = associationRule.getIntMultiplicity
        if(associationValues.count(value => value.valueName == relation) != multiplicity){
          isConcrete = false
        }
      }else{
        isConcrete = false
      }
    })

    //each attribute rule must be fulfilled by exactly one value
    attributeRules.foreach(attributeRule => {
      if(attributeValues.count(value => value.valueName == attributeRule.name) != 1){
        isConcrete = false
      }
    })
    println("DEBUG >> " + name + " isConcrete = " + isConcrete)
    isConcrete
  }

  def hasSingleton: Future[Boolean] = {
    registry.getSingletonTypes(name) map(_.nonEmpty)
  }

  def hasSingletonRoot: Future[Boolean] = {
    registry.getType(name, composeSingletonIdentity(name)) map (_.isDefined)
  }

  def updateSingletonRoot(): Future[Option[DeepInstance]] = {
    unfold() flatMap (_ => removeSingleton() flatMap (_ => {
      if (!isConcrete) {
        Future.successful(None)
      } else {
        val factory = new InstanceFactory(definitionVerifier.get, modelVerifier.get)
        factory.setRegistry(registry)
        factory.newInstance(name, ModelElement.composeSingletonIdentity(name)) map (instance => Some(instance))
      }
    }))
  }

  private def removeSingleton(): Future[Any] = {
    registry.autoRemove(name, ModelElement.composeSingletonIdentity(name))
  }

  /**
   * <p> Checks if this ModelElement is valid by the means of the provided [[ModelVerifier ModelVerifier]]
   * and [[DefinitionVerifier DefinitionVerifier]].
   * <p> TODO as of right now, error logs cannot be propagated apart from throwing exceptions.
   * <p> <strong>The ModelElement needs to be unfolded to perform this operation.</strong>
   *
   * @return Boolean - if this ModelElement and its [[Definition Definition]] is valid
   */
  def isValid: Boolean = {
    if (definitionVerifier.isEmpty || modelVerifier.isEmpty) throw new Exception("No concrete verifiers defined!")
    var res: Boolean = definitionVerifier.get.verify(definition.getRules) && modelVerifier.get.verify(this.createHandle)
    getParents.foreach(modelElement => res = res && modelElement.isValid)
    res
  }

  /**
   * <p> Package-private [[Registry Registry]] getter.
   *
   * @return Registry if available or Exception
   */
  private[modicio] final def registry: Registry = registryOption.getOrElse(throw new Exception("undefined registry"))

  /**
   * <p> Package-private setter for [[Definition Definition]].
   * <p> This operation is by default used by the [[TypeFactory TypeFactory]] during ModelElement construction only.
   *
   * @param definition the Definition of this ModelElement
   */
  private[modicio] final def setDefinition(definition: Definition): Unit = {
    definitionOption = Some(definition)
    definition.registerObserver(_definitionObserver)
  }

  /**
   * <p> Package-private [[Definition Definition]] getter.
   *
   * @return Definition if available or Exception
   */
  private[modicio] def definition: Definition = definitionOption.getOrElse(throw new Exception("undefined definition"))

  /**
   * <p> Protected [[Definition Definition]] getter.
   * <p> This operation is unsafe and expects the Definition to be available!
   *
   * @return Definition if available or Exception
   */
  @Deprecated
  protected def getDefinition: Definition = definition

  /**
   * <p> Package-private setter for [[Registry Registry]].
   * <p> This operation is by default used by the [[TypeFactory TypeFactory]] during ModelElement construction only.
   *
   * @param registry the Registry used for this ModelElement
   */
  private[modicio] final def setRegistry(registry: Registry): Unit = {
    registryOption = Some(registry)
  }

  /**
   * <p> Package-private setter for [[ModelVerifier ModelVerifier]] and [[DefinitionVerifier DefinitionVerifier]].
   * <p> This operation is by default used by the [[TypeFactory TypeFactory]] during ModelElement construction only.
   *
   * @param definitionVerifier the DefinitionVerifier this ModelElement should use to check its Definition
   * @param modelVerifier      the ModelVerifier this ModelElement should use to check its structure
   */
  private[modicio] final def setVerifiers(definitionVerifier: DefinitionVerifier, modelVerifier: ModelVerifier): Unit = {
    this.modelVerifier = Some(modelVerifier)
    this.definitionVerifier = Some(definitionVerifier)
  }

  /**
   * <p> Create and return a [[TypeHandle TypeHandle]].
   * <p> See [[TypeHandle TypeHandle]] implementation for more information!
   *
   * @return a [[TypeHandle TypeHandle]] created from this ModelElement.
   *         Each time, an independent TypeHandle is created and returned.
   */
  final def createHandle: TypeHandle = {
    val isStatic = identity != ModelElement.REFERENCE_IDENTITY
    new TypeHandle(modelElement = this, isStatic)
  }

  /**
   * <p> Predefined query-method. Gets the set of all type-names the ModelElement has in its extension hierarchy.
   * <p> <strong>The ModelElement needs to be unfolded to perform this operation.</strong>
   *
   * @return Set[String] - of polymorph type-names
   */
  def getTypeClosure: Set[String] = {
    val results = mutable.Set[String]()
    results.add(name)
    getParents.foreach(parentModelElement => results.addAll(parentModelElement.getTypeClosure))
    results.toSet
  }

  private[modicio] def deepAttributeRuleSet: Set[AttributeRule] = {
    val result = mutable.Set[AttributeRule]()
    result.addAll(definition.getAttributeRules)
    applyDeepResult[AttributeRule](result, getParents.map(parent => parent.deepAttributeRuleSet))
    result.toSet
  }

  private[modicio] def deepAssociationRuleSet: Set[AssociationRule] = {
    val result = mutable.Set[AssociationRule]()
    result.addAll(definition.getAssociationRules)
    applyDeepResult[AssociationRule](result, getParents.map(parent => parent.deepAssociationRuleSet))
    result.toSet
  }

  private[modicio] def deepValueSet: Set[ConcreteValue] = {
    val result = mutable.Set[ConcreteValue]()
    result.addAll(definition.getConcreteValues)
    applyDeepResult[ConcreteValue](result, getParents.map(parent => parent.deepValueSet))
    result.toSet
  }

  private def applyDeepResult[T <: Rule](result: mutable.Set[T], values: Set[Set[T]]): Unit = {
    values.foreach(ruleSet => {
      ruleSet.foreach(associationRule => {
        val overrideOption = result.find(_.isPolymorphEqual(associationRule))
        if (overrideOption.isEmpty) {
          result.add(associationRule)
        }
      })
    })
  }

  /**
   * <p> Predefined query-method. Gets the set of [[AssociationRule AssociationRules]] part of the
   * [[Definition Definition]] which target a certain other ModelElement i.e. this method returns
   * the different possibility by which a certain other ModelElement can be related to this ModelElement
   *
   * @param typeName of the related ModelElement
   * @return Set[AssociationRule] - between this and the related ModelElement
   */
  def getAssociationRulesOfType(typeName: String): Set[AssociationRule] = {
    definition.getAssociationRules.filter(_.targetName == typeName)
  }

  /**
   * <p> The same as [[ModelElement#getAssociationRulesOfType ModelElement.getAssociationRulesOfType()]] but considers
   * the complete extension hierarchy.
   * <p> <strong>The ModelElement needs to be unfolded to perform this operation.</strong>
   *
   * @param typeName of the related ModelElement
   * @return Set[AssociationRule] - between this (and extensions) and the related ModelElement
   */
  def getDeepAssociationRulesOfType(typeName: String): Set[AssociationRule] = {
    val results = mutable.Set[AssociationRule]()
    results.addAll(getAssociationRulesOfType(typeName))
    getParents.foreach(parent => results.addAll(parent.getDeepAssociationRulesOfType(typeName)))
    results.toSet
  }

  /**
   * <p> Predefined query-method. Gets the set of [[AssociationRule AssociationRules]] part of the
   * [[Definition Definition]] which are variants of the same relation.
   *
   * @param relationName of the association relation to query
   * @return Set[AssociationRule] - regarding a given relation
   */
  def getAssociationRulesOfRelation(relationName: String): Set[AssociationRule] = {
    definition.getAssociationRules.filter(_.associationName == relationName)
  }

  /**
   * <p> The same as [[ModelElement#getAssociationRulesOfRelation ModelElement.getAssociationRulesOfRelation()]] but considers
   * the complete extension hierarchy.
   * <p> <strong>The ModelElement needs to be unfolded to perform this operation.</strong>
   *
   * @param relationName of the association relation to query
   * @return Set[AssociationRule] - regarding the given relation
   */
  def getDeepAssociationRulesOfRelation(relationName: String): Set[AssociationRule] = {
    val results = mutable.Set[AssociationRule]()
    results.addAll(getAssociationRulesOfRelation(relationName))
    getParents.foreach(parent => results.addAll(parent.getDeepAssociationRulesOfRelation(relationName)))
    results.toSet
  }

}

/**
 * <p> Contains static values of the ModelElement class
 */
object ModelElement {

  /**
   * <p> The built-in constant string value used to represent reference identity ModelElements.
   */
  val REFERENCE_IDENTITY = "#"

  /**
   * <p> The built-in constant string value used to represent singleton identities. I.e. objects/instances
   * that can and must exist only once in a registry-context.
   */
  val SINGLETON_IDENTITY = "$"

  val ROOT_NAME = "ROOT"

  def composeSingletonIdentity(typeName: String): String = SINGLETON_IDENTITY + "_" + typeName

  def decomposeSingletonIdentity(identity: String): String = {
    identity.split("_")(1)
  }

  def isSingletonIdentity(identity: String): Boolean = identity.startsWith(SINGLETON_IDENTITY)

}
