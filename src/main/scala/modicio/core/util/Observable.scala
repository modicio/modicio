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
package modicio.core.util

import scala.collection.mutable.ListBuffer

trait Observable {

  protected val observers: ListBuffer[Observer] = ListBuffer()

  protected def notifyObservers(): Unit = observers.foreach(_.onChange())

  def registerObserver(observer: Observer): Unit = observers.addOne(observer)

  def unregisterObserver(observer: Observer): Unit = observers.remove(observers.indexOf(observer))

}
