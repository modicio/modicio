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
import modicio.core.{ModelElement, TypeIterator}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.language.implicitConversions

class TypeHandleJ(modelElement: ModelElement, static: Boolean) extends modicio.core.TypeHandle(modelElement, static) {

  override def getTypeName: String = super.getTypeName

  def getTypeIdentityJ: java.lang.String = super.getTypeIdentity

  def hasSingletonJ: java.util.concurrent.CompletableFuture[Boolean] = super.hasSingleton

  def hasSingletonRootJ: java.util.concurrent.CompletableFuture[Boolean] = super.hasSingletonRoot

  def updateSingletonRootJ(): java.util.concurrent.CompletableFuture[Any] = super.updateSingletonRoot()

  def unfoldJ(): java.util.concurrent.CompletableFuture[TypeHandleJ] = super.unfold() map (convert)

  def commitJ(): java.util.concurrent.CompletableFuture[Unit] = super.commit()

  def applyRuleJ(ruleJ: RuleJ): Unit = {
    super.applyRule(ruleJ.getRule)
  }

  def removeRuleJ(ruleJ: RuleJ): Unit = {
    super.removeRule(ruleJ.getRule)
  }

  def getAssociatedJ: java.util.Set[TypeHandleJ] = convert(super.getAssociated)

  override def iterator: TypeIterator = super.iterator
}
