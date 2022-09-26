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

import modicio.codi.Base
import modicio.codi.rules.api.{AssociationRuleJ, AttributeRuleJ, ExtensionRuleJ}
import modicio.api.JavaAPIConversions._
import modicio.codi.values.api.ConcreteValueJ

trait BaseJ extends Base{
  def getAttributeRulesJ: java.util.Set[AttributeRuleJ] = convert(getAttributeRules)

  def getAssociationRulesJ: java.util.Set[AssociationRuleJ] = convert(getAssociationRules)

  def getExtensionRulesJ: java.util.Set[ExtensionRuleJ] = convert(getExtensionRules)

  def getConcreteValuesJ: java.util.Set[ConcreteValueJ] = convert(getConcreteValues)

}
