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