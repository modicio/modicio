package modic.io.general

import modic.io.TestDataHelper
import modic.io.logic.InstanceService
import modic.io.logic.MetadataService
import modic.io.repository.FragmentRepository
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
class InstanceServiceTests {

    @Autowired
    private lateinit var instanceService: InstanceService

    @Autowired
    private lateinit var metadataService: MetadataService

    @Autowired
    private lateinit var fragmentRepository: FragmentRepository

    @Test
    fun testCreateInstanceNoReferenceFragment(){
        Assertions.assertThrows(Exception::class.java) { instanceService.createInstance("NodeUri", "name", "uri") }
    }

    @Test
    fun testCreateInstance(){

        val referenceFragment = TestDataHelper.getSimpleFragmentOnlyModel()
        fragmentRepository.save(referenceFragment)
        metadataService.setReferenceFragment(referenceFragment.variantID, referenceFragment.runningID)

        val newFragment = instanceService.createInstance("modicio:demo.todo", "Todo", "newURI")

        val fragments = fragmentRepository.findAll()
        Assertions.assertTrue(fragments.contains(newFragment))

    }

    @Test
    fun testGetAllInstances(){

    }

}