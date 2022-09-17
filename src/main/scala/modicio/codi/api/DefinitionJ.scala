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

import modicio.codi.datamappings.api.RuleDataJ
import modicio.codi.rules.api.{AssociationRuleJ, AttributeRuleJ, ExtensionRuleJ}
import modicio.codi.values.api.ConcreteValueJ
import modicio.codi.{Definition, Rule}
import modicio.api.JavaAPIConversions._

class DefinitionJ(val definition: Definition) {

  def getRulesJ: java.util.Set[Rule] = definition.getRules

  def toDataJ(name: java.lang.String, identity: java.lang.String): java.util.Set[RuleDataJ] = convert(definition.toData(name, identity))

  def forkJ(identity: java.lang.String): DefinitionJ = definition.fork(identity)

  def getAttributeRulesJ: java.util.Set[AttributeRuleJ] = convert(definition.getAttributeRules)

  def getAssociationRulesJ: java.util.Set[AssociationRuleJ] = convert(definition.getAssociationRules)

  def getExtensionRulesJ: java.util.Set[ExtensionRuleJ] = convert(definition.getExtensionRules)

  def getConcreteValuesJ: java.util.Set[ConcreteValueJ] = convert(definition.getConcreteValues)

}
