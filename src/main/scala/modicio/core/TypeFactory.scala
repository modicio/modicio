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
package modicio.core

import modicio.core.datamappings.{ModelElementData, RuleData}
import modicio.core.rules.{AssociationRule, AttributeRule, ExtensionRule, RuleDataType}
import modicio.core.values.ConcreteValue
import modicio.verification.{DefinitionVerifier, ModelVerifier}

/**
 * @param definitionVerifier
 * @param modelVerifier
 */
class TypeFactory(private[modicio] val definitionVerifier: DefinitionVerifier,
                  private[modicio] val modelVerifier: ModelVerifier) {

  private var registry: Registry = _

  def setRegistry(registry: Registry): Unit = this.registry = registry

  def newType(name: String, identity: String, isTemplate: Boolean): TypeHandle = {
    val definition = new Definition(definitionVerifier)
    val modelElement = new Node(name, identity, isTemplate /*, modelVerifier*/)

    modelElement.setRegistry(registry)
    modelElement.setDefinition(definition)
    modelElement.setVerifiers(definitionVerifier, modelVerifier)

    modelElement.createHandle
  }

  def loadType(modelElementData: ModelElementData, ruleData: Set[RuleData]): TypeHandle = {
    val definition = new Definition(definitionVerifier)
    ruleData.foreach(data => definition.applyRule(loadRule(data)))

    val modelElement = new Node(modelElementData.name, modelElementData.identity, modelElementData.isTemplate)

    modelElement.setRegistry(registry)
    modelElement.setDefinition(definition)
    modelElement.setVerifiers(definitionVerifier, modelVerifier)

    modelElement.createHandle
  }

  def loadRule(ruleData: RuleData): Rule = {
    ruleData.typeOf match {
      case RuleDataType.ATTRIBUTE => new AttributeRule(ruleData.nativeValue)
      case RuleDataType.VALUE => new ConcreteValue(ruleData.nativeValue)
      case RuleDataType.ASSOCIATION => new AssociationRule(ruleData.nativeValue)
      case RuleDataType.EXTENSION => new ExtensionRule(ruleData.nativeValue)
      case _ => throw new IllegalArgumentException("Cannot determine RuleData typeOf")
    }
  }

}
