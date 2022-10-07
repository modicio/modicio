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

import modicio.api.JavaAPIConversions._
import modicio.core.datamappings.api.{ModelElementDataJ, RuleDataJ}
import modicio.core.{Rule, TypeFactory}
import modicio.verification.api.{DefinitionVerifierJ, ModelVerifierJ}

import java.util.concurrent.CompletableFuture
import scala.concurrent.ExecutionContext.Implicits.global

class TypeFactoryJ(definitionVerifier: DefinitionVerifierJ, modelVerifier: ModelVerifierJ)
  extends TypeFactory(definitionVerifier, modelVerifier) {

  def setRegistryJ(registry: RegistryJ): Unit = super.setRegistry(registry)

  def newTypeJ(name: java.lang.String, identity: java.lang.String, isTemplate: Boolean): CompletableFuture[TypeHandleJ] = super.newType(name, identity, isTemplate) map convert

  def loadTypeJ(modelElementData: ModelElementDataJ, ruleData: java.util.Set[RuleDataJ]): TypeHandleJ = super.loadType(modelElementData, ruleData.map(convert))

  def loadRuleJ(ruleData: RuleDataJ): Rule = super.loadRule(ruleData)
}
