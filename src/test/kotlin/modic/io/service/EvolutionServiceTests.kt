package modic.io.service

import modic.io.TestDataHelper
import modic.io.logic.EvolutionService
import modic.io.logic.ModelService
import modic.io.model.Node
import modic.io.repository.FragmentRepository
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.annotation.DirtiesContext

@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class EvolutionServiceTests {

        @Autowired
        lateinit var modelService: ModelService

        @Autowired
        lateinit var evolutionService: EvolutionService

        @Autowired
        lateinit var fragmentRepository: FragmentRepository

        @Test
        fun createClassWithAttribute() {
            val fragment = TestDataHelper.getSimpleFragmentOnlyModel()
            fragmentRepository.save(fragment)
            val evolutionRequest = "CREATE CLASS TestClass,OPEN CLASS TestClass,ADD ATTRIBUTE testAttribute," +
                    "OPEN ATTRIBUTE testAttribute,SET TYPE NUMBER,CLOSE ATTRIBUTE testAttribute,CLOSE CLASS TestClass"
            evolutionService.evolveFragment(fragment.variantID, fragment.runningID, evolutionRequest)
            val result = fragmentRepository.findMostRecentFragmentsByVariantNameLazy(fragment.variantName, 3)
            Assertions.assertEquals(1, result.size)

            val evolvedFragment = result.first()
            val nodes = evolvedFragment.model!!.getNodes()
            lateinit var retrievedNode:Node
            for (node in nodes) {
                if (node.name == "TestClass") {
                    retrievedNode = node
                    break
                }
            }
            Assertions.assertEquals("TestClass", retrievedNode.name)
            Assertions.assertEquals("modicio:TestClass", retrievedNode.uri)
            Assertions.assertEquals(1, retrievedNode.getAttributes().size)

            val retrievedAttribute = retrievedNode.getAttributes().first()
            Assertions.assertEquals("testAttribute", retrievedAttribute.name)
            Assertions.assertEquals("modicio:testAttribute", retrievedAttribute.uri)
            Assertions.assertEquals("Integer", retrievedAttribute.dType)
        }

    @Test
    fun createAbstractClassAndDelete() {
        val fragment = TestDataHelper.getSimpleFragmentOnlyModel()
        fragmentRepository.save(fragment)
        val evolutionRequest1 = "CREATE ABSTRACT CLASS TestClass"
        evolutionService.evolveFragment(fragment.variantID, fragment.runningID, evolutionRequest1)
        val result = fragmentRepository.findMostRecentFragmentsByVariantNameLazy(fragment.variantName, 3)

        val evolvedFragment = result.first()
        val nodes = evolvedFragment.model!!.getNodes()
        lateinit var retrievedNode:Node
        for (node in nodes) {
            if (node.name == "TestClass") {
                retrievedNode = node
                break
            }
        }
        //3 classes after creating the new one
        Assertions.assertEquals(3, nodes.size)
        Assertions.assertEquals("TestClass", retrievedNode.name)
        Assertions.assertTrue(retrievedNode.getIsAbstract())

        val evolutionRequest2 = "DELETE CLASS TestClass"
        evolutionService.evolveFragment(fragment.variantID, fragment.runningID, evolutionRequest2)
        val emptyResult = fragmentRepository.findMostRecentFragmentsByVariantNameLazy(fragment.variantName, 3)
        val evolvedFragment2 = emptyResult.first()
        val nodes2 = evolvedFragment2.model!!.getNodes()

        //only 2 classes after delete of previously created class
        Assertions.assertEquals(2, nodes2.size)
    }

}