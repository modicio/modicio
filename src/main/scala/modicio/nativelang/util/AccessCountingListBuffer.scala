/**
 * Copyright 2023 Tom Felber
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

package modicio.nativelang.util

import scala.collection.mutable.ListBuffer


class AccessCountingListBuffer[T](
                                   private val internalBuffer: ListBuffer[T] = new ListBuffer[T],
                                   private var readCount: Int = 0,
                                   private var writeCount: Int = 0,
                                   private var memSize: Long = 0,
                                 ) {

  def getReadCount: Int = readCount
  def getWriteCount: Int = writeCount
  def getMemSize: Long = memSize

  private def updateMemSize(): Unit = {
    val runtime = Runtime.getRuntime
    memSize = memSize.max(runtime.totalMemory() - runtime.freeMemory())
  }

  @`inline` def +=(elem: T): AccessCountingListBuffer[T] = {
    writeCount += 1
    internalBuffer += elem
    updateMemSize()
    this
  }

  @`inline` def -=(elem: T): AccessCountingListBuffer[T] = {
    writeCount += 1
    internalBuffer -= elem
    updateMemSize()
    this
  }

  def size: Int = internalBuffer.size

  def getByIndex(index: Int): T = {
    readCount += 1
    internalBuffer(index)
  }

  def find(p: T => Boolean): Option[T] = {
    readCount += 1
    internalBuffer.find(p)
  }

  def filter(pred: T => Boolean): ListBuffer[T] = {
    readCount += 1
    internalBuffer.filter(pred)
  }

  def update(idx: Int, elem: T): Unit = {
    writeCount += 1
    internalBuffer.update(idx, elem)
    updateMemSize()
  }

  def zipWithIndex: ListBuffer[(T, Int)] = {
    readCount += 1
    internalBuffer.zipWithIndex
  }

  def filterInPlace(p: T => Boolean): AccessCountingListBuffer[T] = {
    readCount += 1
    writeCount += 1
    internalBuffer.filterInPlace(p)
    updateMemSize()
    this
  }

  def map[B](f: T => B): AccessCountingListBuffer[B] = {
    readCount += 1
    new AccessCountingListBuffer(internalBuffer.map(f), readCount, writeCount)
  }

  def groupMapReduce[K, B](key: T => K)(f: T => B)(reduce: (B, B) => B): Map[K, B] = {
    readCount += 1
    internalBuffer.groupMapReduce(key)(f)(reduce)
  }

  def remove(idx: Int): T = {
    writeCount += 1
    val result = internalBuffer.remove(idx)
    updateMemSize()
    result
  }

  override def clone(): AccessCountingListBuffer[T] = {
    readCount += 1
    new AccessCountingListBuffer[T](internalBuffer.clone(), readCount, writeCount)
  }

  def toSet(): Set[T] = {
    readCount += 1
    internalBuffer.toSet
  }

  def toListBuffer(): ListBuffer[T] = {
    internalBuffer
  }
}
