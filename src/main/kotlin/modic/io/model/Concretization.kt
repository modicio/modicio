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

/**
 * The [Concretization] binds an [AttributeInstance] to an [Attribute].
 * Hereby, the Concretization class adds as a proxy to hold the AttributeInstance in the [Model] facet
 * (because AttributeInstances would normally live in the [Instance] facet).
 * From an Instance view, the AttributeInstance contains an immutable value that predefines an Attribute.
 *
 * In consequence, the modicio metamodel becomes multilevel model properties.
 * See gradual concretization in multilevel modelling for more information.
 */
@Entity
@XmlAccessorType(XmlAccessType.NONE)
class Concretization(

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
     * Forward reference to the [AttributeInstance] containing a concrete value.
     * It must be assured that an [Attribute] is only concreted once in the meta hierarchy (parent relation trace).
     * It must also be checked that teh Attribute still exists after a model refactoring. In this case, the AttributeInstance
     * may become invalid.
     */
    @field:OneToOne
    @field:XmlElement(name = "AttributeInstance")
    val attributeInstance: AttributeInstance? = null,

    /**
     * Backlink for faster traversal.
     */
    @field:Transient
    @field:XmlTransient
    var node: Node? = null
) {

    constructor() : this(null)

}
