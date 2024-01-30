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
 * The [IObject] represents an instantiated [Node].
 * For each Node there must be exactly one Object per [Fragment].
 * The Object holds all instance information: [AttributeInstance]s, [AssociationInstance]s and
 * [CompositionInstance]s.
 * [ParentRelation]s are not part of the IObject because they are purely represented by the [Node] and can be
 * accessed via the Node reference.
 */
@Entity
@XmlAccessorType(XmlAccessType.NONE)
class IObject(

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
     * URI of the [Node] this is [IObject] is the instance of.
     */
    @field:Column
    @field:XmlAttribute(name = "instance_of")
    val instanceOf: String = "",

    /**
     * The list of [AttributeInstance]s of the [IObject].
     * Each AttributeInstance conforms to exactly one [Attribute] defined in the corresponding [Node].
     * The list representation is used to have a deterministic client experience.
     */
    @field:OneToMany(fetch = FetchType.EAGER, cascade = [CascadeType.ALL])
    @field:XmlElement(name = "AttributeInstance")
    private val attributeInstances: MutableList<AttributeInstance> = LinkedList(),

    /**.
     * The list of [AssociationInstance]s of the [IObject].
     * Each AssociationInstance conforms to exactly one [AssociationRelation] defined in the corresponding [Node].
     * One AssociationRelation can have an arbitrary number of AssociationInstances.
     * This is the flattened set of this relation.
     * The list representation is used to have a deterministic client experience.
     */
    @field:OneToMany(fetch = FetchType.EAGER, cascade = [CascadeType.ALL])
    @field:XmlElement(name = "AssociationInstance")
    private val associationInstances: MutableList<AssociationInstance> = LinkedList(),

    /**
     * The list of [CompositionInstance]s of the [IObject].
     * Each CompositionInstance conforms to exactly one [Composition] defined in the corresponding [Node].
     * One Composition can have an arbitrary number of CompositionInstances.
     * This is the flattened set of this relation.
     * The list representation is used to have a deterministic client experience.
     */
    @field:OneToMany(fetch = FetchType.EAGER, cascade = [CascadeType.ALL])
    @field:XmlElement(name = "CompositionInstance")
    private val compositionInstances: MutableList<CompositionInstance> = LinkedList(),

    /**
     * Backlink for faster traversal
     */
    @field:Transient
    @field:XmlTransient
    var instance: Instance? = null,

    /**
     * Link to the [Node] being the type of this [IObject]
     */
    @field:Transient
    @field:XmlTransient
    var node: Node? = null
) {

    constructor() : this(null)

    fun autowire(){
        node = instance?.fragment?.model?.getNodes()?.find { node -> node.uri == instanceOf }
        compositionInstances.forEach{ c ->
            c.rootInstance = instance
            c.node = node
        }
    }

    fun initializeZeroIDs(){
        dataID = 0
        attributeInstances.forEach { a -> a.initializeZeroIDs() }
        associationInstances.forEach { a -> a.initializeZeroIDs() }
        compositionInstances.forEach { c -> c.initializeZeroIDs() }
    }

    fun addAttributeInstance(newAttributeInstance: AttributeInstance) {
        // Check if an attribute instance with the same URI already exists
        val existingInstance = attributeInstances.find { it.attributeUri == newAttributeInstance.attributeUri }
        if (existingInstance == null) {
            attributeInstances.add(newAttributeInstance)
        }
    }


    fun getAttributeInstances(): List<AttributeInstance> = attributeInstances

    fun getAssociationInstances(): List<AssociationInstance> = associationInstances

    fun addAssociationInstance(associationInstance: AssociationInstance) {
        if (!associationInstances.contains(associationInstance)) associationInstances.add(associationInstance)
    }

    fun removeAssociationInstance(associationInstance: AssociationInstance) {
        associationInstances.remove(associationInstance)
    }

    fun getCompositionInstances(): List<CompositionInstance> = compositionInstances

    fun addCompositionInstance(compositionInstance: CompositionInstance) {
        if (!compositionInstances.contains(compositionInstance)) compositionInstances.add(compositionInstance)
    }

    fun removeCompositionInstance(compositionInstance: CompositionInstance) {
        compositionInstances.remove(compositionInstance)
    }

    fun collectHeaderObjects(): Set<HeaderElement> {
        return compositionInstances.flatMap { c -> c.collectHeaderObjects() }.toSet()
    }

}
