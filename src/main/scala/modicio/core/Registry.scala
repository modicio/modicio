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

import scala.concurrent.Future

/**
 * @param typeFactory
 * @param instanceFactory
 */
abstract class Registry(val typeFactory: TypeFactory, val instanceFactory: InstanceFactory) {

  //protected var baseModels: mutable.Map[String, BaseModel] = mutable.Map[String, BaseModel]()

  def getType(name: String, identity: String): Future[Option[TypeHandle]] = {
    //if(identity == ModelElement.REFERENCE_IDENTITY && baseModels.contains(name)){
    //  Future.successful(Some(baseModels(name).createHandle))
    //}else{
      getDynamicType(name, identity)
    //}
  }

  protected def getDynamicType(name: String, identity: String): Future[Option[TypeHandle]]

  def getReferences: Future[Set[TypeHandle]] = {
    //getDynamicReferences map (refs => refs ++ baseModels.values.map(_.createHandle))
    getDynamicReferences
  }

  protected def getDynamicReferences: Future[Set[TypeHandle]]

  def getSingletonTypes(name: String): Future[Set[TypeHandle]]

  /**
   * Add a [[ModelElement ModelElement]] to this Registry.
   * This implementation is slightly more complicated then necessary to provide support
   * for future model implementation changes.
   *
   * @param typeHandle [[TypeHandle TypeHandle]] of the model-element to store/register
   * @return
   */
  def setType(typeHandle: TypeHandle): Future[Unit] = {
    val modelElement = typeHandle.getModelElement
    modelElement match {
      //case modelElement: BaseModel => {
      //  baseModels.put(modelElement.name, modelElement)
      //  Future.successful((): Unit)
      //}
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
  def autoRemove(name: String, SINGLETON_IDENTITY: String): Future[Any]

  def get(instanceId: String): Future[Option[DeepInstance]]
  def getAll(typeName: String): Future[Set[DeepInstance]]

  def setInstance(deepInstance: DeepInstance): Future[Unit]

}
