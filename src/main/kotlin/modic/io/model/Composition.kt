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
 * A Composition is a stronger specialization of an [AssociationRelation], i.e., it is a part-of relationship.
 * To realize this binding strength, Compositions follow particular principles:
 * - A Composition starts from a [Node] "A" and targets a Node "B". The composition relation is unidirectional and owned
 *   by Node "A". It means "B's" can be part of "A".
 * - In this case, "A" and "B" must be part of the same Model.
 * - Composition loops are forbidden and invalidate the Model.
 * - Compositions can be private or public. The instance objects of a public composite element are visible from outside
 *   the resulting Instance. Consequently, they can become a concrete target for [AssociationRelation]s. If this is not
 *   desired, i.e., the composite element should exclusively exist within its "wrapping" element, then the Composition
 *   must be private. **Note that Nodes in Compositions can have Compositions and so on...**: The privacy acts in this
 *   case like a filter and is parsed starting from the root of an Instance. So the first "private" Compositions makes
 *   all subsequent ones private as well.
 * - Because a Node can occur in different Compositions, the Composition has a [role] that serves as named handle of
 *   the composite instances (just like a relation name).
 *
 * @see [Instance]
 * @see [Model]
 * @see [Fragment]
 * @see [Header]
 * @see [CompositionInstance]
 *
 * @editable
 */
@Entity
@XmlAccessorType(XmlAccessType.NONE)
class Composition(

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
     * The URI uniquely identifying the composition in the scope of its [Model].
     * A modicio URI is defined as a "xs:anyURI" base with the schema extension "modicio:.*"
     * @editable
     */
    @field:Column
    @field:XmlAttribute(name = "uri")
    val uri: String = "",

    /**
     * The role name the composite elements are referred by, i.e., the name of the composition relation.
     * The role must be unique in the scope of the [Node] owning the Composition.
     * @editable
     */
    @field:Column
    @field:XmlAttribute(name = "role")
    val role: String = "",

    /**
     * The URI specifying the composite [Node] element.
     * The target node must be part of the same [Model] as the Node specifying this composition.
     * A modicio URI is defined as a "xs:anyURI" base with the schema extension "modicio:.*"
     * @editable
     */
    @field:Column
    @field:XmlAttribute(name = "target")
    val target: String = "",

    /**
     * Boolean value if the Composition is public or private. See the [Composition] class documentation for more
     * information.
     * Public [Composition] types and their instances are listed in the [Fragment]s [Header].
     * @see [Header]
     * @see [CompositionInstance]
     * @editable
     */
    @field:Column
    @field:XmlAttribute(name = "is_public")
    var isPublic: Boolean = false,

    /**
     * Backlink for faster traversal.
     * **This is not the composite [Node] but the Node owning the Composition.**
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
