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
class Node(
    @Id
    @Column
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private var dataID: Long?,
    @Column
    val name: String,
    @Column
    val uri: String,
    @Column
    private var isAbstract: Boolean,
    @OneToOne(cascade = [CascadeType.ALL])
    val annotation: Annotation,
    @OneToMany(cascade = [CascadeType.ALL])
    private val attributes: MutableList<Attribute>,
    @OneToMany(cascade = [CascadeType.ALL])
    private val associationRelations: MutableList<AssociationRelation>,
    @OneToMany(cascade = [CascadeType.ALL])
    private val parentRelations: MutableSet<ParentRelation>,
    @OneToMany(cascade = [CascadeType.ALL])
    private val plugins: MutableSet<Plugin>,
    @OneToMany(cascade = [CascadeType.ALL])
    private val concretizations: MutableSet<Concretization>,
    @OneToMany(cascade = [CascadeType.ALL])
    private val compositions: MutableSet<Composition>,
    @OneToMany(cascade = [CascadeType.ALL])
    private val scripts: MutableList<Script>,
    @Transient
    var model: Model?
) {

    init {
        attributes.forEach { e -> e.node = this }
        associationRelations.forEach { e -> e.node = this }
        parentRelations.forEach { e -> e.node = this }
        plugins.forEach { e -> e.node = this }
        concretizations.forEach { e -> e.node = this }
        compositions.forEach { e -> e.node = this }
        scripts.forEach { e -> e.node = this }
    }

    fun getIsAbstract(): Boolean = isAbstract

    fun getAttributes(): List<Attribute> = attributes

    fun addAttribute(attribute: Attribute) {
        attribute.node = this
        attributes.add(attribute)
    }

    fun removeAttribute(attribute: Attribute) = attributes.remove(attribute)

    fun getAssociationRelations(): List<AssociationRelation> = associationRelations

    fun addAssociationRelation(associationRelation: AssociationRelation) {
        associationRelation.node = this
        associationRelations.add(associationRelation)
    }

    fun removeAssociationRelation(associationRelation: AssociationRelation) =
        associationRelations.remove(associationRelation)

    fun getParentRelations(): Set<ParentRelation> = parentRelations

    fun addParentRelation(parentRelation: ParentRelation): Boolean {
        parentRelation.node = this
        return parentRelations.add(parentRelation)
    }

    fun removeParentRelation(parentRelation: ParentRelation): Boolean = parentRelations.remove(parentRelation)

    fun getPlugins(): Set<Plugin> = plugins

    fun addPlugin(plugin: Plugin): Boolean {
        plugin.node = this
        return plugins.add(plugin)
    }

    fun removePlugin(plugin: Plugin): Boolean = plugins.remove(plugin)

    fun getConcretizations(): Set<Concretization> = concretizations

    fun addConcretization(concretization: Concretization): Boolean {
        concretization.node = this
        return concretizations.add(concretization)
    }

    fun removeConcretization(concretization: Concretization): Boolean = concretizations.remove(concretization)

    fun getCompositions(): Set<Composition> = compositions

    fun addComposition(composition: Composition) {
        composition.node = this
        compositions.add(composition)
    }

    fun removeComposition(composition: Composition) = compositions.remove(composition)

    fun getScripts(): List<Script> = scripts

    fun addScript(script: Script) {
        script.node = this
        scripts.add(script)
    }

    fun removeScript(script: Script) = scripts.remove(script)


}
