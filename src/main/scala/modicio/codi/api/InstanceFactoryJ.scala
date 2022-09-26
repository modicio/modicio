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

import modicio.api.JavaAPIConversions._
import modicio.codi.InstanceFactory
import modicio.verification.api.{DefinitionVerifierJ, ModelVerifierJ}

import scala.concurrent.ExecutionContext.Implicits.global

class InstanceFactoryJ(definitionVerifier: DefinitionVerifierJ,
                       modelVerifier: ModelVerifierJ) extends InstanceFactory(definitionVerifier, modelVerifier){

  def setRegistryJ(registry: RegistryJ): Unit = super.setRegistry(registry)

  def newInstanceJ(typeName: java.lang.String): java.util.concurrent.CompletableFuture[DeepInstanceJ] =
    super.newInstance(typeName) map convert

/*
  def newInstanceJ(typeName: java.lang.String, newIdentity: java.lang.String): java.util.concurrent.CompletableFuture[DeepInstanceJ] =
    super.newInstance(typeName, newIdentity) map convert

  def loadInstanceJ(instanceData: InstanceData, shape: Shape, typeHandle: TypeHandleJ): java.util.Optional[DeepInstanceJ] =
    super.loadInstance(instanceData, shape, typeHandle) map convert
 */
}
