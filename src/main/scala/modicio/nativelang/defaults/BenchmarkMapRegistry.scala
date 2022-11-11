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
package modicio.nativelang.defaults

import modicio.core._

import scala.collection.mutable
import scala.concurrent.Future

class BenchmarkMapRegistry(typeFactory: TypeFactory, instanceFactory: InstanceFactory)
  extends SimpleMapRegistry(typeFactory, instanceFactory) {

  // String -> Int
  private[modicio] val benchmarkCounter = mutable.Map[String, Int]()

  private[modicio] def load(registry: BenchmarkMapRegistry): Unit = {
    val id = "load"
    benchmarkCounter(id) = benchmarkCounter.getOrElse(id, 0) + 1
    super.load(registry)
  }

  override def getReferenceTimeIdentity: Future[TimeIdentity] = {
    val id = "getReferenceTimeIdentity"
    benchmarkCounter(id) = benchmarkCounter.getOrElse(id, 0) + 1
    super.getReferenceTimeIdentity
  }

  override def incrementVariant: Future[Any] = {
    val id = "incrementVariant"
    benchmarkCounter(id) = benchmarkCounter.getOrElse(id, 0) + 1
    super.incrementVariant
  }

  override def incrementRunning: Future[Any] = {
    val id = "incrementRunning"
    benchmarkCounter(id) = benchmarkCounter.getOrElse(id, 0) + 1
    super.incrementRunning
  }

  override def containsRoot: Future[Boolean] = {
    val id = "containsRoot"
    benchmarkCounter(id) = benchmarkCounter.getOrElse(id, 0) + 1
    super.containsRoot
  }

  override def getTypes: Future[Set[String]] = {
    val id = "getTypes"
    benchmarkCounter(id) = benchmarkCounter.getOrElse(id, 0) + 1
    super.getTypes
  }

  override def getInstanceVariants: Future[Seq[(Long, String)]] = {
    val id = "getInstanceVariants"
    benchmarkCounter(id) = benchmarkCounter.getOrElse(id, 0) + 1
    super.getInstanceVariants
  }

  override def getModelVariants: Future[Seq[(Long, String)]] = {
    val id = "getModelVariants"
    benchmarkCounter(id) = benchmarkCounter.getOrElse(id, 0) + 1
    super.getInstanceVariants
  }

  override def getVariantMap: Future[Map[(Long, String), Int]] = {
    val id = "getVariantMap"
    benchmarkCounter(id) = benchmarkCounter.getOrElse(id, 0) + 1
    super.getVariantMap
  }

  override def getType(name: String, identity: String): Future[Option[TypeHandle]] = {
    val id = "getType"
    benchmarkCounter(id) = benchmarkCounter.getOrElse(id, 0) + 1
    super.getType(name, identity)
  }

  override def setType(typeHandle: TypeHandle): Future[Any] = {
    val id = "setType"
    benchmarkCounter(id) = benchmarkCounter.getOrElse(id, 0) + 1
    super.setType(typeHandle)
  }

  override def getSingletonTypes(name: String): Future[Set[TypeHandle]] = {
    val id = "getSingletonTypes"
    benchmarkCounter(id) = benchmarkCounter.getOrElse(id, 0) + 1
    this.getSingletonTypes(name)
  }

  override protected def setNode(typeHandle: TypeHandle): Future[TimeIdentity] = {
    val id = "setNode"
    benchmarkCounter(id) = benchmarkCounter.getOrElse(id, 0) + 1
    super.setNode(typeHandle)
  }

  override def getReferences: Future[Set[TypeHandle]] = {
    val id = "getReferences"
    benchmarkCounter(id) = benchmarkCounter.getOrElse(id, 0) + 1
    super.getReferences
  }

  override def get(instanceId: String): Future[Option[DeepInstance]] = {
    val id = "get"
    benchmarkCounter(id) = benchmarkCounter.getOrElse(id, 0) + 1
    super.get(instanceId)
  }

  override def getAll(typeName: String): Future[Set[DeepInstance]] = {
    val id = "getAll"
    benchmarkCounter(id) = benchmarkCounter.getOrElse(id, 0) + 1
    super.getAll(typeName)
  }

  override def setInstance(deepInstance: DeepInstance): Future[Unit] = {
    val id = "setInstance"
    benchmarkCounter(id) = benchmarkCounter.getOrElse(id, 0) + 1
    super.setInstance(deepInstance)
  }

  /**
   * Remove parts of the model in a way producing a minimal number of overall deletions while trying to retain integrity
   * <p> <strong>Experimental Feature</strong>
   * <p> In case of a reference-identity ModelElement, the ModelElement is deleted only. In consequence, children pointing to that ModelElement
   * and other ModelElements associating this ModelElement become invalid and must be repaired manually.
   * <p> In case of a singleton-identity ModelElement, the whole singleton-fork of the ModelElement tree and the corresponding
   * [[DeepInstance DeepInstance]] tree are removed.
   * <p> In case of a user-space identity, nothing happens yet => TODO
 *
   * @param name     of the [[ModelElement ModelElement]] trying to remove
   * @param identity of the [[ModelElement ModelElement]] trying to remove
   * @return
   */
  override def autoRemove(name: String, identity: String): Future[Any] = {
    val id = "autoRemove"
    benchmarkCounter(id) = benchmarkCounter.getOrElse(id, 0) + 1
    super.autoRemove(name, identity)
  }
}
