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
package modicio.nativelang.input

import modicio.core.rules.{AssociationRule, AttributeRule, ParentRelationRule}
import modicio.core.values.ConcreteValue
import modicio.core.{DeepInstance, ImmutableShape, ModelElement, Plugin, Registry, Shape, ShapeWrapper, TimeIdentity, Transformer, TypeHandle}
import modicio.verification.{DefinitionVerifier, ModelVerifier}

import scala.collection.mutable
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
 * @param registry
 * @param definitionVerifier
 * @param modelVerifier
 */
class NativeDSLTransformer(registry: Registry,
                           definitionVerifier: DefinitionVerifier,
                           modelVerifier: ModelVerifier) extends
  Transformer[NativeDSL, NativeCompartment](registry, definitionVerifier, modelVerifier) {

  override def extend(input: NativeDSL): Future[Any] = {
    val defaultTimeIdentity = TimeIdentity.create
    Future.sequence(input.model.map(statement => evaluateModelElement(statement, defaultTimeIdentity, register = true)))
  }

  override def transform(input: NativeDSL): Future[Seq[TypeHandle]] = {
    val defaultTimeIdentity = TimeIdentity.create
    Future.sequence(input.model.map(statement => evaluateModelElement(statement, defaultTimeIdentity, false)))
  }
  
  override def extendInstance(input: NativeCompartment): Future[Any] = {
    val typePart = input.definition
    val instancePart = input.configuration

    extend(typePart) flatMap (_ => {

      val instanceFuture = Future.sequence(instancePart.map(data => {
        val (instanceData, parentRelationData, attributeData, associationData) = data
        val shape = new ShapeWrapper(attributeData, mutable.Set.from(associationData), parentRelationData)
        registry.getType(instanceData.instanceOf, instanceData.identity) map (typeHandleOption => {
            new DeepInstance(instanceData.instanceId, instanceData.identity, shape, typeHandleOption.get, registry)
        })
      }))

      instanceFuture map (instances => Future.sequence(instances.map(registry.setInstance)))
    })

  }

  def evaluateModelElement(statement: NativeModelElement, defaultTimeIdentity: TimeIdentity, register: Boolean): Future[TypeHandle] = {
    val name = NativeModelElement.parseName(statement)
    val identity = NativeModelElement.parseIdentity(statement)

    //println("EVALUATE MODEL ELEMENT")
    //println(statement)
    //println(defaultTimeIdentity)

    val timeIdentity = {
      if (statement.timeDescriptor.isDefined) {
        val desc = statement.timeDescriptor.get
        TimeIdentity(desc.variantTime, desc.runningTime, desc.versionTime, desc.variantId, desc.runningId, desc.versionId)
      } else {
        defaultTimeIdentity
      }
    }

    val typeHandleFuture = (for {
      typeHandle <- typeFactory.newType(name, identity, statement.template, Some(timeIdentity))
      _ <- if(register) registry.setType(typeHandle, importMode = true) else Future.successful()
    } yield {
      typeHandle.openImportMode()
      statement.childOf.foreach(parentRelationRule => typeHandle.applyRule(new ParentRelationRule(parentRelationRule)))
      statement.attributes.foreach(propertyRule => typeHandle.applyRule(new AttributeRule(propertyRule)))
      statement.associations.foreach(associationRule => typeHandle.applyRule(new AssociationRule(associationRule)))
      statement.values.foreach(concreteValue => typeHandle.applyRule(new ConcreteValue(concreteValue)))
      if(statement.plugins.isDefined){
        statement.plugins.get.foreach(plugin =>
          typeHandle.addPlugin(new Plugin(plugin.id, plugin.description, plugin.resolver, plugin.content)))
      }
      typeHandle.closeImportMode()
      typeHandle
    })

    for {
      typeHandle <- typeHandleFuture
      _ <- typeHandle.commit(importMode = true)
    } yield {
      typeHandle
    }

  }

  /**
   * Decomposes a runtime model into its serialised NativeModel
   * @param deepInstanceId
   * @return
   */
  override def decomposeInstance(deepInstanceId: String): Future[NativeCompartment] = {
      val statements = mutable.Set[NativeModelElement]()
      val configuration = mutable.Set[ImmutableShape]()
      registry.get(deepInstanceId) flatMap (flatInstance => {
        if (flatInstance.isDefined) {
          flatInstance.get.unfold() map (deepInstance => {
            deepInstance.getParentRelationClosure.foreach(i => {
              val data = i.toData
              configuration.add(data)
              val frag = i.getTypeHandle.getModelElement
              statements.add(buildStatement(frag))
            })
            NativeCompartment(NativeDSL(statements.toSeq), configuration.toSeq.map(s =>
              (s.instanceData, s.parentRelations, s.attributes, s.associations)))
          })
        } else {
          Future.failed(new IllegalArgumentException("Invalid instanceId in decompose()"))
        }
      })
  }

  override def decomposeModel(): Future[NativeDSL] = {
    registry.getReferences map( references => {
      val statements = references.map(_.getModelElement).map(buildStatement)
      NativeDSL(statements.toSeq)
    })
  }

  private def buildStatement(frag: ModelElement): NativeModelElement = {
    val childOf = frag.definition.getParentRelationRules.map(_.serialise()).toSeq
    val associations = frag.definition.getAssociationRules.map(_.serialise()).toSeq
    val attributes = frag.definition.getAttributeRules.map(_.serialise()).toSeq
    val values = frag.definition.getConcreteValues.map(_.serialise()).toSeq
    val plugins = frag.definition.getPlugins.map(_.toData(frag)).toSeq
    val ti = frag.getTimeIdentity
    val timeIdentity = NativeTimeIdentity(ti.variantTime, ti.runningTime, ti.versionTime, ti.variantId, ti.runningId, ti.versionId)
    NativeModelElement(frag.identity + ":" + frag.name, frag.isTemplate, Some(timeIdentity), childOf, associations, attributes, values, Some(plugins))
  }

}
