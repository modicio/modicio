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
 * The [Attribute] represents the model element typically known as attribute or property.
 * By default, only [Node]s can have Attributes.
 * An Attribute consists of identifiers and a key-datatype pair expressing the physical attribute model.
 * @editable
 */
@Entity
@XmlAccessorType(XmlAccessType.NONE)
class Attribute(

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
     * The unique naming URI of the [Attribute] in its current [Model].
     * The uri must not take variant/version into account which is stored separately.
     * A modicio URI is defined as a "xs:anyURI" base with the schema extension "modicio:.*"
     * @editable
     */
    @field:Column
    @field:XmlAttribute(name = "uri")
    var uri: String = "",

    /**
     * The name of an Attribute, also called key.
     * Uniqueness is optional but should be enforced on the level of a single [Node].
     * @editable
     */
    @field:Column
    @field:XmlAttribute(name = "name")
    var name: String = "",

    /**
     * The data type of the attribute serialized as string.
     * TODO: right now, arbitrary types are supported. A later type checker must predefine a set of type values.
     * @editable
     */
    @field:Column
    @field:XmlAttribute(name = "dType")
    var dType: String = "",

    /**
     * Backlink to [Model] to improve traversal.
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
