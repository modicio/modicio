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

/**
 * The [Model] is the part of a [Fragment] representing type information.
 * The Model is an object-oriented class structure. Classes are denoted as [Node]s.
 * Nodes can be connected through associations and inheritance. These relations are directed edges and are
 * owned by the starting node. A Node can compose other Nodes (part-of relationship).
 * @editable
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
     * The set of [Node]s contains all elements of the Model.
     * Practically, the set can be empty.
     * @editable
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
        autowire()
    }

    fun autowire(){
        nodes.forEach { node ->
            node.model = this
            node.autowire()
        }
    }

    fun initializeZeroIDs(){
        dataID = 0
        nodes.forEach { n -> n.initializeZeroIDs() }
    }

    fun getNodes(): Set<Node> = nodes

    fun addNode(node: Node): Boolean {
        node.model = this
        return nodes.add(node)
    }

    fun removeNode(node: Node): Boolean = nodes.remove(node)

    /**
     * Create a slice of the [Node] set.
     * The slice includes the inheritance hierarchy of the root as well as compositions and their inheritance hierarchies
     * recursively.
     * This method is cycle-safe.
     *
     * @param rootNodeURI URI string of the model root.
     * @throws Exception if the URI is not found
     */
    fun sliceDeep(rootNodeURI: String): Set<Node> {
        val results = HashSet<Node>()
        val root = nodes.find { n -> n.uri == rootNodeURI } ?: throw Exception("No root node found or inconsistent model")
        results.add(root)
        root.getParentRelations().forEach { p ->
            if(results.find { r -> r.uri == p.uri } == null) results.addAll(sliceDeep(p.uri))
        }
        root.getCompositions().forEach { p ->
            if(results.find { r -> r.uri == p.target } == null) results.addAll(sliceDeep(p.target))
        }
        return results
    }

    /**
     * Find the [Script] with the provided URI in the set of [Node]s.
     *
     * @param scriptURI URI of the [Script]
     */
    fun findScript(scriptURI: String): Script?{
        return nodes.flatMap { n -> n.getScripts() }.find { s -> s.uri == scriptURI }
    }

}
