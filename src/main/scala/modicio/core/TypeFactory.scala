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

import modicio.core.datamappings.{ModelElementData, PluginData, RuleData}
import modicio.core.rules.{AssociationRule, AttributeRule, ParentRelationRule, RuleDataType}
import modicio.core.values.ConcreteValue
import modicio.verification.{DefinitionVerifier, ModelVerifier}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
 * @param definitionVerifier
 * @param modelVerifier
 */
class TypeFactory(private[modicio] val definitionVerifier: DefinitionVerifier,
                  private[modicio] val modelVerifier: ModelVerifier) {

  private var registry: Registry = _

  def setRegistry(registry: Registry): Unit = this.registry = registry

  def newType(name: String, identity: String, isTemplate: Boolean, defaultTimeIdentity: Option[TimeIdentity] = None):
  Future[TypeHandle] = {

    //println("CREATE NEW TYPE")
    //println(name, identity, isTemplate, defaultTimeIdentity)

    val definition = new DefinitionWrapper(definitionVerifier)

    val tid = TimeIdentity.create

    for {
      time <- {
        if (defaultTimeIdentity.isDefined) {
          Future.successful(defaultTimeIdentity.get)
        } else {
          registry.getReferenceTimeIdentity
        }
      }
      typeHandle <- {
        val modelElement = new ModelElement(name, identity, isTemplate, time)
        modelElement.setRegistry(registry)
        modelElement.setDefinition(definition)
        modelElement.setVerifiers(definitionVerifier, modelVerifier)
        Future.successful(modelElement.createHandle)
      }
    } yield typeHandle

  }

  def loadType(modelElementData: ModelElementData, ruleData: Set[RuleData], pluginData: Set[PluginData]): TypeHandle = {
    val rules = ruleData.map(data => loadRule(data))
    val plugins = pluginData.map(data => Plugin.formatData(data))
    val definition = new DefinitionWrapper(definitionVerifier, rules, plugins)

    val timeIdentity = TimeIdentity.fromModelElementData(modelElementData)
    val modelElement = new ModelElement(modelElementData.name, modelElementData.identity, modelElementData.isTemplate, timeIdentity)

    modelElement.setRegistry(registry)
    modelElement.setDefinition(definition)
    definition.cleanVolatile()
    modelElement.setVerifiers(definitionVerifier, modelVerifier)

    modelElement.createHandle
  }

  def loadRule(ruleData: RuleData): Rule = {
    ruleData.typeOf match {
      case RuleDataType.ATTRIBUTE => new AttributeRule(ruleData.nativeValue)
      case RuleDataType.VALUE => new ConcreteValue(ruleData.nativeValue)
      case RuleDataType.ASSOCIATION => new AssociationRule(ruleData.nativeValue)
      case RuleDataType.PARENT_RELATION => new ParentRelationRule(ruleData.nativeValue)
      case _ => throw new IllegalArgumentException("Cannot determine RuleData typeOf")
    }
  }

}
