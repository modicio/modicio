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
package modicio.core.rules

import scala.collection.mutable

class ConnectionInterface(private val slots: mutable.Buffer[Slot]) {

  def getSlots: Seq[Slot] = slots.toSeq

  def addSlot(slot: Slot): Unit = {
    if(!slots.contains(slot)) {
      slots.addOne(slot)
    }
  }

  def removeSlot(slot: Slot): Slot = slots.remove(slots.indexOf(slot))

  def canConnect(name: String, variantTime: Long): Boolean =
    slots.exists(slot => slot.targetName == name && slot.targetVariantTime == variantTime)

  def contains(slot: Slot): Boolean = slots.contains(slot)

}

object ConnectionInterface {

  def parseInterface(nativeValue: String, targetName: String): ConnectionInterface = {
    val timestamps = nativeValue.split("&").map(_.toLong)
    new ConnectionInterface(mutable.Buffer.from(timestamps.map(t => Slot(targetName, t))))
  }

  def serialise(interface: ConnectionInterface): String = {
    var result = ""
    val maxIdx = interface.slots.size
    interface.slots.toSeq.zipWithIndex.foreach(elem => {
      val (slot, idx) = elem
      result = result.concat(slot.targetVariantTime.toString)
      if(idx < maxIdx-1) {
        result = result.concat("&")
      }
    })
    result
  }

}