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
package modicio.core.rules.api

import modicio.core.Rule
import modicio.core.api.RuleJ
import modicio.core.rules.AttributeRule

class AttributeRuleJ(nativeValue: java.lang.String) extends AttributeRule(nativeValue) with RuleJ {

  override def getRule: Rule = this

  override def forkJ(identity: String): RuleJ = new AttributeRuleJ(super.fork(identity).nativeValue)

}


