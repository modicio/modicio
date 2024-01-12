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
        val evolutionList = evolutionRequest.split(",", ignoreCase = true)
        lateinit var selectedNode: Node
        lateinit var selectedAttribute: Attribute
        lateinit var selectedAssociation: AssociationRelation
        lateinit var selectedComposition: Composition

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
                for (node in fragment.model!!.getNodes()) {
                    if (node.name.equals(nodeName, ignoreCase = true)) {
                        foundClass = true
                        fragment.model.removeNode(node)
                    }
                }
                if (!foundClass) {
                    throw Exception("Class does not exist!")
                } else {
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
                for (attribute in selectedNode.getAttributes()) {
                    if (attribute.name.equals(attributeName, ignoreCase = true)) {
                        selectedNode.removeAttribute(attribute)
                        foundAttribute = true
                    }
                }
                if (!foundAttribute) {
                    throw Exception("Attribute does not exist!")
                } else {
                    continue
                }
            }
            if (request.contains("ADD ATTRIBUTE")) {
                val attributeName = retrieveName(request, "ADD ATTRIBUTE")
                val attributeUri = "modicio:$attributeName"
                selectedNode.addAttribute(Attribute(name = attributeName, uri = attributeUri, node = selectedNode))
                continue
            }
            if (request.contains("OPEN ATTRIBUTE")) {
                val attributeName = retrieveName(request, "OPEN ATTRIBUTE")
                var foundAttribute = false
                for (attribute in selectedNode.getAttributes()) {
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
                    "WORD" -> selectedAttribute.dType = "Date"
                    "PHRASE" -> selectedAttribute.dType = "String"
                    "NUMBER" -> selectedAttribute.dType = "Integer"
                    else -> selectedAttribute.dType = "Default"
                }
                continue
            }
            if (request.contains("ADD ASSOCIATION")) {
                val associationName = retrieveNameWithTarget(request, "ADD ASSOCIATION")
                val targetClassName = retrieveName(request, "ADD ASSOCIATION $associationName TARGET")
                val associationUri = "modicio:$associationName"
                selectedNode.addAssociationRelation(AssociationRelation(uri = associationUri, name = associationName, target = targetClassName,
                                                                        cInterface = Interface(), node = selectedNode))
                continue
            }
            if (request.contains("DELETE ASSOCIATION")) {
                val associationName = retrieveName(request, "DELETE ASSOCIATION");
                var foundAssociation = false
                for (association in selectedNode.getAssociationRelations()) {
                    if (association.name.equals(associationName, ignoreCase = true)) {
                        selectedNode.removeAssociationRelation(association)
                        foundAssociation = true
                    }
                }
                if (!foundAssociation) {
                    throw Exception("Association does not exist!")
                } else {
                    continue
                }
            }
            if (request.contains("OPEN ASSOCIATION")) {
                val associationName = retrieveName(request, "OPEN ASSOCIATION");
                var foundAssociation = false
                for (association in selectedNode.getAssociationRelations()) {
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

    private fun retrieveNameWithTarget(requestFull: String, requestCommand: String): String {
        val nameStartIndex = requestCommand.length + 1
        val nameEndIndex = requestFull.indexOf("TARGET") - 1
        return requestFull.slice(nameStartIndex..nameEndIndex)
    }



}