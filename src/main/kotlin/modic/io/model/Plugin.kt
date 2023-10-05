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
 * A [Plugin] is a generic [Model] extension part of a [Node].
 * A Plugin consists of a description, content and resolver reference.
 * The content can basically contain any string value providing information, presets, image references etc.
 * Plugins can be executable by the modicio engine directly, if an internal resolver is used. However, the client
 * can use custom resolvers and store required information in the Plugin. The resolver will receive the [Instance]
 * [Fragment] to do any computation based on the Plugin.
 * Note that for scripts / code-based plugins, there is a separate [Script] model element directly supported by the
 * modicio engine.
 */
@Entity
@XmlAccessorType(XmlAccessType.NONE)
class Plugin(

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
     * The human-readable description of the plugin.
     */
    @field:Column
    @field:XmlAttribute(name = "description")
    var description: String = "",

    /**
     * The resolver is defined by a URI specifying a resolvable interpretation algorithm.
     */
    @field:Column
    @field:XmlAttribute(name = "resolver")
    var resolver: String = "",

    /**
     * Arbitrary content of the plugin which is passed to the resolver together with the [Node] / [Instance] it
     * is attached to.
     */
    @field:Column
    @field:XmlAttribute(name = "content")
    var content: String = "",

    /**
     * Backlink to [Node] to improve traversal.
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
