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
import modicio.codi.{DeepInstance, Registry}

import java.util
import java.util.Optional
import java.util.concurrent.CompletableFuture
import scala.concurrent.ExecutionContext.Implicits.global

trait RegistryJ {

  def getRegistry: Registry

  def getSingletonTypesJ(name: String): java.util.concurrent.CompletableFuture[util.Set[TypeHandleJ]] =
    getRegistry.getSingletonTypes(name) map (s => convert(s.map(t => convert(t))))

  def autoRemoveJ(name: String, SINGLETON_IDENTITY: String): CompletableFuture[Any] = getRegistry.autoRemove(name, SINGLETON_IDENTITY)

  def getJ(instanceId: String): CompletableFuture[Optional[DeepInstanceJ]] = convert(getRegistry.get(instanceId)) map (s => convert(s.map(t => convert(t))))

  def getAllJ(typeName: String): CompletableFuture[util.Set[DeepInstanceJ]] = convert(getRegistry.getAll(typeName)) map (s => convert(s.map(t => convert(t))))

  def setInstanceJ(deepInstance: DeepInstance): CompletableFuture[Unit] = getRegistry.setInstance(deepInstance)

}
