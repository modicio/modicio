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
 * The Header contains the information which [IObject]s of an [Instance] are accessible from the outside, i.e., which
 * objects can be targeted by [AssociationRelation]s.
 * **A Header only exists in [Fragment]s carrying an [Instance].**
 * The individual objects are defined as [HeaderElement]s.
 * The [Composition] defines if an object is publicly visible or not (non-composition objects are always public).
 * As soon as a [IObject] is created that is public according to its type, it is added as a [HeaderElement]
 * @see [Composition]
 * @see [CompositionInstance]
 */
@Entity
@XmlAccessorType(XmlAccessType.NONE)
class Header(

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
     * The list of [HeaderElement]s stored in the [Trace].
     */
    @field:OneToMany(fetch = FetchType.EAGER, cascade = [CascadeType.ALL])
    @field:XmlElement(name = "HeaderElement")
    private val elements: MutableList<HeaderElement> = LinkedList(),

    /**
     * Backlink for faster traversal
     */
    @field:Transient
    @field:XmlTransient
    var instance: Instance? = null
) {

    constructor() : this(null)

    init {
        elements.forEach { e -> e.header = this }
    }

    fun initializeZeroIDs(){
        dataID = 0
        elements.forEach{ e -> e.initializeZeroIDs()}
    }

    fun getElements(): List<HeaderElement> = elements

    fun addElement(headerElement: HeaderElement) {
        if (headerElement.header != this && headerElement.header != null) {
            headerElement.dataID = null
            headerElement.header = this
        }
        if (!elements.contains(headerElement)) elements.add(headerElement)
    }

    fun removeElement(headerElement: HeaderElement) {
        elements.remove(headerElement)
    }

}
