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

import modicio.core.Base
import modicio.core.rules.api.{AssociationRuleJ, AttributeRuleJ, ParentRelationRuleJ}
import modicio.api.JavaAPIConversions._
import modicio.core.values.api.ConcreteValueJ

trait BaseJ extends Base{
  def getAttributeRulesJ: java.util.Set[AttributeRuleJ] = convert(getAttributeRules)

  def getAssociationRulesJ: java.util.Set[AssociationRuleJ] = convert(getAssociationRules)

  def getParentRelationRulesJ: java.util.Set[ParentRelationRuleJ] = convert(getParentRelationRules)

  def getConcreteValuesJ: java.util.Set[ConcreteValueJ] = convert(getConcreteValues)

}
