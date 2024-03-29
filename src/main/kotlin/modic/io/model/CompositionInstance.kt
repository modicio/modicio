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
import java.util.*
import kotlin.jvm.Transient

@Entity
@XmlAccessorType(XmlAccessType.NONE)
class CompositionInstance(

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

    @field:Column
    @field:XmlAttribute(name = "name")
    val name: String = "",

    @field:Column
    @field:XmlAttribute(name = "model_root")
    val modelRoot: String = "",

    @field:Column
    @field:XmlAttribute(name = "uri")
    val uri: String = "",

    @field:Column
    @field:XmlAttribute(name = "composition_uri")
    val compositionUri: String = "",

    @field:OneToMany(fetch = FetchType.EAGER, cascade = [CascadeType.ALL])
    @field:XmlElement(name = "Object")
    private val objects: MutableList<IObject> = LinkedList(),

    @field:Transient
    @field:XmlTransient
    var rootInstance: Instance? = null,

    @field:Transient
    @field:XmlTransient
    var node: Node? = null
) {

    constructor() : this(null)


    fun initializeZeroIDs(){
        dataID = 0
    }

    fun addObject(iObject: IObject) {
        if (!objects.contains(iObject)) objects.add(iObject)
    }

    fun removeObject(iObject: IObject) {
        objects.remove(iObject)
    }

    private fun getCompositionRule(): Composition? {
        return node!!.getCompositions().find { c -> c.uri == compositionUri }
    }

    fun collectHeaderObjects(): Set<HeaderElement> {
        val headerElements: MutableSet<HeaderElement> = HashSet()
        if(!getCompositionRule()!!.isPublic) return headerElements
        headerElements.add(HeaderElement(0, node!!.uri, uri))
        headerElements.addAll(objects.flatMap { o -> o.collectHeaderObjects() })
        return headerElements
    }

}
