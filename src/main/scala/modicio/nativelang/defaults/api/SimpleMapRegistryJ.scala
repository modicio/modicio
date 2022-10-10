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
package modicio.nativelang.defaults.api

import modicio.core.Registry
import modicio.core.api.{InstanceFactoryJ, RegistryJ, TypeFactoryJ}
import modicio.nativelang.defaults.SimpleMapRegistry

import scala.language.implicitConversions

class SimpleMapRegistryJ(typeFactory: TypeFactoryJ, instanceFactoryJ: InstanceFactoryJ) extends SimpleMapRegistry(typeFactory, instanceFactoryJ) with RegistryJ {

  override def getRegistry: Registry = this

}