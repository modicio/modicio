package util

import modicio.nativelang.util.LRUCache
import org.scalatest.AppendedClues.convertToClueful
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should

class LRUCacheSpec extends AnyFlatSpec with should.Matchers{
  "Adding an element" must "succeed" in {
    val cache = new LRUCache[String, Int]()
    val response = cache.set("one", 1)
    val result = cache.get("one")
    response should be(true)
    result.get should be(1)
  }

  "Adding more elements than the size" must "result in dropped elements" in {
    val cache = new LRUCache[String, Int]()
    for (number <- Range(0, 11)) {
      cache.set(number.toString, number)
    }
    cache.size() should be(10) withClue cache.size()
    for (number <- Range(1, 11)) {
      val result = cache.get(number.toString)
      noException should be thrownBy result.get withClue "\nCould not be retrieved: " + number.toString + "\nElements: " + cache.getAll
      result.get should be(number)
    }
    val (map, list) = cache.getAll
    map.contains("0") should be(false)
    list.contains(0) should be(false)
  }

  "Setting existing element" must "override it with new value" in {
    val cache = new LRUCache[String, Int]()
    cache.set("1", 1)
    cache.get("1").get should be(1)
    cache.set("1", 2)
    cache.get("1").get should be(2)
    cache.size() should be(1)
  }
}
