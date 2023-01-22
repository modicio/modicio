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

import java.util.concurrent.CompletableFuture
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.jdk.CollectionConverters._
import scala.jdk.FutureConverters._
import scala.jdk.OptionConverters._
import scala.language.implicitConversions

object J {

  implicit def convert[T](value: java.util.concurrent.CompletableFuture[T]): Future[T] = value.asScala

  implicit def convert[T](value: Future[T]): java.util.concurrent.CompletableFuture[T] = value.asJava.toCompletableFuture

  implicit def future[T](value: java.util.concurrent.CompletableFuture[T]): Future[T] = value.asScala

  implicit def future[T](value: Future[T]): java.util.concurrent.CompletableFuture[T] = value.asJava.toCompletableFuture

  implicit def convert[T](value: Option[T]): java.util.Optional[T] = value.toJava

  implicit def convert[T](value: java.util.Optional[T]): Option[T] = value.toScala

  implicit def convert[T](value: java.util.Set[T]): Set[T] = value.asScala.toSet

  implicit def convert[T](value: Set[T]): java.util.Set[T] = value.asJava

  implicit def convert[T](value: java.util.List[T]): Seq[T] = value.asScala.toSeq

  implicit def convert[T](value: Seq[T]): java.util.List[T] = value.asJava

  implicit def convert[T, K](value: Map[T, K]): java.util.Map[T, K] = value.asJava

  implicit def convert[T, K](value: java.util.Map[T, K]): Map[T, K] = value.asScala.toMap

}