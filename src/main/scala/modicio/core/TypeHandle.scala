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

import modicio.core.rules.Slot

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future


class TypeHandle(private val modelElement: ModelElement, val static: Boolean) {

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

  def commit(): Future[Any] = modelElement.commit()

  def iterator: TypeIterator = new TypeIterator(modelElement)

  def applyRule(rule: Rule): Unit = {
    if (!static) {
      modelElement.applyRule(rule)
    } else {
      throw new Exception("Forbidden: instantiated types are not changeable")
    }
  }

  def removeRule(rule: Rule): Unit = {
    if (!static) {
      modelElement.definition.removeRule(rule)
    } else {
      throw new Exception("Forbidden: instantiated types are not changeable")
    }
  }

  def removeRule(ruleID: String): Unit = {
    if (!static) {
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

}
