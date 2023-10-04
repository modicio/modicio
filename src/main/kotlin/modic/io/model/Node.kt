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
import java.util.*

/**
 * The Node is the top-level model element of the modicio metamodel. It represents a class in an object-oriented sense
 * but can also represent a Node in a typed graph.
 * The Node as first-level element contains all further model elements such as relations to other Nodes which
 * are always unidirectional edges.
 */
@Entity
@XmlAccessorType(XmlAccessType.NONE)
class Node(

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
    @XmlTransient
    private var dataID: Long? = null,

    /**
     * The name provides the [Node] a practical identifier.
     * Uniqueness is optional but desired.
     * The name should relate to the [uri] and is typically its last part.
     */
    @field:Column
    @field:XmlAttribute(name = "name")
    val name: String = "",

    /**
     * The unique naming URI of the [Node] in its current [Model].
     * The uri must not take variant/version into account which is stored separately.
     * A modicio URI is defined as a "xs:anyURI" base with the schema extension "modicio:.*"
     */
    @field:Column
    @field:XmlAttribute(name = "uri")
    val uri: String = "",

    /**
     * If the [Node] is abstract, it cannot be used as the root of an [Instance].
     * In other words, it is abstract in the sense of object-oriented programming.
     */
    @field:Column
    @field:XmlAttribute(name = "is_abstract")
    private var isAbstract: Boolean = false,

    /**
     * The [Annotation] contains the variant and version identifiers.
     * The Annotation is mandatory.
     */
    @field:OneToOne(fetch = FetchType.EAGER, cascade = [CascadeType.ALL])
    @field:XmlElement(name = "Annotation")
    val annotation: Annotation? = null,

    /**
     * The list of [Attribute]s define the attributes/properties of the [Node] model.
     * Attributes must be unique (by name and uri) per Node.
     * The list must fulfill set properties. Ordering is important for interpretation and rendering.
     */
    @field:OneToMany(fetch = FetchType.EAGER, cascade = [CascadeType.ALL])
    @field:XmlElement(name = "Attribute")
    private val attributes: MutableList<Attribute> = LinkedList(),

    /**
     * The list of [AssociationRelation]s define directed association relations to other [Node]s.
     * Attributes must be unique (by name and uri) per Node.
     * The list must fulfill set properties. Ordering is important for interpretation and rendering.
     */
    @field:OneToMany(fetch = FetchType.EAGER, cascade = [CascadeType.ALL])
    @field:XmlElement(name = "AssociationRelation")
    private val associationRelations: MutableList<AssociationRelation> = LinkedList(),

    /**
     * The list of [ParentRelation]s define directed inheritance relations to other [Node]s.
     * Attributes must be unique (by name and uri) per Node.
     */
    @field:OneToMany(fetch = FetchType.EAGER, cascade = [CascadeType.ALL])
    @field:XmlElement(name = "ParentRelation")
    private val parentRelations: MutableSet<ParentRelation> = HashSet(),

    /**
     * [Plugin]s can be used to extend the [Node] with custom functionality.
     */
    @field:OneToMany(fetch = FetchType.EAGER, cascade = [CascadeType.ALL])
    @field:XmlElement(name = "Plugin")
    private val plugins: MutableSet<Plugin> = HashSet(),

    /**
     * [Concretization]s can be used to materialize an [Attribute] of a related parent, i.e. assigning it a
     * constant value for all children of this [Node].
     * This required:
     * - The specified Attribute must exist in the scope of this model element (match by uri)
     * - The provided value must match the Attribute type
     */
    @field:OneToMany(fetch = FetchType.EAGER, cascade = [CascadeType.ALL])
    @field:XmlElement(name = "Concretization")
    private val concretizations: MutableSet<Concretization> = HashSet(),

    /**
     * [Composition]s can be used to represent "part-of" relationships.
     * The [Node] a Composition refers to must exist in the scope of the Model.
     * Note the special scoping and visibility mechanisms explained in [Composition] and [Header].
     */
    @field:OneToMany(fetch = FetchType.EAGER, cascade = [CascadeType.ALL])
    @field:XmlElement(name = "Composition")
    private val compositions: MutableSet<Composition> = HashSet(),

    /**
     * [Script]s can be used to attach dynamic behaviour to a [Node].
     * Note further details and restrictions explained in [Script].
     */
    @field:OneToMany(fetch = FetchType.EAGER, cascade = [CascadeType.ALL])
    @field:XmlElement(name = "Script")
    private val scripts: MutableList<Script> = LinkedList(),

    /**
     * Backlink to [Model] to improve traversal.
     */
    @field:Transient
    @field:XmlTransient
    var model: Model? = null
) {

    constructor() : this(null)

    /**
     * Autowire backlinks that are not part of the JPA schema (transient)
     */
    init {
        attributes.forEach { e -> e.node = this }
        associationRelations.forEach { e -> e.node = this }
        parentRelations.forEach { e -> e.node = this }
        plugins.forEach { e -> e.node = this }
        concretizations.forEach { e -> e.node = this }
        compositions.forEach { e -> e.node = this }
        scripts.forEach { e -> e.node = this }
    }

    fun getIsAbstract(): Boolean = isAbstract

    fun getAttributes(): List<Attribute> = attributes

    fun addAttribute(attribute: Attribute) {
        attribute.node = this
        attributes.add(attribute)
    }

    fun removeAttribute(attribute: Attribute) = attributes.remove(attribute)

    fun getAssociationRelations(): List<AssociationRelation> = associationRelations

    fun addAssociationRelation(associationRelation: AssociationRelation) {
        associationRelation.node = this
        associationRelations.add(associationRelation)
    }

    fun removeAssociationRelation(associationRelation: AssociationRelation) =
        associationRelations.remove(associationRelation)

    fun getParentRelations(): Set<ParentRelation> = parentRelations

    fun addParentRelation(parentRelation: ParentRelation): Boolean {
        parentRelation.node = this
        return parentRelations.add(parentRelation)
    }

    fun removeParentRelation(parentRelation: ParentRelation): Boolean = parentRelations.remove(parentRelation)

    fun getPlugins(): Set<Plugin> = plugins

    fun addPlugin(plugin: Plugin): Boolean {
        plugin.node = this
        return plugins.add(plugin)
    }

    fun removePlugin(plugin: Plugin): Boolean = plugins.remove(plugin)

    fun getConcretizations(): Set<Concretization> = concretizations

    fun addConcretization(concretization: Concretization): Boolean {
        concretization.node = this
        return concretizations.add(concretization)
    }

    fun removeConcretization(concretization: Concretization): Boolean = concretizations.remove(concretization)

    fun getCompositions(): Set<Composition> = compositions

    fun addComposition(composition: Composition) {
        composition.node = this
        compositions.add(composition)
    }

    fun removeComposition(composition: Composition) = compositions.remove(composition)

    fun getScripts(): List<Script> = scripts

    fun addScript(script: Script) {
        script.node = this
        scripts.add(script)
    }

    fun removeScript(script: Script) = scripts.remove(script)


}
