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
import jakarta.xml.bind.annotation.XmlAttribute
import jakarta.xml.bind.annotation.XmlTransient

/**
 * The [ParentRelation] models inheritance between two [Node]s. This relation is a unidirectional edge and is owned
 * by the child Node, i.e., the specialization.
 * - The parent link is an open property, i.e., it is only evaluated at runtime if resolution is possible. Resolution
 *   must be possible during instantiation.
 * - Parents must be from the same [Fragment].
 * @editable
 */
@Entity
@XmlAccessorType(XmlAccessType.NONE)
class ParentRelation(

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
     * The URI defines the target parent [Node] to inherit from by its URI.
     * This reference is resolved during edit, load and store. Otherwise, the model becomes (temporarily) invalid.
     * A modicio URI is defined as a "xs:anyURI" base with the schema extension "modicio:.*"
     * @editable
     */
    @field:Column
    @field:XmlAttribute(name = "uri")
    val uri: String = "",

    /**
     * Autowired link that is not part of the JPA schema (transient).
     * This is not the parent [Node]! But the owner of this element.
     */
    @field:Transient
    @field:XmlTransient
    var node: Node? = null
) {

    constructor() : this(null)

    fun initializeZeroIDs(){
        dataID = 0
    }

}
