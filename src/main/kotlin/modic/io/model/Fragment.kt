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
import jakarta.xml.bind.annotation.*
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter
import modic.io.model.xml.XMLDateTimeAdaptor
import java.time.Instant


@Entity
@XmlRootElement(name = "Fragment")
@XmlAccessorType(XmlAccessType.NONE)
class Fragment(

    @field:Id
    @field:Column
    @field:GeneratedValue(strategy = GenerationType.IDENTITY)
    @field:XmlTransient
    var dataID: Long? = null,

    @field:Column
    @field:XmlAttribute(name = "is_open")
    val open: Boolean = false,

    @field:Column
    @field:XmlJavaTypeAdapter(value = XMLDateTimeAdaptor::class, type = Instant::class)
    @field:XmlAttribute(name = "variant_time")
    var variantTime: Instant = Instant.MIN,

    @field:Column
    @field:XmlAttribute(name = "variant_id")
    var variantID: String = "",

    @field:Column
    @field:XmlAttribute(name = "is_reference")
    var isReference: Boolean = false,

    @field:OneToOne(cascade = [CascadeType.ALL])
    @field:XmlElement(name = "Model")
    val model: Model? = null,

    @field:OneToOne(cascade = [CascadeType.ALL])
    val instance: Instance? = null,

    @field:OneToOne(cascade = [CascadeType.ALL])
    @field:XmlElement(name = "Trace")
    val trace: Trace? = null
) {

    constructor() : this(null)

    @field:XmlAttribute(name = "xmlns:xsi")
    private val xsi = "http://www.w3.org/2001/XMLSchema-instance"

    @field:XmlAttribute(name = "xmlns")
    private val xmlns = "http://modic.io/ns"

    @field:XmlAttribute(name = "xsi:schemaLocation")
    private val location = "http://modic.io/ns modicio_lang.xsd"

    init {
        if (model != null) model.fragment = this
        if (trace != null) trace.fragment = this
        if (instance != null) instance.fragment = this
    }

    companion object {

        /*
        fun validateToXSD(fragment: Fragment): Unit{
            val schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI)
            val schema: Schema = schemaFactory.newSchema(ClassPathResource("modicio_lang.xsd").file)

            val marshaller: Marshaller = JAXBContext.newInstance(Fragment::class.java).createMarshaller()
            marshaller.schema = schema
            marshaller.marshal(fragment, DefaultHandler())
        }
        */

    }

}