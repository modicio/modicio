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
package modicio.nativelang.defaults

import modicio.core._
import modicio.core.util.IdentityProvider

import scala.collection.mutable
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class SimpleMapRegistry(typeFactory: TypeFactory, instanceFactory: InstanceFactory)
  extends Registry(typeFactory, instanceFactory) {

  // name -> (identity -> model)
  private[modicio] val typeRegistry = mutable.Map[String, mutable.Map[String, TypeHandle]]()

  // instanceID -> instance
  private[modicio] val instanceRegistry = mutable.Map[String, DeepInstance]()

  private[modicio] def load(registry: SimpleMapRegistry): Unit = {
    this.typeRegistry.addAll(registry.typeRegistry)
    this.instanceRegistry.addAll(registry.instanceRegistry)
  }


  override def getReferenceTimeIdentity: Future[TimeIdentity] = {
    val rootGroup = typeRegistry.get(ModelElement.ROOT_NAME)
    if(rootGroup.nonEmpty && rootGroup.get.nonEmpty){
      val root = rootGroup.get.get(ModelElement.REFERENCE_IDENTITY)
        if(root.isEmpty) {
          Future.failed(new IllegalAccessException("No ROOT reference element present"))
        }else {
          Future.successful(root.get.getTimeIdentity)
        }
    }else{
     Future.failed(new IllegalAccessException("No ROOT reference element present"))
    }
  }

  override def incrementVariant: Future[Any] = {
      val variantTime = IdentityProvider.newTimestampId()
      val variantId = IdentityProvider.newRandomId()
      getReferences map (references => references.map(element =>
        element.getModelElement.setTimeIdentity(TimeIdentity.incrementVariant(element.getTimeIdentity, variantTime, variantId))))
  }

  override def incrementRunning: Future[Any] = {
    val runningTime = IdentityProvider.newTimestampId()
    val runningId = IdentityProvider.newRandomId()
    getReferences map (references => references.map(element =>
      element.getModelElement.setTimeIdentity(TimeIdentity.incrementRunning(element.getTimeIdentity, runningTime, runningId))))
  }

  override def containsRoot: Future[Boolean] = {
    if(typeRegistry.contains(ModelElement.ROOT_NAME) &&
      typeRegistry(ModelElement.ROOT_NAME).contains(ModelElement.REFERENCE_IDENTITY)) {
      Future.successful(true)
    }else{
      Future.successful(false)
    }
  }
  
  override def getTypes: Future[Set[String]] = getReferences map (references => references.map(_.getTypeName))

  override def getVariants: Future[Seq[(Long, String)]] = {
    val root = typeRegistry(ModelElement.ROOT_NAME).get(ModelElement.REFERENCE_IDENTITY)
    val rootTime = root.get.getTimeIdentity
    val res: Seq[(Long, String)] = instanceRegistry.values.map(i => {
      val time = i.getTypeHandle.getTimeIdentity
      (time.variantTime, time.variantId)
    }).toSeq :+ (rootTime.variantTime, rootTime.variantId)
    Future.successful(res)
  }

  override def getType(name: String, identity: String): Future[Option[TypeHandle]] = {
    val typeGroup = typeRegistry.get(name)
    if (typeGroup.isEmpty) {
      Future.successful(None)
    } else {
      val result = typeGroup.get.get(identity)
      Future.successful(result)
    }
  }

  override def getSingletonTypes(name: String): Future[Set[TypeHandle]] = {
    val typeGroup = typeRegistry.get(name)
    if (typeGroup.isEmpty) {
      Future.successful(Set())
    }else {
      val result = typeGroup.get.filter(element => ModelElement.isSingletonIdentity(element._1)).values.toSet
      Future.successful(result)
    }
  }

  override protected def setNode(typeHandle: TypeHandle): Future[TimeIdentity] = {
    val name = typeHandle.getTypeName
    val identity = typeHandle.getTypeIdentity
    if (!typeRegistry.contains(name)) {
      typeRegistry.addOne(name, mutable.Map[String, TypeHandle]())
    }
    val typeGroup = typeRegistry(name)
    if (typeGroup.contains(identity)) {
      typeGroup.remove(identity)
    }
    typeGroup.addOne(identity, typeHandle)
    if(identity == ModelElement.REFERENCE_IDENTITY){
      incrementRunning map (_ => typeHandle.getTimeIdentity)
    }else{
      Future.successful(typeHandle.getTimeIdentity)
    }

  }

  override def getReferences: Future[Set[TypeHandle]] = {
    Future.successful(typeRegistry.values.flatMap(_.values).filter(_.getTypeIdentity == ModelElement.REFERENCE_IDENTITY).toSet)
  }

  override def get(instanceId: String): Future[Option[DeepInstance]] = {
    if (DeepInstance.isSingletonRoot(instanceId)) {
      Future.successful(instanceRegistry.get(
        DeepInstance.deriveRootSingletonInstanceId(
          ModelElement.decomposeSingletonIdentity(instanceId))))
    } else {
      Future.successful(instanceRegistry.get(instanceId))
    }

  }

  override def getAll(typeName: String): Future[Set[DeepInstance]] = {
    Future.successful(instanceRegistry.toSet.filter(_._2.getTypeHandle.getTypeName == typeName).map(_._2))
  }

  override def setInstance(deepInstance: DeepInstance): Future[Unit] = {
    Future.successful(instanceRegistry.addOne(deepInstance.getInstanceId, deepInstance))
  }

  /**
   * Remove parts of the model in a way producing a minimal number of overall deletions while trying to retain integrity
   * <p> <strong>Experimental Feature</strong>
   * <p> In case of a reference-identity ModelElement, the ModelElement is deleted only. In consequence, children pointing to that ModelElement
   * and other ModelElements associating this ModelElement become invalid and must be repaired manually.
   * <p> In case of a singleton-identity ModelElement, the whole singleton-fork of the ModelElement tree and the corresponding
   * [[DeepInstance DeepInstance]] tree are removed.
   * <p> In case of a user-space identity, nothing happens yet => TODO
 *
   * @param name     of the [[ModelElement ModelElement]] trying to remove
   * @param identity of the [[ModelElement ModelElement]] trying to remove
   * @return
   */
  override def autoRemove(name: String, identity: String): Future[Any] = {

    if (identity == ModelElement.REFERENCE_IDENTITY) {
      //In case of reference identity, remove model-element locally. FIXME The model may become invalid

      val typeGroupOption = typeRegistry.get(name)
      if (typeGroupOption.isDefined) {
        typeGroupOption.get.remove(identity)
        incrementRunning
      } else {
        Future.failed(new IllegalArgumentException("AUTO DELETE: No type group found"))
      }

    } else if (identity == ModelElement.SINGLETON_IDENTITY) {
      //In case of a singleton identity modelElement

      val singletonInstanceId = DeepInstance.deriveSingletonInstanceId(identity, name)

      //get the associated singleton deep instance
      val deepInstanceOption = instanceRegistry.get(singletonInstanceId)
      if (deepInstanceOption.isDefined) {
        //unfold the singleton deep-instance

        deepInstanceOption.get.unfold() flatMap (unfoldedInstance => {
          val parentRelations = unfoldedInstance.getTypeHandle.getModelElement.getParents
          //delete all parent model-elements of the singleton deep-instance

          //delete the actual deep-instance and trigger deletion of its parents
          instanceRegistry.remove(singletonInstanceId)
          val mapOfFutures = parentRelations.map(parentRelation => autoRemove(parentRelation.name, ModelElement.SINGLETON_IDENTITY))
          Future.sequence(mapOfFutures)

        }) map (_ => {
          //delete the corresponding model-elements
          val typeGroupOption = typeRegistry.get(name)
          if (typeGroupOption.isDefined) {
            typeGroupOption.get.remove(identity)
          }
        })

      } else {
       Future.failed(new IllegalArgumentException("AUTO DELETE: No such singleton instance found"))
      }
    } else {
      //TODO
      Future.successful((): Unit)
    }
  }

}
