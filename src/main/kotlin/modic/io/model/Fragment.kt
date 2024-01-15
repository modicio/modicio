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
import java.sql.Timestamp
import java.time.Instant
import java.util.*

/**
 * The [Fragment] is the central class (aka root element) of the modicio metamodel.
 * A Fragment is identified by its own variability identifiers [variantTime] and [variantID] (and optional [variantName]).
 * A Fragment can be open or closed. Open refers to an open-world assumption regarding further model elements,
 * i.e., if a Fragment is open, it represents a knowingly or unknowingly incomplete model.
 *
 * The [Fragment] must contain a [Model] and can contain an [Instance] and [Trace].
 *
 * The Model, i.e., the type/class facet contains a complete or incomplete model representation.
 *
 * The Instance, i.e., object/instance facet contains a single instance if defined. This instance
 * is defined as an object net, i.e., a deep-instance regarding a root element in the Model.
 * Per definition, the Model must contain at least all information to interpret the Instance.
 *
 * The Trace contains deltas (change operations) applied to the Model or Instance.
 *
 * A Fragment is always of a specific variant. The variant refers to a "branch" in terms of version control systems.
 * The variant information is represented by the fields [variantName], [variantID] and [variantTime]. Please check the
 * modicio versioning paradigm for further information.
 */
@Entity
@XmlRootElement(name = "Fragment")
@XmlAccessorType(XmlAccessType.NONE)
class Fragment(

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
     * Optional value of the predecessor fragment in variant and/or version (as precise as known) if known
     */
    @field:Column
    @field:XmlAttribute(name = "predecessor_id")
    var predecessorID: String? = null,

    /**
     * A [Fragment] can be open or closed. Open refers to an open-world assumption regarding further model elements,
     * i.e., if a Fragment is open, it represents a knowingly or unknowingly incomplete model.
     * In consequence, a Fragment is most likely open (that's where the name Fragment comes from)
     *
     * A not open [Fragment] is guaranteed to contain a knowingly complete model.
     */
    @field:Column
    @field:XmlAttribute(name = "is_open")
    var open: Boolean = false,

    /**
     * The variantName is an optional arbitrary identifier to name a particular variant.
     * The variantName is not required to be unique (which would be impossible in the distributed case).
     * Therefore, it should not be used to access a variant. The purpose is only for helping the human modeller to
     * distinguish variants otherwise identified by id and timestamp.
     */
    @field:Column
    @field:XmlAttribute(name = "variant_name")
    var variantName: String = "",

    /**
     * The variantTime represents the creation point of the variant as UTC timestamp.
     * This information is stored redundant in [Annotation].
     */
    variantTime: Timestamp = Timestamp.from(Instant.MIN),

    /**
     * The variantID is a unique string identifier of the variant. This implementation uses random-based UUIDs.
     * This guarantees (to a high probability) that each variant in a distributed system is uniquely identified. The
     * pair of variantID and variantTime result in a sortable unique identifier.
     *
     * The variantID can safely be used as an access identifier, also in distributed use-cases.
     * This information is stored redundant in [Annotation].
     */
    variantID: String = "",

    /**
     * The runningTime represents the creation point / update of the running version as UTC timestamp.
     */
    @field:Column
    @field:XmlJavaTypeAdapter(value = XMLDateTimeAdaptor::class, type = Instant::class)
    @field:XmlAttribute(name = "running_time")
    var runningTime: Timestamp = Timestamp.from(Instant.MIN),

    /**
     * The runningID is a unique string identifier of the running version. This implementation uses random-based UUIDs.
     * This guarantees (to a high probability) that each running version in a distributed system is uniquely identified.
     * The pair of runningID and runningTime result in a sortable unique identifier.
     *
     * The runningID can safely be used as an access identifier, also in distributed use-cases.
     */
    @field:Column
    @field:XmlAttribute(name = "running_id")
    var runningID: String = "",

    /**
     * Boolean value denoting if the given [Fragment] is the current reference definition of its variant.
     * Being a reference implicates that the Fragment is not open and has no [Instance] defined.
     * Basically, the reference Fragment represents the whole up-to-date default model which is used for instantiation.
     */
    @field:Column
    @field:XmlAttribute(name = "is_reference")
    var isReference: Boolean = false,

    /**
     * Each [Fragment] must contain a [Model].
     * The optional flag exists only for technical reasons during lazy loading in the persistence layer.
     *
     * @see [Model] for further information
     */
    @field:OneToOne(fetch = FetchType.EAGER, cascade = [CascadeType.ALL])
    @field:XmlElement(name = "Model")
    val model: Model? = null,

    /**
     * A [Fragment] can contain an [Instance].
     * A Fragment with a defined Instance fulfills the definition of an ESI. Refer to the publication for more information
     * TODO paper DOI link
     * The [Instance] data must be fully interpretable by the type definitions in the provided model.
     * The optional flag exists only for technical reasons during lazy loading in the persistence layer.
     *
     * @see [Model] for further information
     */
    @field:OneToOne(fetch = FetchType.EAGER, cascade = [CascadeType.ALL])
    var instance: Instance? = null,

    /**
     * A [Fragment] can contain a [Trace].
     * The Trace contains deltas (change operations) applied to the Model or Instance.
     */
    @field:OneToOne(fetch = FetchType.EAGER, cascade = [CascadeType.ALL])
    @field:XmlElement(name = "Trace")
    val trace: Trace? = null,

    /**
     * A global system independent identifier of a particular fragment
     */
    @field:Column(unique = true)
    @field:XmlAttribute(name = "global_id")
    var globalID: String? = UUID.randomUUID().toString()

) {

    constructor() : this(null)

    /**
     * Required argument to export valid XML
     */
    @field:Transient
    @field:XmlAttribute(name = "xmlns:xsi")
    private val xsi = "http://www.w3.org/2001/XMLSchema-instance"

    /**
     * Required argument to export valid XML
     */
    @field:Transient
    @field:XmlAttribute(name = "xmlns")
    private val xmlns = "http://modic.io/ns"

    /**
     * Required argument to export valid XML
     */
    @field:Transient
    @field:XmlAttribute(name = "xsi:schemaLocation")
    private val location = "http://modic.io/ns modicio_lang.xsd"

    /**
     * Autowire backlinks that are not part of the JPA schema (transient)
     */
    init {
        autowire()
    }

    @field:Column
    @field:XmlAttribute(name = "variant_id")
    var variantID: String = variantID
        set(value) {
            field = value
            model?.getNodes()?.forEach { node -> node.annotation?.variantID = value }
        }

    @field:Column
    //@field:XmlJavaTypeAdapter(value = XMLDateTimeAdaptor::class, type = Instant::class)
    @field:XmlAttribute(name = "variant_time")
    var variantTime: Timestamp = variantTime
        set(value) {
            field = value
            model?.getNodes()?.forEach { node -> node.annotation?.variantTime = value }
        }

    fun initializeZeroIDs() {
        dataID = 0
        instance?.initializeZeroIDs()
        trace?.initializeZeroIDs()
        model?.initializeZeroIDs()
    }

    fun autowire(){
        if (model != null) model.fragment = this
        if (trace != null) trace.fragment = this
        if (instance != null) instance!!.fragment = this
        model?.autowire()
        trace?.autowire()
        instance?.autowire()
    }

    //companion object {
    //FIXME some validation experiments that do not work right now
    /*
    fun validateToXSD(fragment: Fragment): Unit{
        val schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI)
        val schema: Schema = schemaFactory.newSchema(ClassPathResource("modicio_lang.xsd").file)

        val marshaller: Marshaller = JAXBContext.newInstance(Fragment::class.java).createMarshaller()
        marshaller.schema = schema
        marshaller.marshal(fragment, DefaultHandler())
    }
    */
    //}

}