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
package modicio.codi.rules.api

import modicio.codi.Rule
import modicio.codi.api.RuleJ
import modicio.codi.rules.ExtensionRule

class ExtensionRuleJ(nativeValue: java.lang.String) extends ExtensionRule(nativeValue) with RuleJ {

  override def getRule: Rule = this

  override def forkJ(identity: String): RuleJ = new ExtensionRuleJ(super.fork(identity).nativeValue)

}
