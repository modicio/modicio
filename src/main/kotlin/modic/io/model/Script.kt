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
 * A script is a special kind of [Plugin] to hold executable code.
 * This code is directly executed by the modicio engine and can be specified by the modeller.
 *
 * **As of right now, Scripts are a planned concept and no particular realization is considered yet.**
 */
@Entity
@XmlAccessorType(XmlAccessType.NONE)
class Script(

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
     * The URI uniquely identifying the Script in the scope of its [Model].
     * A modicio URI is defined as a "xs:anyURI" base with the schema extension "modicio:.*"
     */
    @field:Column
    @field:XmlAttribute(name = "uri")
    var uri: String = "",

    /**
     * The name of the Script to be identifiable by a human client.
     * The name should be unique in the scope of the owning [Node].
     */
    @field:Column
    @field:XmlAttribute(name = "name")
    var name: String = "",

    /**
     * The action type is a further specification of the action behaviour.
     * Details have to be specified during further implementation.
     */
    @field:Column
    @field:XmlAttribute(name = "action_type")
    var actionType: String = "",

    /**
     * The resolver is defined by a URI specifying a resolvable interpretation algorithm.
     * For the default modicio DSL, a valid modicio URI pointing to the desired language version must be used.
     * A modicio URI is defined as a "xs:anyURI" base with the schema extension "modicio:.*"
     */
    @field:Column
    @field:XmlAttribute(name = "resolver")
    var resolver: String = "",

    /**
     * The script's content which is interpreted by the resolver.
     */
    @field:Column
    @field:XmlAttribute(name = "any_value")
    var anyValue: String = "",

    /**
     * Backlink for faster traversal.
     */
    @field:Transient
    @field:XmlTransient
    var node: Node? = null
) {

    constructor() : this(null)

}
