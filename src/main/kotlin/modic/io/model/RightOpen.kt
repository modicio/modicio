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
 * The [RightOpen] class represents an open interval in time over a range of version.
 * The interval targets one particular variant that is inferred by the usage context of the interval.
 * @see Annotation
 * @see AssociationRelation
 * @editable
 */
@Entity
@XmlAccessorType(XmlAccessType.NONE)
class RightOpen(

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
    var dataID: Long?,

    /**
     * Defines the timestamp of the (inclusive) right / future border of the version interval as a UTC instant.
     * Only using a right border time allows arbitrary intervals but may be ambiguous across distributed use-cases.
     * This field is required.
     * @editable
     */
    @field:Column
    @field:XmlJavaTypeAdapter(value = XMLDateTimeAdaptor::class, type = Timestamp::class)
    @field:XmlAttribute(name = "border_version_time")
    val borderVersionTime: Timestamp = Timestamp.from(Instant.MIN),

    /**
     * Binds the [borderVersionTime] to a specific version.
     * The specified version must exist and must be known to the system the interval is interpreted on.
     * This field optional.
     * @editable
     */
    @field:Column
    @field:XmlAttribute(name = "border_version_id")
    val borderVersionID: String? = null
) {

    constructor() : this(null)

    fun initializeZeroIDs(){
        dataID = 0
    }

}