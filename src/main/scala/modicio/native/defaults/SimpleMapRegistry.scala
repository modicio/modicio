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
package modicio.native.defaults

import modicio.codi.{DeepInstance, Fragment, InstanceFactory, Registry, TypeFactory, TypeHandle}

import scala.collection.mutable
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class SimpleMapRegistry(typeFactory: TypeFactory, instanceFactory: InstanceFactory)
  extends Registry(typeFactory, instanceFactory) {

  private val typeRegistry = mutable.Map[String, mutable.Map[String, TypeHandle]]()
  private val instanceRegistry = mutable.Map[String, DeepInstance]()

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
      val result = typeGroup.get.filter(element => Fragment.isSingletonIdentity(element._1)).values.toSet
      Future.successful(result)
    }
  }

  override protected def setNode(typeHandle: TypeHandle): Future[Unit] = {
    val name = typeHandle.getTypeName
    val identity = typeHandle.getTypeIdentity
    if (!typeRegistry.contains(name)) {
      typeRegistry.addOne(name, mutable.Map[String, TypeHandle]())
    }
    val typeGroup = typeRegistry(name)
    if (typeGroup.contains(identity)) {
      typeGroup.remove(identity)
    }
    Future.successful(typeGroup.addOne(identity, typeHandle))
  }

  override def getReferences: Future[Set[TypeHandle]] = {
    Future.successful(typeRegistry.values.flatMap(_.values).filter(_.getTypeIdentity == Fragment.REFERENCE_IDENTITY)
      .toSet ++ baseModels.values.map(_.createHandle))
  }

  override def get(instanceId: String): Future[Option[DeepInstance]] = {
    if (DeepInstance.isSingletonRoot(instanceId)) {
      Future.successful(instanceRegistry.get(
        DeepInstance.deriveRootSingletonInstanceId(
          Fragment.decomposeSingletonIdentity(instanceId))))
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
   * <p> In case of a reference-identity Fragment, the Fragment is deleted only. In consequence, children pointing to that Fragment
   * and other Fragments associating this Fragment become invalid and must be repaired manually.
   * <p> In case of a singleton-identity Fragment, the whole singleton-fork of the Fragment tree and the corresponding
   * [[DeepInstance DeepInstance]] tree are removed.
   * <p> In case of a user-space identity, nothing happens yet => TODO
 *
   * @param name     of the [[Fragment Fragment]] trying to remove
   * @param identity of the [[Fragment Fragment]] trying to remove
   * @return
   */
  override def autoRemove(name: String, identity: String): Future[Any] = {

    if (identity == Fragment.REFERENCE_IDENTITY) {
      //In case of reference identity, remove model-element locally. FIXME The model may become invalid

      val typeGroupOption = typeRegistry.get(name)
      if (typeGroupOption.isDefined) {
        Future.successful(typeGroupOption.get.remove(identity))
      } else {
        Future.failed(new IllegalArgumentException("AUTO DELETE: No type group found"))
      }

    } else if (identity == Fragment.SINGLETON_IDENTITY) {
      //In case of a singleton identity fragment

      val singletonInstanceId = DeepInstance.deriveSingletonInstanceId(identity, name)

      //get the associated singleton deep instance
      val deepInstanceOption = instanceRegistry.get(singletonInstanceId)
      if (deepInstanceOption.isDefined) {
        //unfold the singleton deep-instance

        deepInstanceOption.get.unfold() flatMap (unfoldedInstance => {
          val extensions = unfoldedInstance.getTypeHandle.getFragment.getParents
          //delete all parent model-elements of the singleton deep-instance

          //delete the actual deep-instance and trigger deletion of its parents
          instanceRegistry.remove(singletonInstanceId)
          val mapOfFutures = extensions.map(extension => autoRemove(extension.name, Fragment.SINGLETON_IDENTITY))
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
      Future.successful()
    }
  }
}
