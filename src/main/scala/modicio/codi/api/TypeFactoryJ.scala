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

import modicio.api.JavaAPIConversions._
import modicio.codi.datamappings.api.{FragmentDataJ, RuleDataJ}
import modicio.codi.{Rule, TypeFactory}
import modicio.verification.api.{DefinitionVerifierJ, ModelVerifierJ}

class TypeFactoryJ(definitionVerifier: DefinitionVerifierJ, modelVerifier: ModelVerifierJ)
  extends TypeFactory(definitionVerifier, modelVerifier) {

  def setRegistryJ(registry: RegistryJ): Unit = super.setRegistry(registry)

  def newTypeJ(name: java.lang.String, identity: java.lang.String, isTemplate: Boolean): TypeHandleJ = super.newType(name, identity, isTemplate)

  def loadTypeJ(fragmentData: FragmentDataJ, ruleData: java.util.Set[RuleDataJ]): TypeHandleJ = super.loadType(fragmentData, ruleData.map(convert))

  def loadRuleJ(ruleData: RuleDataJ): Rule = super.loadRule(ruleData)
}
