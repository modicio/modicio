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
package modicio.core.api

import modicio.core.datamappings.api.RuleDataJ
import modicio.core.rules.api.{AssociationRuleJ, AttributeRuleJ, ParentRelationRuleJ}
import modicio.core.values.api.ConcreteValueJ
import modicio.core.{Definition, Rule}
import modicio.api.JavaAPIConversions._
import modicio.core.rules.{AssociationRule, AttributeRule, ParentRelationRule}
import modicio.core.values.ConcreteValue

class DefinitionJ(val definition: Definition) extends BaseJ {

  def getRulesJ: java.util.Set[Rule] = definition.getRules

  def toDataJ(name: java.lang.String, identity: java.lang.String): java.util.Set[RuleDataJ] = convert(definition.toData(name, identity))

  def forkJ(identity: java.lang.String): DefinitionJ = definition.fork(identity)

  override def getAttributeRulesJ: java.util.Set[AttributeRuleJ] = convert(definition.getAttributeRules)

  override def getAssociationRulesJ: java.util.Set[AssociationRuleJ] = convert(definition.getAssociationRules)

  override def getParentRelationRulesJ: java.util.Set[ParentRelationRuleJ] = convert(definition.getParentRelationRules)

  override def getConcreteValuesJ: java.util.Set[ConcreteValueJ] = convert(definition.getConcreteValues)

  override def getAttributeRules: Set[AttributeRule] = definition.getAttributeRules

  override def getAssociationRules: Set[AssociationRule] = definition.getAssociationRules

  override def getParentRelationRules: Set[ParentRelationRule] = definition.getParentRelationRules

  override def getConcreteValues: Set[ConcreteValue] = definition.getConcreteValues

}
