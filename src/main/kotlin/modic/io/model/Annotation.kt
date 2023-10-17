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
 * The [Annotation] model element is attached to every [Node].
 * An Annotation stores the identifiers in space (variant) and time (version) of each element.
 * - the variant is like a branch and is updated for the whole connected model (or slice of it)
 * - the version is updated per model element if the element itself or one of its managed children is edited.
 */
@Entity
@XmlAccessorType(XmlAccessType.NONE)
class Annotation(

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
    @XmlTransient
    var dataID: Long? = null,

    /**
     * The versionTime represents the creation point of the version as UTC timestamp.
     * Together with [versionID] it forms the version (time) identifier per model element.
     */
    @field:Column
    //@field:XmlJavaTypeAdapter(value = XMLDateTimeAdaptor::class, type = Instant::class)
    @field:XmlAttribute(name = "version_time")
    var versionTime: Timestamp = Timestamp.from(Instant.MIN),

    /**
     * The versionID is a unique string identifier of the version. This implementation uses random-based UUIDs.
     * This guarantees (to a high probability) that each version in a distributed system is uniquely identified. The
     * pair of versionID and [versionTime] result in a sortable unique identifier.
     * The versionID can safely be used as an access identifier, also in distributed use-cases.
     */
    @field:Column
    @field:XmlAttribute(name = "version_id")
    var versionID: String = "",

    /**
     * The variantTime represents the creation point of the variant as UTC timestamp.
     * This information is stored redundant in [Fragment]. However, the information stored here are the primary source
     * of truth.
     */
    @field:Column
    @field:XmlJavaTypeAdapter(value = XMLDateTimeAdaptor::class, type = Instant::class)
    @field:XmlAttribute(name = "variant_time")
    var variantTime: Timestamp = Timestamp.from(Instant.MIN),

    /**
     * The variantID is a unique string identifier of the variant. This implementation uses random-based UUIDs.
     * This guarantees (to a high probability) that each variant in a distributed system is uniquely identified. The
     * pair of variantID and variantTime result in a sortable unique identifier.
     *
     * The variantID can safely be used as an access identifier, also in distributed use-cases.
     * This information is stored redundant in [Fragment]. However, the information stored here are the primary source
     */
    @field:Column
    @field:XmlAttribute(name = "variant_id")
    var variantID: String = ""
) {

    constructor() : this(null)

    fun initializeZeroIDs(){
        dataID = 0
    }

}