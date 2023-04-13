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

package util

import modicio.nativelang.util.AccessCountingListBuffer
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should

class AccessCountingBufferSpec extends AnyFlatSpec with should.Matchers {

  "Adding one Element" must "increase write counter by one" in {
    val buffer = new AccessCountingListBuffer[Int]()
    buffer += 1
    buffer.getWriteCount should be(1)
  }

  "Adding one Element" must "add the element correctly" in {
    val buffer = new AccessCountingListBuffer[Int]()
    buffer += 1
    buffer.getByIndex(0) should be(1)
    buffer.size should be(1)
  }

  "Removing one Element" must "increase write counter by one" in {
    val buffer = new AccessCountingListBuffer[Int]()
    buffer += 1
    buffer -= 1
    buffer.getWriteCount should be(2)
  }

  "Removing one Element" must "remove the element correctly" in {
    val buffer = new AccessCountingListBuffer[Int]()
    buffer += 1
    buffer -= 1
    buffer.size should be(0)
  }

  "getByIndex" must "return right element" in {
    val buffer = new AccessCountingListBuffer[Int]()
    buffer += 1
    buffer += 5
    buffer.getByIndex(0) should be(1)
    buffer.getByIndex(1) should be(5)
  }

  "find" must "return right element" in {
    val buffer = new AccessCountingListBuffer[Int]()
    buffer += 1
    buffer += 2
    buffer += 3
    buffer += 4
    buffer.find(value => value == 4).get should be(4)
  }

  "filter" must "return right elements" in {
    val buffer = new AccessCountingListBuffer[Int]()
    buffer += 1
    buffer += 2
    buffer += 3
    buffer += 4
    val new_list = buffer.filter(values => values > 2)
    new_list.head should be(3)
    new_list(1) should be(4)
    new_list.size should be(2)
  }

  "update" must "update right element" in {
    val buffer = new AccessCountingListBuffer[Int]()
    buffer += 1
    buffer += 2
    buffer.update(1, 5)
    buffer.getByIndex(0) should be(1)
    buffer.getByIndex(1) should be(5)
  }

  "remove" must "remove the right element" in {
    val buffer = new AccessCountingListBuffer[Int]()
    buffer += 1
    buffer += 2
    val item = buffer.remove(0)
    item should be(1)
    buffer.getByIndex(0) should be(2)
  }

  "clone" must "create a correct copy of the buffer" in {
    val buffer = new AccessCountingListBuffer[Int]()
    buffer += 1
    val clone = buffer.clone()
    clone.size should be(1)
    clone.find(_item => _item == 1).isDefined should be(true)
  }

  "toSet" must "return a set containing all the items of the original list" in {
    val buffer = new AccessCountingListBuffer[Int]()
    buffer += 1
    val set = buffer.toSet()
    set.find(_item => _item == 1).isDefined should be(true)
    set.size should be(1)
  }
}
