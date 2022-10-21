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

package modicio

import modicio.core.{InstanceFactory, TypeFactory}
import modicio.nativelang.defaults.{SimpleDefinitionVerifier, SimpleMapRegistry, SimpleModelVerifier}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should

class AbstractIntegrationSpec extends AnyFlatSpec with should.Matchers {

  val modelVerifier = new SimpleModelVerifier()
  val definitionVerifier = new SimpleDefinitionVerifier()

  val typeFactory = new TypeFactory(definitionVerifier, modelVerifier)
  val instanceFactory = new InstanceFactory(definitionVerifier, modelVerifier)

  val registry = new SimpleMapRegistry(typeFactory, instanceFactory)
  typeFactory.setRegistry(registry)
  instanceFactory.setRegistry(registry)

}
