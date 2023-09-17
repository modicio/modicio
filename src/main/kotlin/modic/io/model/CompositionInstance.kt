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
class CompositionInstance(
    @Id
    @Column
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var dataID: Long?,
    @Column
    val name: String,
    @Column
    val modelRoot: String,
    @Column
    val uri: String,
    @Column
    val compositionUri: String,
    @OneToMany(cascade = [CascadeType.ALL])
    private val objects: MutableList<IObject>,
    @Transient
    var instance: Instance?
) {

    fun addObject(iObject: IObject) {
        if (!objects.contains(iObject)) objects.add(iObject)
    }

    fun removeObject(iObject: IObject) {
        objects.remove(iObject)
    }

}
