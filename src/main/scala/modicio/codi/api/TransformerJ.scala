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

import modicio.codi.Transformer
import modicio.verification.api.{DefinitionVerifierJ, ModelVerifierJ}
import modicio.api.JavaAPIConversions._

import java.util.Optional
import scala.concurrent.Future

abstract class TransformerJ[IN, OUT](registry: RegistryJ,
                                     definitionVerifier: DefinitionVerifierJ,
                                     modelVerifier: ModelVerifierJ)
  extends Transformer[IN, OUT](registry, definitionVerifier, modelVerifier) {

  override final def extend(input: IN): Future[Unit] = extendJ(input)

  override final def decompose(input: Option[String]): Future[OUT] = decomposeJ(input)

  def extendJ(input: IN): java.util.concurrent.CompletableFuture[Unit]

  def decomposeJ(input: Optional[java.lang.String]): java.util.concurrent.CompletableFuture[OUT]
}
