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

package modicio.codi.rules

import modicio.core.Rule
import modicio.core.rules.ParentRelationRule
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should

class RuleSpec extends AnyFlatSpec with should.Matchers {

  val nativeStringNoId = ":#:Project"
  val nativeStringId = "abc:#:Project"

  "Rule" must "be correctly constructed from a (native value) DSL string with empty id" in {
    val rule = new ParentRelationRule(nativeStringNoId)
    rule.id should be (Rule.UNKNOWN_ID)
  }

  it must "be correctly evaluate a predefined id" in {
    val rule = new ParentRelationRule(nativeStringId)
    rule.id should be("abc")
  }

  it must "be correctly using autoId if enabled" in {
    Rule.enableAutoID()
    val rule = new ParentRelationRule(nativeStringNoId)
    Rule.disableAutoID()
    rule.id should not be Rule.UNKNOWN_ID
  }

}
