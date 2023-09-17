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
import java.time.Instant

@Entity
@XmlAccessorType(XmlAccessType.NONE)
class Model(

    @field:Id
    @field:Column
    @field:GeneratedValue(strategy = GenerationType.IDENTITY)
    var dataID: Long? = null,

    @field:Column
    @field:XmlJavaTypeAdapter(value = XMLDateTimeAdaptor::class, type = Instant::class)
    @field:XmlAttribute(name = "running_time")
    var runningTime: Instant = Instant.MIN,

    @field:Column
    @field:XmlAttribute(name = "running_id")
    var runningID: String = "",

    @field:OneToMany(cascade = [CascadeType.ALL])
    private val nodes: MutableSet<Node> = HashSet(),

    @field:XmlTransient
    @field:Transient
    var fragment: Fragment? = null
) {

    constructor() : this(null)

    init {
        nodes.forEach { node -> node.model = this }
    }

    fun getNodes(): Set<Node> = nodes

    fun addNode(node: Node): Boolean {
        node.model = this
        return nodes.add(node)
    }

    fun removeNode(node: Node): Boolean = nodes.remove(node)

}
