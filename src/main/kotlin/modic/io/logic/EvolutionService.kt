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
                val creationStatement = "create class"
                val nameStartIndex = evolutionRequest.indexOf(creationStatement, startIndex = 0, ignoreCase = true) + creationStatement.length
                val nameEndIndex = evolutionRequest.length - 1
                val nodeName = evolutionRequest.slice(nameStartIndex..nameEndIndex)
                val newNode = Node(name = nodeName)
                fragment.model!!.addNode(newNode)
            }
            evolutionRequest.contains("create abstract class", ignoreCase = true) -> {
                val creationStatement = "create abstract class"
                val nameStartIndex = evolutionRequest.indexOf(creationStatement, startIndex = 0, ignoreCase = true) + creationStatement.length
                val nameEndIndex = evolutionRequest.length - 1
                val nodeName = evolutionRequest.slice(nameStartIndex..nameEndIndex)
                val newNode = Node(name = nodeName, isAbstract = true)
                fragment.model!!.addNode(newNode)
            }
            evolutionRequest.contains("delete class", ignoreCase = true) -> {
                val deleteStatement = "delete class"
                val nameStartIndex = evolutionRequest.indexOf(deleteStatement, startIndex = 0, ignoreCase = true) + deleteStatement.length
                val nameEndIndex = evolutionRequest.length - 1
                val nodeName = evolutionRequest.slice(nameStartIndex..nameEndIndex)
                for (node in fragment.model!!.getNodes()) {
                    if (node.name.equals(nodeName, ignoreCase = true)) {
                        fragment.model.removeNode(node)
                    } else {
                        throw Exception("No class with such name!")
                    }
                }
            }
            evolutionRequest.contains("open class", ignoreCase = true) -> {
                val classStatement = "open class"
                val nameStartIndex = evolutionRequest.indexOf(classStatement, startIndex = 0, ignoreCase = true) + classStatement.length
                // TO  DO
                if (evolutionRequest.contains("add attribute", ignoreCase = true)) {
                    val attributeStatement = "add attribute"
                    val nameStartIndex = evolutionRequest.indexOf(attributeStatement, startIndex = classStatement.length, ignoreCase = true) + attributeStatement.length
                    val nameEndIndex = evolutionRequest.indexOf("open attribute", startIndex = classStatement.length, ignoreCase = true) - 1
                    val attributeName = evolutionRequest.slice(nameStartIndex..nameEndIndex)
                    // TO DO
                }
            }
            else -> throw Exception("No match for request found.")
        }
        //4. apply the result changes to the fragment


        //5. store the fragment with a new runningID and current runningTime
        modelService.pushFullModel(fragment, variantID, fragment.variantName ?: "", true)
    }



}