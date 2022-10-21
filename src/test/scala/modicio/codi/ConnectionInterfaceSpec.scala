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

package modicio.codi

import modicio.core.rules.ConnectionInterface
import modicio.core.util.IdentityProvider
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should

class ConnectionInterfaceSpec extends AnyFlatSpec with should.Matchers {

  private val time1: Long = IdentityProvider.newTimestampId()
  private val time2: Long = time1 + 60000
  private val targetName = "Todo"

  "A ConnectionInterface" must "be correctly deserialize its serialisation with single slot" in {
    val interface = ConnectionInterface.parseInterface(time1.toString, targetName)
    interface.getSlots.size should be(1)
    val slot = interface.getSlots.head
    slot.targetName should be(targetName)
    slot.targetVariantTime should be(time1)
  }

  "A ConnectionInterface" must "be correctly deserialize its serialisation with two slots" in {
    val interface = ConnectionInterface.parseInterface(time1.toString + "&" + time2.toString, targetName)
    interface.getSlots.size should be(2)
    val slots = interface.getSlots.toArray
    val slot1 = slots(0)
    val slot2 = slots(1)
    slot1.targetVariantTime should equal (time1)
    slot2.targetVariantTime should equal (time2)
  }

  "A ConnectionInterface" must "be correctly serialize with one slot" in {
    val serialisation = time1.toString
    val interface = ConnectionInterface.parseInterface(serialisation, targetName)
    ConnectionInterface.serialise(interface) should be(serialisation)
  }

  "A ConnectionInterface" must "be correctly serialize with two slots" in {
    val serialisation = time1.toString.concat("&").concat(time2.toString)
    val interface = ConnectionInterface.parseInterface(serialisation, targetName)
    ConnectionInterface.serialise(interface) should be(serialisation)
  }

}
