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

package modic.io.logic

import jakarta.persistence.EntityManager
import jakarta.persistence.PersistenceContext
import jakarta.transaction.Transactional
import modic.io.model.*
import modic.io.repository.FragmentRepository
import org.springframework.stereotype.Service
import java.sql.Timestamp
import java.util.*

@Service
class EvolutionService(
    val modelService: ModelService,
    val fragmentRepository: FragmentRepository) {

    @PersistenceContext
    private val entityManager: EntityManager? = null

    @Transactional
    fun evolveFragment(variantID: String, runningID: String, evolutionRequest: String, backwards: Boolean = false){

        //1. get fragment to evolve
        val fragment = fragmentRepository.findModelOnlyFragmentWithVariantAndRunningIDFirstLazy(variantID, runningID)!!

        //2. detach fragment from the entity manager
        entityManager!!.detach(fragment)

        //3. do all the request compilation stuff

        //get the Evolution String ready for parsing
        val requestList = evolutionRequest.replace("&", "").split(",", ignoreCase = true)

        val forwardEvolution: MutableList<String> = LinkedList()
        val backwardsEvolution: MutableList<String> = LinkedList()

        for (statement in requestList) {
            if (statement.contains("/")) {
                val statements = statement.split("/", ignoreCase = true)
                forwardEvolution.add(statements.elementAt(0))
                backwardsEvolution.add(statements.elementAt(1))
            } else {
                forwardEvolution.add(statement)
            }
        }

        //check which request should be evolved - forwards or backwards
        val evolutionList: List<String> = if (backwards) {
            backwardsEvolution.reversed()
        } else {
            forwardEvolution.toList()
        }

        var selectedNode: Node? = null
        var selectedAttribute: Attribute? = null
        var selectedAssociation: AssociationRelation? = null
        var selectedComposition: Composition? = null

        for (request in evolutionList) {
            if (request.contains("CREATE CLASS")) {
                val nodeName = retrieveName(request, "CREATE CLASS")
                val nodeUri = "modicio:$nodeName"
                val newNode = Node(name = nodeName, uri = nodeUri, model = fragment.model)
                fragment.model!!.addNode(newNode)
                continue
            }
            if (request.contains("CREATE ABSTRACT CLASS")) {
                val nodeName = retrieveName(request, "CREATE ABSTRACT CLASS")
                val nodeUri = "modicio:$nodeName"
                val newNode = Node(name = nodeName, uri = nodeUri, isAbstract = true, model = fragment.model)
                fragment.model!!.addNode(newNode)
                continue
            }
            if (request.contains("DELETE CLASS")) {
                val nodeName = retrieveName(request, "DELETE CLASS")
                var foundClass = false
                lateinit var nodeToDelete: Node
                for (node in fragment.model!!.getNodes()) {
                    if (node.name.equals(nodeName, ignoreCase = true)) {
                        foundClass = true
                        nodeToDelete = node
                    }
                }
                if (!foundClass) {
                    throw Exception("Class does not exist!")
                } else {
                    fragment.model.removeNode(nodeToDelete)
                    selectedNode = null
                    continue
                }
            }
            if (request.contains("OPEN CLASS")) {
                val nodeName = retrieveName(request, "OPEN CLASS")
                var foundClass = false
                for (node in fragment.model!!.getNodes()) {
                    if (node.name.equals(nodeName, ignoreCase = true)) {
                        selectedNode = node
                        foundClass = true
                    }
                }
                if (!foundClass) {
                    throw Exception("Class does not exist!")
                } else {
                    continue
                }
            }
            if (request.contains("DELETE ATTRIBUTE")) {
                val attributeName = retrieveName(request, "DELETE ATTRIBUTE")
                var foundAttribute = false
                lateinit var attributeToDelete: Attribute
                lateinit var concretToDelete: Concretization
                for (attribute in selectedNode!!.getAttributes()) {
                    if (attribute.name.equals(attributeName, ignoreCase = true)) {
                        attributeToDelete = attribute
                        foundAttribute = true
                        for (concretization in selectedNode.getConcretizations()) {
                            if (concretization.attributeInstance?.attributeUri.equals(attribute.uri)) {
                                concretToDelete = concretization
                            }
                        }
                    }
                }
                if (!foundAttribute) {
                    throw Exception("Attribute does not exist!")
                } else {
                    selectedNode.removeConcretization(concretToDelete)
                    selectedNode.removeAttribute(attributeToDelete)
                    selectedAttribute = null
                    continue
                }
            }
            if (request.contains("ADD ATTRIBUTE")) {
                val attributeName = retrieveName(request, "ADD ATTRIBUTE")
                val attributeUri = "modicio:$attributeName"
                selectedNode!!.addAttribute(Attribute(name = attributeName, uri = attributeUri, node = selectedNode))
                continue
            }
            if (request.contains("OPEN ATTRIBUTE")) {
                val attributeName = retrieveName(request, "OPEN ATTRIBUTE")
                var foundAttribute = false
                for (attribute in selectedNode!!.getAttributes()) {
                    if (attribute.name.equals(attributeName, ignoreCase = true)) {
                        selectedAttribute = attribute
                        foundAttribute = true
                    }
                }
                if (!foundAttribute) {
                    throw Exception("Attribute does not exist!")
                } else {
                    continue
                }
            }
            if (request.contains("SET TYPE")) {
                val typeName = retrieveName(request, "SET TYPE")
                when (typeName) {
                    "WORD" -> selectedAttribute!!.dType = "Date"
                    "PHRASE" -> selectedAttribute!!.dType = "String"
                    "NUMBER" -> selectedAttribute!!.dType = "Integer"
                    else -> selectedAttribute!!.dType = "Default"
                }
                continue
            }
            if (request.contains("ADD ASSOCIATION")) {
                val associationName = retrieveNameFromComplexCommand(request, "ADD ASSOCIATION", "TARGET")
                val targetClassName = retrieveName(request, "ADD ASSOCIATION $associationName TARGET")
                val targetUri = "modicio:$targetClassName"
                var foundTarget = false
                for (node in fragment.model!!.getNodes()) {
                    if (node.uri.equals(targetUri, ignoreCase = true)) {
                        foundTarget = true
                    }
                }
                if (foundTarget) {
                    val associationUri = "modicio:$associationName"
                    selectedNode!!.addAssociationRelation(AssociationRelation(uri = associationUri, name = associationName, target = targetUri,
                        cInterface = Interface(), node = selectedNode))
                    continue
                } else {
                    throw Exception("No target class with this name found!")
                }
            }
            if (request.contains("DELETE ASSOCIATION")) {
                val associationName = retrieveName(request, "DELETE ASSOCIATION")
                var foundAssociation = false
                lateinit var assocToDelete: AssociationRelation
                for (association in selectedNode!!.getAssociationRelations()) {
                    if (association.name.equals(associationName, ignoreCase = true)) {
                        assocToDelete = association
                        foundAssociation = true
                    }
                }
                if (!foundAssociation) {
                    throw Exception("Association does not exist!")
                } else {
                    selectedNode.removeAssociationRelation(assocToDelete)
                    selectedAssociation = null
                    continue
                }
            }
            if (request.contains("OPEN ASSOCIATION")) {
                val associationName = retrieveName(request, "OPEN ASSOCIATION")
                var foundAssociation = false
                for (association in selectedNode!!.getAssociationRelations()) {
                    if (association.name.equals(associationName, ignoreCase = true)) {
                        selectedAssociation = association
                        foundAssociation = true
                    }
                }
                if (!foundAssociation) {
                    throw Exception("Association does not exist!")
                } else {
                    continue
                }
            }
            if (request.contains("SET COMPATIBLE WITH VERSION")) {
                val versionDate = retrieveName(request, "SET COMPATIBLE WITH VERSION")
                val timestampDate = versionDate + "T12:00:00"
                selectedAssociation!!.cInterface?.addPointDelimiter(Point(versionTime = Timestamp.valueOf(timestampDate)))
            }
            if (request.contains("SET VERSION RANGE FROM")) {
                val rangeStart = retrieveNameFromComplexCommand(request, "SET VERSION RANGE FROM", "TO")
                val rangeEnd = retrieveName(request, "SET VERSION RANGE FROM $rangeStart TO")
                val startDate = rangeStart + "T12:00:00"
                val endDate = rangeEnd + "T12:00:00"
                selectedAssociation!!.cInterface?.addIntervalDelimiter(Region(leftBorderVersionTime = Timestamp.valueOf(startDate),
                    rightBorderVersionTime = Timestamp.valueOf(endDate)))
            }
            if (request.contains("SET COMPATIBLE WITH ALL VERSIONS OF VARIANT")) {
                val variantDate = retrieveName(request, "SET COMPATIBLE WITH ALL VERSIONS OF VARIANT")
                val timestampVariant = variantDate + "T12:00:00"
                selectedAssociation!!.cInterface?.addPointDelimiter(Point(variantTime = Timestamp.valueOf(timestampVariant)))
            }
            if (request.contains("SET VERSION UP TO DATE")) {
                val leftBorder = retrieveName(request, "SET VERSION UP TO DATE")
                val leftBorderDate = leftBorder + "T12:00:00"
                selectedAssociation!!.cInterface?.addOLeftOpenDelimiter(LeftOpen(borderVersionTime = Timestamp.valueOf(leftBorderDate)))

            }
            if (request.contains("SET VERSION STARTING FROM DATE")) {
                val rightBorder = retrieveName(request, "SET VERSION STARTING FROM DATE")
                val rightBorderDate = rightBorder + "T12:00:00"
                selectedAssociation!!.cInterface?.addRightOpenDelimiter(RightOpen(borderVersionTime = Timestamp.valueOf(rightBorderDate)))
            }
            if (request.contains("ADD PARENT_RELATION")) {
                val targetClass = retrieveName(request, "ADD PARENT_RELATION")
                val inheritanceUri = "modicio:$targetClass"
                var foundTarget = false
                for (node in fragment.model!!.getNodes()) {
                    if (node.name.equals(targetClass, ignoreCase = true)) {
                        foundTarget = true
                    }
                }
                if (foundTarget) {
                    selectedNode!!.addParentRelation(ParentRelation(uri = inheritanceUri, node = selectedNode))
                    continue
                } else {
                    throw Exception("No target class with this name found!")
                }
            }
            if (request.contains("DELETE PARENT_RELATION")) {
                val targetClass = retrieveName(request, "DELETE PARENT_RELATION")
                val inheritanceUri = "modicio:$targetClass"
                var foundRelation = false
                lateinit var relationToDelete: ParentRelation
                for (relation in selectedNode!!.getParentRelations()) {
                    if (relation.uri.equals(inheritanceUri, ignoreCase = true)) {
                        relationToDelete = relation
                        foundRelation = true
                    }
                }
                if (!foundRelation) {
                    throw Exception("Parent relation does not exist!")
                } else {
                    selectedNode.removeParentRelation(relationToDelete)
                    continue
                }
            }
            if (request.contains("DELETE COMPOSITION")) {
                val compositionName = retrieveName(request, "DELETE COMPOSITION")
                val compositionUri = "modicio:$compositionName"
                var foundComposition = false
                lateinit var composToDelete: Composition
                for (composition in selectedNode!!.getCompositions()) {
                    if (composition.uri.equals(compositionUri, ignoreCase = true)) {
                        composToDelete = composition
                        foundComposition = true
                    }
                }
                if (!foundComposition) {
                    throw Exception("Composition does not exist!")
                } else {
                    selectedNode.removeComposition(composToDelete)
                    selectedComposition = null
                    continue
                }
            }
            if (request.contains("ADD COMPOSITION")) {
                val compositionName = retrieveNameFromComplexCommand(request, "ADD COMPOSITION", "TARGET")
                val targetName = retrieveName(request, "ADD COMPOSITION")
                val compositionUri = "modicio:$compositionName"
                val targetUri = "modicio:$targetName"
                var foundTarget = false
                for (node in fragment.model!!.getNodes()) {
                    if (node.uri.equals(targetUri, ignoreCase = true)) {
                        foundTarget = true
                    }
                }
                if (foundTarget) {
                    selectedComposition = Composition(role = compositionName, uri = compositionUri, target = targetUri, node = selectedNode)
                    selectedNode!!.addComposition(selectedComposition)
                    continue
                } else {
                    throw Exception("No target class with this name found!")
                }
            }
            if (request.contains("MAKE COMPOSITION")) {
                selectedComposition!!.isPublic = request.contains("PUBLIC")
                continue
            }
            if (request.contains("OPEN COMPOSITION")) {
                val compositionName = retrieveName(request, "OPEN COMPOSITION")
                var foundComposition = false
                for (composition in selectedNode!!.getCompositions()) {
                    if (composition.role.equals(compositionName, ignoreCase = true)) {
                        selectedComposition = composition
                        foundComposition = true
                    }
                }
                if (!foundComposition) {
                    throw Exception("Composition does not exist!")
                } else {
                    continue
                }
            }
            if (request.contains("CHANGE ATTRIBUTE NAME")) {
                val newName = retrieveName(request, "CHANGE ATTRIBUTE NAME TO")
                selectedAttribute!!.name = newName
                continue
            }
            if (request.contains("CHANGE ATTRIBUTE URI")) {
                val newUri = retrieveName(request, "CHANGE ATTRIBUTE URI TO")
                selectedAttribute!!.uri = newUri
                continue
            }
            if (request.contains("SET ATTRIBUTE VALUE")) {
                val value = retrieveName(request, "SET ATTRIBUTE VALUE TO")
                val attributeInstance = AttributeInstance(attributeUri = selectedAttribute!!.uri, anyValue = value)
                lateinit var concretToDelete: Concretization
                for (concretization in selectedNode!!.getConcretizations()) {
                    if (concretization.attributeInstance?.attributeUri.equals(selectedAttribute.uri)) {
                        concretToDelete = concretization
                    }
                }
                selectedNode.removeConcretization(concretToDelete)
                selectedNode.addConcretization(Concretization(attributeInstance = attributeInstance, node = selectedNode))
                continue
            }
        }


        //4. store the fragment with a new runningID and current runningTime
        fragment.globalID = UUID.randomUUID().toString()
        modelService.pushFullModel(fragment, variantID, fragment.variantName ?: "", true)
    }

    private fun retrieveName(requestFull: String, requestCommand: String): String {
        val nameStartIndex = requestCommand.length + 1
        val nameEndIndex = requestFull.length - 1
        return requestFull.slice(nameStartIndex..nameEndIndex)
    }

    private fun retrieveNameFromComplexCommand(requestFull: String, requestCommand: String, secondPart: String): String {
        val nameStartIndex = requestCommand.length + 1
        val nameEndIndex = requestFull.indexOf(secondPart) - 1
        return requestFull.slice(nameStartIndex..nameEndIndex)
    }



}