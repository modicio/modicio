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
package modicio.codi

import scala.collection.mutable
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

/**
 * @param typeFactory
 * @param instanceFactory
 */
abstract class Registry(val typeFactory: TypeFactory, val instanceFactory: InstanceFactory) {

  protected val baseModels: mutable.Map[String, BaseModel] = mutable.Map[String, BaseModel]()

  def getType(name: String, identity: String): Future[Option[TypeHandle]] = {
    if(identity == Fragment.REFERENCE_IDENTITY && baseModels.contains(name)){
      Future.successful(Some(baseModels(name).createHandle))
    }else{
      getDynamicType(name, identity)
    }
  }

  protected def getDynamicType(name: String, identity: String): Future[Option[TypeHandle]]

  def getReferences: Future[Set[TypeHandle]] = {
    getDynamicReferences map (refs => refs ++ baseModels.values.map(_.createHandle))
  }

  protected def getDynamicReferences: Future[Set[TypeHandle]]

  def getSingletonTypes(name: String): Future[Set[TypeHandle]]

  /**
   * Add a [[Fragment Fragment]] to this Registry.
   * <p> This operation must distinguish between [[BaseModel BaseModels]] with reference-identity and any other
   * model-elements. reference-BaseModels are always stored only in a non-persistent data structure and not passed to the
   * template-method handling persistence because reference BaseModels are coded into the application logic and are set
   * on application start always.
   *
   * @param typeHandle [[TypeHandle TypeHandle]] of the model-element to store/register
   * @return
   */
  def setType(typeHandle: TypeHandle): Future[Unit] = {
    val fragment = typeHandle.getFragment
    fragment match {
      case fragment: BaseModel => {
        baseModels.put(fragment.name, fragment)
        Future.successful()
      }
      case _ => setNode(typeHandle)
    }
  }

  /**
   * Template-method called by [[Registry#setType setType()]] exclusively.
   * <p> This operation is implemented by a concrete registry and stores a new dynamic or forked model-element.
   *
   * @param typeHandle [[TypeHandle TypeHandle]] of a dynamic or forked model-element to store/register
   * @return
   */
  protected def setNode(typeHandle: TypeHandle): Future[Unit]

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
  def autoRemove(name: String, SINGLETON_IDENTITY: String): Future[Any]

  def get(instanceId: String): Future[Option[DeepInstance]]
  def getAll(typeName: String): Future[Set[DeepInstance]]

  def setInstance(deepInstance: DeepInstance): Future[Unit]

}
