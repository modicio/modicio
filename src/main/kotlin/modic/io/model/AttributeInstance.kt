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
 * The AttributeInstance represents a concretization of an [Attribute].
 * @see [Attribute]
 */
@Entity
@XmlAccessorType(XmlAccessType.NONE)
class AttributeInstance(

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
     * URI of the [Attribute] defining this AttributeInstances.
     */
    @field:Column
    @field:XmlAttribute(name = "attribute_uri")
    val attributeUri: String = "",

    /**
     * Textual value representation of the Attribute.
     */
    @field:Column
    @field:XmlAttribute(name = "any_value")
    var anyValue: String = ""
) {

    constructor() : this(null)

    fun initializeZeroIDs(){
        dataID = 0
    }

}
