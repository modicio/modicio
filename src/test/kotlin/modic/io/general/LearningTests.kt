package modic.io.general

import modic.io.TestDataHelper
import modic.io.logic.ModelService
import modic.io.model.Fragment
import modic.io.repository.FragmentRepository
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
class LearningTests {

    @Autowired
    private lateinit var modelService: ModelService

    @Autowired
    private lateinit var fragmentRepository: FragmentRepository

    @Test
    fun learningTest() {

        val fragment = Fragment()
        modelService.newFullVariantWithNameFromFragment(fragment, "newName")

        val fragments = fragmentRepository.findAll()

        Assertions.assertTrue(fragments.size == 1)

    }

    @Test
    fun anotherLearningTest(){

        val fragment = TestDataHelper.getSimpleFragmentOnlyModel()

        //Fragment.validateToXSD(fragment)

    }



}