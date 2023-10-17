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
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter
import modic.io.model.xml.XMLDateTimeAdaptor
import java.sql.Timestamp
import java.time.Instant

/**
 * The [Point] class represents a closed interval in space-time over a single variant (point in space)
 * or a single point in space-time by adding a single version (that is optional).
 * The interval targets one particular variant that is inferred by the usage context of the interval.
 * @see Annotation
 * @see AssociationRelation
 */
@Entity
@XmlAccessorType(XmlAccessType.NONE)
class Point(

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
     * Defines the timestamp of the single variant (point in space, range in time) as a UTC instant.
     * This field is required.
     */
    @field:Column
    //@field:XmlJavaTypeAdapter(value = XMLDateTimeAdaptor::class, type = Instant::class)
    @field:XmlAttribute(name = "variant_time")
    val variantTime: Timestamp = Timestamp.from(Instant.MIN),

    /**
     * Binds the [variantTime] to a specific version ID.
     * This field optional but its usage is strongly proposed due to the ambiguity of a variant timestamp alone.
     */
    @field:Column
    @field:XmlAttribute(name = "variant_id")
    val variantID: String? = null,

    /**
     * Defines the timestamp of the single version (point in time) as a UTC instant.
     * This field is optional.
     * Using a version time collapses the range to one single point in space-time, i.e., a single variant+version pair.
     */
    @field:Column
    @field:XmlJavaTypeAdapter(value = XMLDateTimeAdaptor::class, type = Instant::class)
    @field:XmlAttribute(name = "version_time")
    val versionTime: Instant? = null,

    /**
     * Binds the [versionTime] to a specific version ID to resolve unambiguity,
     * This field optional.
     */
    @field:Column
    @field:XmlAttribute(name = "version_id")
    val versionID: String? = null
) {

    constructor() : this(null)

    fun initializeZeroIDs(){
        dataID = 0
    }

}
