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
 * @param isTemplate if the template can be instantiated directly or only used as part of an parentRelation hierarchy / isAbstract
 */
class Node
(
  name: String,
  identity: String,
  isTemplate: Boolean,
  timeIdentity: TimeIdentity
) extends ModelElement(name, identity, isTemplate, timeIdentity) {

  override final def isNode: Boolean = true

  protected val parentRelations: mutable.Set[ModelElement] = mutable.Set()

  override def getParents: Set[ModelElement] = Set.from(parentRelations)

  override def unfold(): Future[Unit] = {
    fold()
    super.unfold() flatMap (_ => {
      //resolve parentRelations
      val parentRelationRules = definition.getParentRelationRules
      if (parentRelationRules.isEmpty) {
        Future.successful((): Unit)
      }else{
        Future.sequence(parentRelationRules.map(parentRelationRule => {
          registry.getType(parentRelationRule.parentName, parentRelationRule.parentIdentity)
        })) map (handleOptions => {
          parentRelations.addAll(handleOptions.filter(_.isDefined).map(_.get.getModelElement))
        }) flatMap (_ => {
          unfoldParentRelations()
        })
      }
    })
  }

  private def unfoldParentRelations(): Future[Unit] = {
    Future.sequence(parentRelations.map(_.unfold())) flatMap (_ => Future.unit)
  }

  override def fold(): Unit = {
    super.fold()
    parentRelations.clear()
  }

  override private[modicio] def toData: (ModelElementData, Set[RuleData]) = {
    val modelElementData = ModelElementData(name, identity, isTemplate, isNode = true, timeIdentity.variantTime,
      timeIdentity.runningTime, timeIdentity.versionTime, timeIdentity.variantId, timeIdentity.runningId, timeIdentity.versionId)
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
    parentRelations.foreach(parentRelation => parentRelation.fork(identity))
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

  /**
   * <p> Trigger the persistence process for this ModelElement. Child classes may overwrite the behaviour of this method.
   * <p> See overwriting implementations for more information.
   *
   * @return Future[Unit] - after the persistence process was completed
   */
  override def commit(): Future[Any] = {
    super.commit() flatMap (_ => Future.sequence(parentRelations.map(_.commit())))
  }

}
