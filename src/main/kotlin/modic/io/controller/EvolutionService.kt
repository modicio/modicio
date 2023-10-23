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

package modic.io.controller

import jakarta.transaction.Transactional
import modic.io.logic.ModelService
import modic.io.repository.FragmentRepository
import org.springframework.stereotype.Service

@Service
class EvolutionService(
    val modelService: ModelService,
    val fragmentRepository: FragmentRepository) {

    @Transactional
    fun evolveFragment(variantID: String, runningID: String, evolutionRequest: String){

        val fragment = fragmentRepository
        //1. get fragment to evolve

        //2. detach fragment from the entity manager

        //3. do all the request compilation stuff

        //4. apply the result changes to the fragment

        //5. store the fragment with a new runningID and current runningTime

    }

}