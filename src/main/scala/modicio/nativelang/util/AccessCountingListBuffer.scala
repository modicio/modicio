package modicio.nativelang.util

import scala.collection.mutable.ListBuffer


class AccessCountingListBuffer[T] {
  private val internalBuffer = new ListBuffer[T]
  private var readCount = 0
  private var writeCount = 0

  def getReadCount: Int = readCount
  def getWriteCount: Int = writeCount

  @`inline` def +=(elem: T): AccessCountingListBuffer[T] = {
    writeCount += 1
    internalBuffer += elem
    this
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
  }

  def zipWithIndex: ListBuffer[(T, Int)] = {
    readCount += 1
    internalBuffer.zipWithIndex
  }

  def filterInPlace(p: T => Boolean): AccessCountingListBuffer[T] = {
    readCount += 1
    internalBuffer.filterInPlace(p)
    this
  }

  def map[B](f: T => B): ListBuffer[B] = {
    readCount += 1
    internalBuffer.map(f)
  }

  def groupMapReduce[K, B](key: T => K)(f: T => B)(reduce: (B, B) => B): Map[K, B] = {
    readCount += 1
    internalBuffer.groupMapReduce(key)(f)(reduce)
  }
}
