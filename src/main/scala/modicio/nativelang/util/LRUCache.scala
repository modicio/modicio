package modicio.nativelang.util

import java.util.concurrent.TimeUnit
import java.util.concurrent.locks.ReentrantReadWriteLock
import scala.collection.immutable.HashMap
import scala.concurrent.duration.{Duration, DurationLong}

class LRUCache[K, V](size: Int = 10, expire: Duration = Duration(365, TimeUnit.DAYS) ) extends Cache[K, V]{

  private var list: List[(K, V, Long)] = List[(K, V, Long)]()
  private var map: HashMap[K, (K,V, Long)] = HashMap[K, (K, V, Long)]()
  private val lock: ReentrantReadWriteLock = new ReentrantReadWriteLock()

  def set(key: K, value: V): Boolean = {
    lock.writeLock().lock()
    try {
      val item = (key, value, getTime)
      if (map.contains(key)) {
        val node = map(key)
        list = updateAndMoveToFront(node, item)
      } else {
        if (size() >= size) {
          val (_map, _list) = evictElement()
          map = _map
          list = _list
        }
        list = list.appended(item)
        list = updateAndMoveToFront(item, item)
      }
      map = map.updated(key, item)
      true
    } finally {
      lock.writeLock().unlock()
    }
  }
  def get(key: K): Option[V] = {
    lock.readLock().lock()
    try {
      val node = map.get(key)
      if (node.isDefined) {
        val (key, value, time) = node.get
        if (getExpired(time)) {
          evictElement(key)
          return Option.empty
        }
        map = map.updated(key, (key, value, time))
        list = moveToFront((key, value, time))
        return Option(value)
      }
      Option.empty
    } finally {
      lock.readLock().unlock()
    }
  }

  def getAll: (HashMap[K, (K, V, Long)], List[(K, V, Long)]) = {
    lock.readLock().lock()
    try {
      (map, list)
    }
    finally {
      lock.readLock().unlock()
    }
  }

  def size(): Int = {
    list.size
  }

  def isEmpty(): Boolean = {
    list.isEmpty
  }

  def clear(): Unit = {
    list = List[(K, V, Long)]()
    map = HashMap[K, (K, V, Long)]()
  }

  private def moveToFront(node: (K, V, Long)): List[(K, V, Long)] = {
    list.span(_._1 != node._1) match {
      case (as, h :: bs) => h :: as ++ bs
      case _ => list
    }
  }

  private def updateAndMoveToFront(node: (K, V, Long), update: (K, V, Long)): List[(K, V, Long)] = {
    list.span(_._1 != node._1) match {
      case (as, _ :: bs) => update :: as ++ bs
      case _ => update :: list
    }
  }
  private def evictElement(): (HashMap[K, (K, V, Long)], List[(K, V, Long)]) = {
    lock.writeLock().lock()
    try {
      val item = list.last
      (map.removed(item._1), list.init)
    } finally {
      lock.writeLock().unlock()
    }
  }

  private def evictElement(key: K): (HashMap[K, (K, V, Long)], List[(K, V, Long)]) = {
    list.span(_._1 != key) match {
      case (as, _ :: bs) => ( map.removed(key), as ++ bs)
      case _ => (map, list)
    }
  }

  def remove(key: K): Unit = {
    lock.readLock().lock()
    try {
      if (!map.contains(key)) return
    } finally {
      lock.readLock().unlock()
    }

    lock.writeLock().lock()
    try {
      val (_map, _list) = evictElement(key)
      map = _map
      list = _list
    } finally {
      lock.writeLock().unlock()
    }

  }

  private def getTime: Long = {
    System.currentTimeMillis()
  }

  private def getExpired(time: Long): Boolean = {
    System.currentTimeMillis().millis > time.millis + expire
  }
}
