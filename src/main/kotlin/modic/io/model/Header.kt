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

@Entity
class Header(

    /**
     * Technical database (JPA) identifier used for relation joins.
     * The [dataID] is system specific and not exported to XML.
     * It must not be used to identify elements in distributed use-cases.
     * It should not be used to identify elements from outside the service. All model elements provide other
     * suitable identifiers to be used.
     */
    @Id
    @Column
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var dataID: Long?,
    @OneToMany(cascade = [CascadeType.ALL])
    private val elements: MutableList<HeaderElement>,
    @Transient
    var instance: Instance?
) {

    init {
        elements.forEach { e -> e.header = this }
    }

    fun getElements(): List<HeaderElement> = elements

    fun addElement(headerElement: HeaderElement) {
        if (headerElement.header != this && headerElement.header != null) {
            headerElement.dataID = null
            headerElement.header = this
        }
        if (!elements.contains(headerElement)) elements.add(headerElement)
    }

    fun removeElement(headerElement: HeaderElement) {
        elements.remove(headerElement)
    }

}
