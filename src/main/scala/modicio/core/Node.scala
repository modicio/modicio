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

import scala.collection.mutable
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
 * @param name       name of the ModelElement, name and identity form a unique pair.
 * @param identity   identity of the ModelElement
 * @param isTemplate if the template can be instantiated directly or only used as part of an extension hierarchy / isAbstract
 */
class Node
(
  name: String,
  identity: String,
  isTemplate: Boolean
) extends ModelElement(name, identity, isTemplate) {

  override final def isNode: Boolean = true

  protected val extensions: mutable.Set[ModelElement] = mutable.Set()

  override def getParents: Set[ModelElement] = Set.from(extensions)

  override def unfold(): Future[Unit] = {
    fold()
    super.unfold() flatMap (_ => {
      //resolve extensions
      val extensionRules = definition.getExtensionRules
      if (extensionRules.isEmpty) {
        Future.successful((): Unit)
      }else{
        Future.sequence(extensionRules.map(extensionRule => {
          registry.getType(extensionRule.parentName, extensionRule.parentIdentity)
        })) map (handleOptions => {
          extensions.addAll(handleOptions.filter(_.isDefined).map(_.get.getModelElement))
        }) flatMap (_ => {
          unfoldExtensions()
        })
      }
    })
  }

  private def unfoldExtensions(): Future[Unit] = {
    Future.sequence(extensions.map(_.unfold())) flatMap (_ => Future.unit)
  }

  override def fold(): Unit = {
    super.fold()
    extensions.clear()
  }

  override private[modicio] def toData: (ModelElementData, Set[RuleData]) = {
    val modelElementData = ModelElementData(name, identity, isTemplate, isNode = true)
    val ruleData = definition.toData(name, identity)
    (modelElementData, ruleData)
  }

  /**
   * For a complete deep type by applying a new identity.
   * The type must be unfolded before forking!
   * @param identity
   * @return
   */
  override def fork(identity: String): ModelElement = {
    extensions.foreach(extension => extension.fork(identity))
    super.fork(identity)
  }

  override def applyRule(rule: Rule): Unit = {
    //TODO verify here
    definition.applyRule(rule)
    fold()
    Future.successful((): Unit)
  }

  override def removeRule(rule: Rule): Unit = {
    //TODO verify here
    definition.removeRule(rule)
    fold()
    Future.successful((): Unit)
  }

}