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

import modicio.codi.Rule

import scala.beans.BeanProperty

class RuleJ(@BeanProperty private val rule: Rule) extends Rule(rule.serialise()){

  def getId: java.lang.String = rule.id

  def serialiseJ(): java.lang.String = rule.serialise()

  def serialiseSimpleJ(): java.lang.String = rule.serialiseSimple()

  def forkJ(identity: java.lang.String): RuleJ = new RuleJ(rule.fork(identity))

  def isPolymorphEqualJ(ruleJ: RuleJ): Boolean = rule.isPolymorphEqual(ruleJ.rule)

  override def serialise(): String = rule.serialise()

  override def serialiseSimple(): String = rule.serialiseSimple()

  override def verify(): Boolean = rule.verify()

  override def fork(identity: String): Rule = rule.fork(identity)

  override def getDataType: Int = rule.getDataType

  override def isPolymorphEqual(rule: Rule): Boolean = rule.isPolymorphEqual(rule)
}