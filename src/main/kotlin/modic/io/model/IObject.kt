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
class IObject(
    @Id
    @Column
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var dataID: Long?,
    @Column
    val instanceOf: String,
    @OneToMany(cascade = [CascadeType.ALL])
    private val attributeInstances: MutableList<AttributeInstance>,
    @OneToMany(cascade = [CascadeType.ALL])
    private val associationInstances: MutableList<AssociationInstance>,
    @OneToMany(cascade = [CascadeType.ALL])
    private val compositionInstances: MutableList<CompositionInstance>,
) {

    fun getAttributeInstances(): List<AttributeInstance> = attributeInstances

    fun getAssociationInstances(): List<AssociationInstance> = associationInstances

    fun addAssociationInstance(associationInstance: AssociationInstance) {
        if (!associationInstances.contains(associationInstance)) associationInstances.add(associationInstance)
    }

    fun removeAssociationInstance(associationInstance: AssociationInstance) {
        associationInstances.remove(associationInstance)
    }

    fun getCompositionInstances(): List<CompositionInstance> = compositionInstances

    fun addCompositionInstance(compositionInstance: CompositionInstance) {
        if (!compositionInstances.contains(compositionInstance)) compositionInstances.add(compositionInstance)
    }

    fun removeCompositionInstance(compositionInstance: CompositionInstance) {
        compositionInstances.remove(compositionInstance)
    }

}
