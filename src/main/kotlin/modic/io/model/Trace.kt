/**
 * Copyright 2023 Karl Kegel
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

package modic.io.model

import jakarta.persistence.*
import jakarta.xml.bind.annotation.XmlAccessType
import jakarta.xml.bind.annotation.XmlAccessorType
import jakarta.xml.bind.annotation.XmlElement
import jakarta.xml.bind.annotation.XmlTransient
import java.util.*

@Entity
@XmlAccessorType(XmlAccessType.NONE)
class Trace(

    @field:Id
    @field:Column
    @field:GeneratedValue(strategy = GenerationType.IDENTITY)
    @field:XmlTransient
    var dataID: Long? = null,

    @field:OneToMany(cascade = [CascadeType.ALL])
    @field:XmlElement(name = "Delta")
    private val deltas: MutableList<Delta> = LinkedList(),

    @field:XmlTransient
    @field:Transient
    var fragment: Fragment? = null
) {

    constructor() : this(null)

    fun getDeltas(): List<Delta> = deltas

    fun addDelta(delta: Delta) = deltas.add(delta)

    fun clearTrace(): Unit = deltas.clear()

}
