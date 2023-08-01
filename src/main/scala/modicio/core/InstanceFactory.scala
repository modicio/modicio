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

import modicio.core.datamappings.{AssociationData, AttributeData, InstanceData, ParentRelationData}
import modicio.core.util.IdentityProvider
import modicio.core.values.ConcreteValue
import modicio.verification.{DefinitionVerifier, ModelVerifier}

import scala.collection.mutable
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.jdk.FutureConverters._
import scala.language.implicitConversions

/**
 * @param definitionVerifier
 * @param modelVerifier
 */
class InstanceFactory(private[modicio] val definitionVerifier: DefinitionVerifier,
                      private[modicio] val modelVerifier: ModelVerifier) {

  private var registry: Registry = _

  def setRegistry(registry: Registry): Unit = this.registry = registry

  def newInstance(typeName: String): Future[DeepInstance] = newInstance(typeName, IdentityProvider.newRandomId())

  def newInstance(typeName: String, newIdentity: String): Future[DeepInstance] = {
    registry.getType(typeName, ModelElement.REFERENCE_IDENTITY) flatMap (typeHandleOption =>
      typeHandleOption.getOrElse(throw new Exception("type-model not found")).unfold() flatMap (referenceTypeHandle => {
        if (referenceTypeHandle.getModelElement.isTemplate) {
          Future.failed(new Exception("unable to instantiate template type-models at bottom level"))
        } else {
          val identity: String = newIdentity
          val deepValueSet: Set[ConcreteValue] = referenceTypeHandle.getModelElement.deepValueSet

          referenceTypeHandle.getModelElement.fork(identity, persist = true) flatMap (forkedModelElement => {
          forkedModelElement.setVerifiers(definitionVerifier, modelVerifier)

          forkedModelElement.createHandle.unfold() flatMap (unfoldedTypeHandle => {
            val instanceBuffer: mutable.Set[DeepInstance] = mutable.Set[DeepInstance]()
            val rootInstanceId = deriveInstance(unfoldedTypeHandle, identity, deepValueSet, instanceBuffer).getInstanceId
            Future.sequence(instanceBuffer.map(registry.setInstance)) map (_ => instanceBuffer.find(_.getInstanceId == rootInstanceId).get)
          })

        })
      }}))
  }

  /**
   *
   * @param typeHandle unfolded TypeHandle with forked and identified modelElement model
   * @param identity
   * @param deepValueSet
   * @param instanceBuffer
   * @return
   */
  private def createParentRelations(typeHandle: TypeHandle,
                               identity: String, rootInstanceId: String,
                               deepValueSet: Set[ConcreteValue],
                               instanceBuffer: mutable.Set[DeepInstance]): Set[ParentRelationData] = {
    val modelElement = typeHandle.getModelElement
    val parentRelations = modelElement.getParents
    parentRelations.map(parentRelationModelElement => deriveInstance(parentRelationModelElement.createHandle, identity, deepValueSet, instanceBuffer)).map(newInstance => ParentRelationData(0, rootInstanceId, newInstance.getInstanceId))
  }

  /**
   *
   * @param typeHandle unfolded TypeHandle with forked and identified modelElement model
   * @param identity
   * @param deepValueSet
   * @param instanceBuffer
   * @return
   */
  private def deriveInstance(typeHandle: TypeHandle,
                             identity: String,
                             deepValueSet: Set[ConcreteValue],
                             instanceBuffer: mutable.Set[DeepInstance]): DeepInstance = {
    val instanceId: String = IdentityProvider.newRandomId()
    val parentRelations = createParentRelations(typeHandle, identity, instanceId, deepValueSet, instanceBuffer)
    val shape = new ShapeWrapper(
      deriveAttributes(typeHandle, instanceId, deepValueSet),
      mutable.Set.from(deriveAssociations(typeHandle, instanceId, deepValueSet)),
      parentRelations)
    val deepInstance = new DeepInstance(instanceId, identity, shape, typeHandle, registry)
    instanceBuffer.add(deepInstance)
    deepInstance
  }

  /**
   * TODO resolve overwrites and if this is actually the most concrete point to assign a value
   * @param typeHandle
   * @param instanceId
   * @param deepValueSet
   * @return
   */
  private def deriveAttributes(typeHandle: TypeHandle, instanceId: String, deepValueSet: Set[ConcreteValue]): Set[AttributeData] = {
    val attributeRules = typeHandle.getModelElement.definition.getAttributeRules
    attributeRules.map(attributeRule => {
      val valueOption = deepValueSet.find(value => value.concreteOf(attributeRule))
      if(valueOption.isEmpty) {
        AttributeData(0, instanceId, attributeRule.name, "", isFinal = false)
      }else{
        val value = valueOption.get
        AttributeData(0, instanceId, attributeRule.name, value.getAttributeDescriptor.attributeValue, isFinal = true)
      }
    })
  }

  /**
   * TODO resolve overwrites and if this is actually the most concrete point to assign a value
   *  here, we should check also if the assigned association target exists actually. This is currently based on trust.
   * @param typeHandle
   * @param instanceId
   * @param deepValueSet
   * @return
   */
  private def deriveAssociations(typeHandle: TypeHandle, instanceId: String, deepValueSet: Set[ConcreteValue]): Set[AssociationData] = {
    val associationRules = typeHandle.getModelElement.definition.getAssociationRules
    val result = mutable.Set[AssociationData]()
    associationRules.foreach(associationRule => {
      //there may be concrete values of this rule
      deepValueSet.filter(value => value.concreteOf(associationRule)).foreach(value => {
        val relationName = value.valueName
        val concreteAssociation = value.getAssociationDescriptor
        val target = concreteAssociation.targetModelElement
        //val targetIdentity = concreteAssociation.targetIdentity // we assume only singleton identities here!
        result.add(AssociationData(0, relationName, instanceId, ModelElement.RESOLVE, isFinal = true))
      })
    })
    result.toSet
  }

  //TODO construct Instance from a lot of stuff here #
  // parentRelations do not need to be considered :D, they are loaded only on unfold

  /**
   * Load and construct the DeepInstance of a given TypeHandle.
   * <p> This operation must first check, that an Instance can and should be constructed at all. This is not the case,
   * if the TypeHandle describes a reference-modelElement (#-Identity). In case of singleton objects, type-specific flags
   * are set.
   * <p> If no instance can be constructed, because no instance data was found or the given TypeHandle is corrupted, an
   * empty option is returned.
   * <p> <strong>In the general case, this operation returns a DeepInstance which is not unfolded!</strong>
   * Although depending on the used Registry, an unfolded or partially unfolded (ModelElement-side) result is possible.
   *
   * @param instanceData
   * @param configuration
   * @param typeHandle
   * @return
   */
  def loadInstance(instanceData: InstanceData, shape: ShapeWrapper, typeHandle: TypeHandle): Option[DeepInstance] = {
    //TODO maybe some verification or remove the option return
    Some(new DeepInstance(instanceData.instanceId, instanceData.identity, shape, typeHandle, registry))
  }

}
