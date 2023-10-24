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
 * The Instance class represents the instance facet of the ESI clabject ([Fragment]).
 * The Instance contains the set of instance-objects forming a physical deep instance of the logically instantiated
 * [Node].
 * @see [Fragment]
 * @see [IObject]
 */
@Entity
@XmlAccessorType(XmlAccessType.NONE)
class Instance(

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
     * The (technical) name of the Instance.
     * The name does not have to be unique, although it should be as identifying as possible.
     */
    @field:Column
    @field:XmlAttribute(name = "name")
    val name: String = "",

    /**
     * The URI of the root [Node] of the Instance ESI.
     * The target Node must exist within the same Fragment and its dependencies must fulfill ESI properties.
     */
    @field:Column
    @field:XmlAttribute(name = "model_root")
    val modelRoot: String = "",

    /**
     * The URI uniquely identifying the Instance as a whole.
     */
    @field:Column
    @field:XmlAttribute(name = "uri")
    val uri: String = "",

    /**
     * The [Header] to store publicly accessible (composite) objects.
     * @see [Composition]
     * @see [CompositionInstance]
     */
    @field:OneToOne(fetch = FetchType.EAGER, cascade = [CascadeType.ALL])
    @field:XmlElement(name = "Header")
    val header: Header? = null,

    /**
     * The set of instance objects to represent the instance type physically.
     * This set is not allowed to be empty (at least the root element must exist as object)
     * The list type is used for traversal sorting reasons, duplicates must not exist.
     */
    @field:OneToMany(fetch = FetchType.EAGER, cascade = [CascadeType.ALL])
    @field:XmlElement(name = "Object")
    private val objects: MutableList<IObject> = LinkedList(),

    /**
     * Backlink for faster traversal.
     */
    @field:Transient
    @field:XmlTransient
    var fragment: Fragment? = null,

    /**
     * Backlink for faster traversal.
     */
    @field:Transient
    @field:XmlTransient
    var rootNode: Node? = null
) {

    constructor() : this(null)

    fun initializeZeroIDs(){
        dataID = 0
        header?.initializeZeroIDs()
        objects.forEach { o -> o.initializeZeroIDs() }
    }

    init {
        autowire()
    }

    fun autowire(){
        header?.instance = this
        rootNode = fragment?.model?.getNodes()?.find { node -> node.uri == modelRoot }
        objects.forEach{obj ->
            obj.instance = this
            obj.autowire()
        }
    }

    fun getObjects(): List<IObject> = objects

    fun addObject(iObject: IObject) {
        if (!objects.contains(iObject)) objects.add(iObject)
    }

    fun removeObject(iObject: IObject) {
        objects.remove(iObject)
    }

    fun updateHeader() {
        val existingHeaderElements = header!!.getElements()
        val currentPublicElements = collectHeaderInstances()

        val retainedHeaderElements: MutableList<HeaderElement> = existingHeaderElements.filter { elem ->
            currentPublicElements.find { pub ->
                elem.compositeInstanceUri == pub.compositeInstanceUri } != null }.toMutableList()

        retainedHeaderElements.addAll(currentPublicElements.filter { pub ->
            retainedHeaderElements.find { elem -> elem.compositeInstanceUri == pub.compositeInstanceUri} == null })

        header.clearElements()
        header.addAll(retainedHeaderElements)
    }

    fun getParentOfObject(iObject: IObject): Node? {
        return fragment!!.model!!.getNodes().find { n -> n.uri == iObject.instanceOf }
    }

    private fun collectHeaderInstances(): Set<HeaderElement> {
        val allPublicElements: MutableSet<HeaderElement> = HashSet()
        allPublicElements.add(HeaderElement(0, rootNode!!.uri, uri))
        allPublicElements.addAll(getObjects().flatMap { obj -> obj.collectHeaderObjects() })
        return allPublicElements
    }

    companion object {

        fun constructIObjects(rootNode: Node, typeNodes: Set<Node>): Set<IObject>{
            val inheritanceClosure: Set<Node> = Node.getInheritanceClosure(rootNode, typeNodes)
            val allConcreteValues: Set<Concretization> = inheritanceClosure.flatMap { n -> n.getConcretizations() }.toSet()
            val iObjects = inheritanceClosure.map { node ->
                val attributeInstances: MutableList<AttributeInstance> =
                    node.getAttributes().filter { att ->
                        allConcreteValues.find { value -> value.attributeInstance!!.attributeUri == att.uri } == null
                    }.map { att ->
                        AttributeInstance(0, att.uri, "")
                    }.toMutableList()
                val associationInstances: MutableList<AssociationInstance> = LinkedList()
                val compositionInstances: MutableList<CompositionInstance> = LinkedList()
                IObject(0, node.uri, attributeInstances, associationInstances, compositionInstances)
            }.toSet()
            return iObjects
        }

    }

}
