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
package modicio.verification.api

import modicio.codi.Rule
import modicio.codi.api.RuleJ
import modicio.verification.DefinitionVerifier

import modicio.api.JavaAPIConversions._

trait DefinitionVerifierJ extends DefinitionVerifier {

  override final def verify(rules: Set[Rule]): Boolean = verifyJ(convert(rules))

  def verifyJ(rules: java.util.Set[RuleJ]): Boolean

}
