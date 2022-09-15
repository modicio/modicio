package modicio.api

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

}
