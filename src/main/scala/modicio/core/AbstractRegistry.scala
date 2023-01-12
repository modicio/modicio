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
abstract class AbstractRegistry(val typeFactory: TypeFactory, val instanceFactory: InstanceFactory) extends Registry {

  //protected var baseModels: mutable.Map[String, BaseModel] = mutable.Map[String, BaseModel]()

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
  def setType(typeHandle: TypeHandle, importMode: Boolean = false ): Future[Any] = {
    val modelElement = typeHandle.getModelElement
    containsRoot flatMap (root => {
      if(root || (modelElement.name == ModelElement.ROOT_NAME && modelElement.identity == ModelElement.REFERENCE_IDENTITY)){
        println("SET TYPE")
        println(typeHandle.getTypeName, typeHandle.getTypeIdentity)
        setNode(typeHandle, importMode) flatMap (_ => {
          if(!importMode && modelElement.identity == ModelElement.REFERENCE_IDENTITY){
            incrementRunning
          }else{
            Future.successful()
          }
        })
      }else{
        println("SET TYPE")
        println("failed for",typeHandle.getTypeName, typeHandle.getTypeIdentity)
        Future.failed(throw new IllegalArgumentException("Registry must contain ROOT element"))
      }
    })
  }

  /**
   * Template-method called by [[AbstractRegistry#setType setType()]] exclusively.
   * <p> This operation is implemented by a concrete registry and stores a new dynamic or forked model-element.
   *
   * <p><strong>Check setType documentation!</strong>
   *
   * @param typeHandle [[TypeHandle TypeHandle]] of a dynamic or forked model-element to store/register
   * @return TODO doc
   */
  protected def setNode(typeHandle: TypeHandle, importMode: Boolean = false): Future[Any]

}
