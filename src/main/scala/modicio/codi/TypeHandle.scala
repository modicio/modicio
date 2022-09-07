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
package modicio.codi

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future


class TypeHandle(private val fragment: Fragment, val static: Boolean) {

  def isValid: Boolean = fragment.isValid

  def getTypeName: String = fragment.name

  def getTypeIdentity: String = fragment.identity

  def getIsStatic: Boolean = static

  def getIsTemplate: Boolean = fragment.isTemplate

  def isConcrete: Boolean = fragment.isConcrete

  def hasSingleton: Future[Boolean] = fragment.hasSingleton

  def hasSingletonRoot: Future[Boolean] = fragment.hasSingletonRoot

  def updateSingletonRoot(): Future[Any] = fragment.updateSingletonRoot()

  private[modicio] def getFragment: Fragment = fragment

  def unfold(): Future[TypeHandle] = fragment.unfold() map (_ => this)

  def commit(): Future[Unit] = fragment.commit()

  def iterator: TypeIterator = new TypeIterator(fragment)

  def applyRule(rule: Rule): Unit = {
    if (!static) {
      fragment.applyRule(rule)
    } else {
      throw new Exception("Forbidden: instantiated types are not changeable")
    }
  }

  def removeRule(rule: Rule): Unit = {
    if (!static) {
      fragment.definition.removeRule(rule)
    } else {
      throw new Exception("Forbidden: instantiated types are not changeable")
    }
  }

  def removeRule(ruleID: String): Unit = {
    if (!static) {
      fragment.definition.removeRuleByID(ruleID)
    } else {
      throw new Exception("Forbidden: instantiated types are not changeable")
    }
  }

  def getAssociated: Set[TypeHandle] = {
    fragment.associations.toSet
  }

}
