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
import modic.io.model.Fragment
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
        if (evolutionRequest.contains("create class", ignoreCase = true)) {
            val creationStatement = "create class"
            val nameStart = evolutionRequest.indexOf(creationStatement, startIndex = 0, ignoreCase = true) + creationStatement.length
            val nameEnd = evolutionRequest.length - 1
            val nodeName = evolutionRequest.slice(nameStart..nameEnd)
            val newNode = Node(name = nodeName)
            fragment.model!!.addNode(newNode)
        } else if (evolutionRequest.contains("create abstract class", ignoreCase = true)) {
            val creationStatement = "create abstract class"
            val nameStart = evolutionRequest.indexOf(creationStatement, startIndex = 0, ignoreCase = true) + creationStatement.length
            val nameEnd = evolutionRequest.length - 1
            val nodeName = evolutionRequest.slice(nameStart..nameEnd)
            val newNode = Node(name = nodeName, isAbstract = true)
            fragment.model!!.addNode(newNode)
        } else if (evolutionRequest.contains("delete class", ignoreCase = true)) {
            val deleteStatement = "delete class"
            val nameStart = evolutionRequest.indexOf(deleteStatement, startIndex = 0, ignoreCase = true) + deleteStatement.length
            val nameEnd = evolutionRequest.length - 1
            val nodeName = evolutionRequest.slice(nameStart..nameEnd)
            for (node in fragment.model!!.getNodes()) {
                if (node.name.equals(nodeName, ignoreCase = true)) {
                    fragment.model.removeNode(node)
                }
            }
        }
        //4. apply the result changes to the fragment


        //5. store the fragment with a new runningID and current runningTime
        modelService.pushFullModel(fragment, variantID, fragment.variantName ?: "", true)
    }



}