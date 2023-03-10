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

package util

import modicio.FixtureSpec
import modicio.core.datamappings.AttributeData
import modicio.nativelang.util.AccessCountingListBuffer

import scala.collection.mutable.ListBuffer


class DataIndependenceSpec extends FixtureSpec {
  "Cloning Data" must "result in properly independent objects" in { fixture => {
      val one = new AttributeData(10, "One", "One", "One", true);
      val two = one.copy();
      two.value = "Two";
      one.value should not be (two.value);
    }
  }

  "Cloning a Buffer" must "result in properly independent objects" in { fixutre => {
      val one = new AttributeData(10, "One", "One", "One", true)
      val oneB = new ListBuffer[AttributeData]()
      oneB += one
      val twoB = oneB.clone().map(_data => _data.copy())
      val two = twoB(0)
      two.value = "Two";
      one.value should not be (two.value)
    }
  }

  "Classes" must "be Classes" in { fixture => {
    def supers(cl: Class[_]): List[Class[_]] = {
      if (cl == null) Nil else cl :: supers(cl.getSuperclass)
    }

    def generic[T <: Product](obj: T) = {
    }

    print(supers(classOf[AttributeData]))
    1 should be(1)
  }}

  "Map" must "produce an independent List" in { fixture => {
    val one = new AttributeData(10, "One", "One", "One", true)
    val oneB = new AccessCountingListBuffer[AttributeData]()
    oneB += one
    val twoB = oneB.map(_datum => _datum.copy())
    oneB += new AttributeData(20, "Two", "Two", "Two", true)
    one.value = "Three"

    oneB.size should not be(twoB.size)
    one.value should not be(twoB.getByIndex(0).value)
  }
  }

  "FilterInPlace" must "not write back after clone" in { fixture => {
    val one = new AttributeData(10, "One", "One", "One", true)
    val two = new AttributeData(20, "Two", "Two", "Two", true)
    val oneB = new AccessCountingListBuffer[AttributeData]()
    oneB += one
    oneB += two
    val twoB = oneB.clone()

    twoB.filterInPlace((_datum) => _datum.value == "One")

    oneB.size should not be (twoB.size)
  }
  }
}
