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
import java.time.LocalDateTime

@Entity
class Model(
    @Id
    @Column
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var dataID: Long?,
    @Column
    var runningTime: LocalDateTime,
    @Column
    var runningID: String,
    @ManyToOne(cascade = [CascadeType.ALL])
    private val nodes: MutableSet<Node>,
    @Transient
    var fragment: Fragment?
) {

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
