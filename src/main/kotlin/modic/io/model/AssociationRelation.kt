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

/**
 * The [AssociationRelation] models a directed association edge from one [Node] to another Node.
 * The association is always owned by the starting Node.
 * - Modicio Associations have no multiplicity
 * - The target existence is an open property, i.e., it is only evaluated at runtime if an association is realizable
 * - Associations are variability-aware. This is defined via the [Interface]
 */
@Entity
@XmlAccessorType(XmlAccessType.NONE)
class AssociationRelation(

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
     * The unique naming URI of the [AssociationRelation] in its current [Model].
     * The uri must not take variant/version into account which is stored separately.
     * A modicio URI is defined as a "xs:anyURI" base with the schema extension "modicio:.*"
     */
    @field:Column
    @field:XmlAttribute(name = "uri")
    var uri: String = "",

    /**
     * The name of the [AssociationRelation].
     * Uniqueness is optional but should be assured in a common [Node]
     */
    @field:Column
    @field:XmlAttribute(name = "name")
    var name: String = "",

    /**
     * The target [Node] that is the other end of instances of this relation.
     * - The target does not know that it can be associated (unidirectional)
     * - **The target must conform to a Node URI**
     * - The existence of the target is only checked at runtime (during instantiation attempt)
     */
    @field:Column
    @field:XmlAttribute(name = "target")
    var target: String = "",

    /**
     * The [Interface] (refers to the ConnectionInterface ESI concept).
     * The Interface specifies a set of (open) intervals and points in time-space an allowed target must be part of
     * to fulfill this association.
     * An AssociationRelation must have an Interface.
     */
    @field:OneToOne(cascade = [CascadeType.ALL])
    @field:XmlElement(name = "Interface")
    val cInterface: Interface? = null,

    /**
     * Backlink to [Node] to improve traversal.
     * This is not the target [Node]! But the owner of this element.
     */
    @field:Transient
    @field:XmlTransient
    var node: Node? = null
) {

    constructor() : this(null)

    /**
     * Autowire backlinks that are not part of the JPA schema (transient)
     */
    init {
        cInterface?.associationRelation = this
    }

}
