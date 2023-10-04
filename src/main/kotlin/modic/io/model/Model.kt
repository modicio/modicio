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
import jakarta.xml.bind.annotation.*
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter
import modic.io.model.xml.XMLDateTimeAdaptor
import java.time.Instant

/**
 * The [Model] is the part of a [Fragment] representing type information.
 * The Model is an object-oriented class structure. Classes are denoted as [Node]s.
 * Nodes can be connected through associations and inheritance. These relations are directed edges and are
 * owned by the starting node. A Node can compose other Nodes (part-of relationship).
 *
 * The Model has a [runningTime] and [runningID] identifiers to store the running version. For more information, check
 * the modicio versioning documentation.
 */
@Entity
@XmlAccessorType(XmlAccessType.NONE)
class Model(

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
     * The runningTime represents the creation point / update of the running version as UTC timestamp.
     */
    @field:Column
    @field:XmlJavaTypeAdapter(value = XMLDateTimeAdaptor::class, type = Instant::class)
    @field:XmlAttribute(name = "running_time")
    var runningTime: Instant = Instant.MIN,

    /**
     * The runningID is a unique string identifier of the running version. This implementation uses random-based UUIDs.
     * This guarantees (to a high probability) that each running version in a distributed system is uniquely identified.
     * The pair of runningID and runningTime result in a sortable unique identifier.
     *
     * The runningID can safely be used as an access identifier, also in distributed use-cases.
     */
    @field:Column
    @field:XmlAttribute(name = "running_id")
    var runningID: String = "",

    /**
     * The set of [Node]s contains all elements of the Model.
     * Practically, the set can be empty.
     */
    @field:OneToMany(fetch = FetchType.EAGER, cascade = [CascadeType.ALL])
    @field:XmlElement(name = "Node")
    private val nodes: MutableSet<Node> = HashSet(),

    /**
     * Backlink to [Fragment] to improve traversal.
     */
    @field:XmlTransient
    @field:Transient
    var fragment: Fragment? = null
) {

    constructor() : this(null)

    /**
     * Autowire backlinks that are not part of the JPA schema (transient)
     */
    init {
        nodes.forEach { node -> node.model = this }
    }

    fun getNodes(): Set<Node> = nodes

    fun addNode(node: Node): Boolean {
        node.model = this
        return nodes.add(node)
    }

    fun removeNode(node: Node): Boolean = nodes.remove(node)

}
