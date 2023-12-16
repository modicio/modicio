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
import modic.io.model.Attribute
import modic.io.model.Node
import modic.io.repository.FragmentRepository
import org.springframework.stereotype.Service

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
        when (true) {
            evolutionRequest.contains("create class", ignoreCase = true) -> {
                val nodeName = retrieveName("create class", 0, evolutionRequest)
                val nodeUri = "modicio:$nodeName"
                val newNode = Node(name = nodeName, uri = nodeUri)
                fragment.model!!.addNode(newNode)
            }
            evolutionRequest.contains("create abstract class", ignoreCase = true) -> {
                val nodeName = retrieveName("create abstract class", 0, evolutionRequest)
                val nodeUri = "modicio:$nodeName"
                val newNode = Node(name = nodeName, uri = nodeUri, isAbstract = true)
                fragment.model!!.addNode(newNode)
            }
            evolutionRequest.contains("delete class", ignoreCase = true) -> {
                val nodeName = retrieveName("delete class", 0, evolutionRequest)
                for (node in fragment.model!!.getNodes()) {
                    if (node.name.equals(nodeName, ignoreCase = true)) {
                        fragment.model.removeNode(node)
                    } else {
                        throw Exception("No class with such name!")
                    }
                }
            }
            evolutionRequest.contains("open class", ignoreCase = true) -> {
                val nodeName = retrieveName("open class", 0, evolutionRequest)
                lateinit var selectedNode: Node
                for (node in fragment.model!!.getNodes()) {
                    if (node.name.equals(nodeName, ignoreCase = true)) {
                        selectedNode = node
                    } else {
                        throw Exception("No class with such name!")
                    }
                }
                if (evolutionRequest.contains("delete attribute", ignoreCase = true)) {
                    val attributeName = retrieveName("delete attribute", 10, evolutionRequest)
                    for (attribute in selectedNode.getAttributes()) {
                        if (attribute.name.equals(attributeName, ignoreCase = true)) {
                            selectedNode.removeAttribute(attribute)
                        } else {
                            throw Exception("No attribute with such name!")
                        }
                    }
                }
                if (evolutionRequest.contains("add attribute", ignoreCase = true)) {
                    val attributeName = retrieveName("add attribute", 10, evolutionRequest)
                    val attributeUri = "modicio:$attributeName"
                    selectedNode.addAttribute(Attribute(name = attributeName, uri = attributeUri, node = selectedNode))

                }
                if (evolutionRequest.contains("open attribute", ignoreCase = true)) {
                    val attributeName = retrieveName("open attribute", 10, evolutionRequest)
                    lateinit var selectedAttribute: Attribute
                    for (attribute in selectedNode.getAttributes()) {
                        if (attribute.name.equals(attributeName, ignoreCase = true)) {
                            selectedAttribute = attribute
                        } else {
                            throw Exception("No attribute with such name!")
                        }
                    }
                    if (evolutionRequest.contains("set type", ignoreCase = true)) {
                        val typeName = retrieveName("set type", 25, evolutionRequest)
                        when (typeName) {
                            "WORD" -> selectedAttribute.dType = "Date"
                            "PHRASE" -> selectedAttribute.dType = "String"
                            "NUMBER" -> selectedAttribute.dType = "Integer"
                            else -> selectedAttribute.dType = "Default"
                        }
                    }
                }
            }
            else -> throw Exception("No match for request found.")
        }
        //4. apply the result changes to the fragment


        //5. store the fragment with a new runningID and current runningTime
        modelService.pushFullModel(fragment, variantID, fragment.variantName ?: "", true)
    }

    private fun retrieveName(statement: String, startAt: Int, evolutionRequest: String): String {
        val nameStartIndex =
            evolutionRequest.indexOf(statement, startIndex = startAt, ignoreCase = true) + statement.length
        val nameEndIndex = evolutionRequest.indexOf(",", startIndex = nameStartIndex) - 1
        return evolutionRequest.slice(nameStartIndex..nameEndIndex)
    }



}