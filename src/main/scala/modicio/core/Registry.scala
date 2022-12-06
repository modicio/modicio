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

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
 * @param typeFactory
 * @param instanceFactory
 */
abstract class Registry(val typeFactory: TypeFactory, val instanceFactory: InstanceFactory) {

  //protected var baseModels: mutable.Map[String, BaseModel] = mutable.Map[String, BaseModel]()

  def getReferenceTimeIdentity: Future[TimeIdentity]

  def incrementVariant: Future[Any]

  def incrementRunning: Future[Any]

  def containsRoot: Future[Boolean]

  def getType(name: String, identity: String): Future[Option[TypeHandle]]

  def getReferences: Future[Set[TypeHandle]]

  def exchangeModel(set: Set[TypeHandle]): Future[Any]

  def getReferenceTypes: Future[Set[String]]

  def getAllTypes: Future[Set[String]]

  def getInstanceVariants: Future[Seq[(Long, String)]]

  def getTypeVariants: Future[Seq[(Long, String)]]

  def getVariantMap: Future[Map[(Long, String), Int]]

  /**
   *
   * @param name
   * @return
   */
  def getSingletonRefsOf(name: String): Future[Set[DeepInstance]]

  /**
   * Add a [[ModelElement ModelElement]] to this Registry.
   * This implementation is slightly more complicated then necessary to provide support
   * for future model implementation changes.
   * <p>
   * <strong>
   *   If this method is called, the TimeIdentity of the stored ModelElement must increment its version and the
   *   running version must be incremented in ALL TimeIdentities part of a reference ModelElement as well.
   * </strong>
   * <p> If the TypeHandle to add has a not yet wired (check local id) TimeIdentity, this must be applied as well.
   *
   * @param typeHandle [[TypeHandle TypeHandle]] of the model-element to store/register
   * @return TODO doc
   */
  def setType(typeHandle: TypeHandle): Future[Any] = {
    val modelElement = typeHandle.getModelElement
    containsRoot flatMap (root => {
      if(root || (modelElement.name == ModelElement.ROOT_NAME && modelElement.identity == ModelElement.REFERENCE_IDENTITY)){
        println("SET TYPE")
        println(typeHandle.getTypeName, typeHandle.getTypeIdentity)
        for{
          timeIdentity <- setNode(typeHandle)
          _ <- incrementRunning
        } yield timeIdentity
      }else{
        println("SET TYPE")
        println("failed for",typeHandle.getTypeName, typeHandle.getTypeIdentity)
        Future.failed(throw new IllegalArgumentException("Registry must contain ROOT element"))
      }
    })
  }

  /**
   * Template-method called by [[Registry#setType setType()]] exclusively.
   * <p> This operation is implemented by a concrete registry and stores a new dynamic or forked model-element.
   *
   * <p><strong>Check setType documentation!</strong>
   *
   * @param typeHandle [[TypeHandle TypeHandle]] of a dynamic or forked model-element to store/register
   * @return TODO doc
   */
  protected def setNode(typeHandle: TypeHandle): Future[Any]

  /**
   * Remove parts of the model in a way producing a minimal number of overall deletions while trying to retain integrity
   * <p> <strong>Experimental Feature</strong>
   * <p> In case of a reference-identity ModelElement, the ModelElement is deleted only. In consequence, children pointing to that ModelElement
   * and other ModelElements associating this ModelElement become invalid and must be repaired manually.
   * <strong>
   * If this case is called, the running version must be incremented in ALL TimeIdentities part of reference ModelElements.
   * </strong>
   * <p> If the TypeHandle to add has a not yet wired (check local id) TimeIdentity, this must be applied as well.
   * <p> In case of a singleton-identity ModelElement, the whole singleton-fork of the ModelElement tree and the corresponding
   * [[DeepInstance DeepInstance]] tree are removed.
   *
   * @param name     of the [[ModelElement ModelElement]] trying to remove
   * @param identity of the [[ModelElement ModelElement]] trying to remove
   * @return
   */
  def autoRemove(name: String, identity: String): Future[Any]

  def autoRemove(instanceId: String): Future[Any]

  def get(instanceId: String): Future[Option[DeepInstance]]
  def getAll(typeName: String): Future[Set[DeepInstance]]

  def setInstance(deepInstance: DeepInstance): Future[Unit]

}
