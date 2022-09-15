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
package modicio.api

import modicio.codi.{Registry, Rule, TypeHandle}
import modicio.codi.api.{RegistryJ, RuleJ, TypeHandleJ}

import scala.concurrent.Future
import scala.language.implicitConversions
import scala.jdk.OptionConverters._
import scala.jdk.FutureConverters._
import scala.jdk.CollectionConverters._

object JavaAPIConversions {

  implicit def futureToFuture[T](value: java.util.concurrent.CompletableFuture[T]): Future[T] = {
    value.asScala
  }

  implicit def futureToFuture[T](value: Future[T]): java.util.concurrent.CompletableFuture[T] = {
    value.asJava.toCompletableFuture
  }

  implicit def optionToOption[T](value: Option[T]): java.util.Optional[T] = {
    value.toJava
  }

  implicit def optionToOption[T](value: java.util.Optional[T]): Option[T] = {
    value.toScala
  }

  implicit def setToSet[T](value: java.util.Set[T]): Set[T] = {
    value.asScala.toSet
  }

  implicit def setToSet[T](value: Set[T]): java.util.Set[T] = {
    value.asJava
  }

  implicit def typeHandleToTypeHandleJ(value: TypeHandle): TypeHandleJ = new TypeHandleJ(value.getFragment, value.getIsStatic)

  implicit def registryJToRegistry(value: RegistryJ): Registry = value.getRegistry

  implicit def ruleJToRule(value: RuleJ): Rule = value.getRule



}
