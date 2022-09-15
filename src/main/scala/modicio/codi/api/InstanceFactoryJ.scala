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
package modicio.codi.api

import modicio.codi.datamappings.InstanceData
import modicio.codi.{DeepInstance, Shape, TypeHandle}
import modicio.verification.{DefinitionVerifier, ModelVerifier}

import modicio.api.JavaAPIConversions._

class InstanceFactoryJ(definitionVerifier: DefinitionVerifier,
                       modelVerifier: ModelVerifier) extends modicio.codi.InstanceFactory(definitionVerifier, modelVerifier){

  def newInstanceJ(typeName: java.lang.String): java.util.concurrent.CompletableFuture[DeepInstance] = super.newInstance(typeName)

  def newInstanceJ(typeName: java.lang.String, newIdentity: java.lang.String): java.util.concurrent.CompletableFuture[DeepInstance] = super.newInstance(typeName, newIdentity)

  def loadInstanceJ(instanceData: InstanceData, shape: Shape, typeHandle: TypeHandle): java.util.Optional[DeepInstance] = super.loadInstance(instanceData, shape, typeHandle)
}
