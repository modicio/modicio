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

package modic.io.messages

import jakarta.xml.bind.annotation.XmlAccessType
import jakarta.xml.bind.annotation.XmlAccessorType
import jakarta.xml.bind.annotation.XmlAttribute
import jakarta.xml.bind.annotation.XmlRootElement
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter
import modic.io.model.xml.XMLDateTimeAdaptor
import java.sql.Timestamp
import java.time.Instant

@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
data class MetaData(

    @field:XmlJavaTypeAdapter(value = XMLDateTimeAdaptor::class, type = Instant::class)
    @field:XmlAttribute(name = "variantTime")
    val timestamp: Timestamp,

    @field:XmlAttribute(name = "variantID")
    val uuid: String,

    @field:XmlAttribute(name = "variantName")
    val name: String?
)
