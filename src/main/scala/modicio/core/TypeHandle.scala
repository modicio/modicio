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

import modicio.core.rules.{AssociationRule, AttributeRule, ParentRelationRule, Slot}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future


class TypeHandle(private val modelElement: ModelElement, val static: Boolean) {

  private var importMode: Boolean = false

  private[modicio] def openImportMode(): Unit = this.importMode = true
  private[modicio] def closeImportMode(): Unit = this.importMode = false


  def isValid: Boolean = modelElement.isValid

  def getTypeName: String = modelElement.name

  def getTypeIdentity: String = modelElement.identity

  def getIsStatic: Boolean = static

  def getIsTemplate: Boolean = modelElement.isTemplate

  def isConcrete: Boolean = modelElement.isConcrete

  def getTimeIdentity: TimeIdentity = modelElement.getTimeIdentity

  def hasSingleton: Future[Boolean] = modelElement.hasSingleton

  def hasSingletonRoot: Future[Boolean] = modelElement.hasSingletonRoot

  def updateSingletonRoot(): Future[Any] = modelElement.updateSingletonRoot()

  private[modicio] def getModelElement: ModelElement = modelElement

  def unfold(): Future[TypeHandle] = modelElement.unfold() map (_ => this)

  def commit(importMode: Boolean = false): Future[Any] = modelElement.commit(importMode)

  def iterator: TypeIterator = new TypeIterator(modelElement)

  def getTypeClosure: Set[String] = modelElement.getTypeClosure

  /**
   * <p> Get the direct parent relations (extensions) of a [[ModelElement]].
   * <p> Note that indirections are not covered! If the [[ModelElement]] is unfolded, please use [[ModelElement.getTypeClosure]] instead.
   *
   * @return Set[ParentRelationRule] - direct parent relations
   */
  def getParentRelations: Set[ParentRelationRule] = modelElement.definition.getParentRelationRules

  def getAttributes: Set[AttributeRule] = modelElement.definition.getAttributeRules

  def getDeepAttributes: Set[AttributeRule] = modelElement.deepAttributeRuleSet

  def getAssociations: Set[AssociationRule] = modelElement.definition.getAssociationRules

  def getDeepAssociations: Set[AssociationRule] = modelElement.deepAssociationRuleSet

  def applyRule(rule: Rule): Unit = {
    if (!static || importMode) {
      modelElement.applyRule(rule)
    } else {
      throw new Exception("Forbidden: instantiated types are not changeable")
    }
  }

  def removeRule(rule: Rule): Unit = {
    if (!static || importMode) {
      modelElement.definition.removeRule(rule)
    } else {
      throw new Exception("Forbidden: instantiated types are not changeable")
    }
  }

  def removeRule(ruleID: String): Unit = {
    if (!static || importMode) {
      modelElement.definition.removeRuleByID(ruleID)
    } else {
      throw new Exception("Forbidden: instantiated types are not changeable")
    }
  }

  def applySlot(ruleID: String, variantTimeArg: String): Boolean = {
    val ruleOption = modelElement.definition.getAssociationRules.find(_.id == ruleID)
    if(ruleOption.isDefined){
      val rule = ruleOption.get
      val target = rule.targetName
      rule.getInterface.addSlot(Slot(target, variantTimeArg))
        true
    } else {
      false
    }
  }

  def removeSlot(ruleID: String, variantTimeArg: String): Boolean = {
    val ruleOption = modelElement.definition.getAssociationRules.find(_.id == ruleID)
    if (ruleOption.isDefined) {
      val rule = ruleOption.get
      val interface = rule.getInterface
      val slots = interface.getSlots.filter(_.targetVariantTimeArg == variantTimeArg)
      slots.foreach(interface.removeSlot)
      true
    } else {
      false
    }
  }

  def getAssociated: Set[TypeHandle] = {
    modelElement.associations.toSet
  }

  def getPlugins: Set[Plugin] = {
    modelElement.definition.getPlugins
  }

  def addPlugin(plugin: Plugin): Unit = {
    if(!getIsStatic) {
      modelElement.definition.applyPlugin(plugin)
    }
  }

  def getPluginsByDescription(description: String): Set[Plugin] = {
    getPlugins.filter(_.description == description)
  }

  def getPluginsByResolver(resolver: String): Set[Plugin] = {
    getPlugins.filter(_.resolver == resolver)
  }

  def removePlugin(plugin: Plugin): Unit = {
    modelElement.definition.removePlugin(plugin)
  }


}
