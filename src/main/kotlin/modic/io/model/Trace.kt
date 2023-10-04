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

/**
 * The Trace is an optional part of a [Fragment] that contains change operations (deltas) that were performed to the
 * [Fragment] in the past.
 * The usage of the Trace is implementation specific and may be refined in future work.
 * In general, the Trace targets the Model, but could target Instance changes as well. The coarse idea is the following:
 * Each evolution step by the client (e.g. that increments the running version) produces a sequence of [Delta]s which
 * are added to the Trace. This allows going back in time (reverse operations) and helps to (re-)construct model
 * transformations between different variants and versions.
 * If a new Fragment is created from an existing one, the Trace of the new Fragment is emptied.
 *
 * Unlike a delta modes (see Ina Schäfer et al.) does a Trace not have the properties of a partially ordered set. A
 * Traces has the properties of a totally ordered set, i.e., a sequence of change operations.
 */
@Entity
@XmlAccessorType(XmlAccessType.NONE)
class Trace(

    /**
     * Technical database (JPA) identifier used for relation joins.
     * The [dataID] is system specific and not exported to XML.
     * It must not be used to identify elements in distributed use-cases.
     * It should not be used to identify elements from outside the service. All model elements provide other
     * suitable identifiers to be used.
     */
    @field:Id
    @field:Column
    @field:GeneratedValue(strategy = GenerationType.IDENTITY)
    @field:XmlTransient
    var dataID: Long? = null,

    /**
     * The sequence of [Delta]s
     */
    @field:OneToMany(fetch = FetchType.EAGER, cascade = [CascadeType.ALL])
    @field:XmlElement(name = "Delta")
    private val deltas: MutableList<Delta> = LinkedList(),

    /**
     * Backlink for faster traversal
     */
    @field:XmlTransient
    @field:Transient
    var fragment: Fragment? = null
) {

    constructor() : this(null)

    fun getDeltas(): List<Delta> = deltas

    fun addDelta(delta: Delta) = deltas.add(delta)

    fun clearTrace(): Unit = deltas.clear()

}
