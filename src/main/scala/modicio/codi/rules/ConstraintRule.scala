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

import modicio.codi.Rule

/**
 * TODO this rule is not implemented yet and serves only as conceptual placeholder
 * This feature is on the release Roadmap for a future version, an issue may be introduced.
 *
 * @param nativeValue the string representation in the native format
 */
class ConstraintRule(nativeValue: String) extends Rule(nativeValue) {

  override def serialise(): String = ???

  override def serialiseSimple(): String = ???

  override def verify(): Boolean = ???

  override def fork(identity: String): Rule = ???

  override def getDataType: Int = RuleDataType.CONSTRAINT

}

/**
 * BehaviourRule companion object for the static factory creator from parameters
 */
object ConstraintRule {
  def create(): ConstraintRule = ???
}