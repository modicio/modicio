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

import modicio.verification.{DefinitionVerifier, ModelVerifier}

import scala.concurrent.Future

abstract class Transformer[MODEL, COMPARTMENT](protected val registry: Registry,
                              protected val definitionVerifier: DefinitionVerifier,
                              protected val modelVerifier: ModelVerifier) {

  protected val typeFactory = new TypeFactory(definitionVerifier, modelVerifier)
  typeFactory.setRegistry(registry)

  def extend(input: MODEL): Future[Any]

  def extendInstance(input: COMPARTMENT): Future[Any]

  def decomposeInstance(instanceId: String): Future[COMPARTMENT]

  def decomposeModel(): Future[MODEL]

}
