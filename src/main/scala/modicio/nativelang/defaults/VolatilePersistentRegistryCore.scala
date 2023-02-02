/**
 * Copyright 2022 Johannes Gröschel
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

import modicio.core.datamappings.{AssociationData, AttributeData, InstanceData, ModelElementData, ParentRelationData, RuleData}

import java.util.concurrent.locks.ReentrantReadWriteLock
import scala.collection.mutable.ListBuffer

/**
 * Helper class for [[VolatilePersistentRegistry]]. Includes all required buffers and locks.
 * Can be used to have several VolatilePersistentRegistries use the same buffers and locks.
 * <p> This class is used for testing and benchmarking mainly.
 */
class VolatilePersistentRegistryCore {
  /**
   * To avoid deadlocks, acquire locks in the following order:
   * 1. modelElementDataLock
   * 2. instanceDataLock
   * 3. ruleDataLock
   * 4. attributeDataLock
   * 5. parentRelationDataLock
   * 6. associationDataLock
   */

  var modelElementDataBuffer: ListBuffer[ModelElementData] = new ListBuffer[ModelElementData]()
  val modelElementDataLock: ReentrantReadWriteLock = new ReentrantReadWriteLock()

  var instanceDataBuffer: ListBuffer[InstanceData] = new ListBuffer[InstanceData]()
  val instanceDataLock: ReentrantReadWriteLock = new ReentrantReadWriteLock()

  var ruleDataBuffer: ListBuffer[RuleData] = new ListBuffer[RuleData]()
  val ruleDataLock: ReentrantReadWriteLock = new ReentrantReadWriteLock()

  var attributeDataBuffer: ListBuffer[AttributeData] = new ListBuffer[AttributeData]()
  val attributeDataLock: ReentrantReadWriteLock = new ReentrantReadWriteLock()

  var parentRelationDataBuffer: ListBuffer[ParentRelationData] = new ListBuffer[ParentRelationData]()
  val parentRelationDataLock: ReentrantReadWriteLock = new ReentrantReadWriteLock()

  var associationDataBuffer: ListBuffer[AssociationData] = new ListBuffer[AssociationData]()
  val associationDataLock: ReentrantReadWriteLock = new ReentrantReadWriteLock()
}
