package modic.io.service

import jakarta.transaction.Transactional
import modic.io.TestDataHelper
import modic.io.logic.InstanceService
import modic.io.logic.MetadataService
import modic.io.logic.ModelService
import modic.io.model.Accessor
import modic.io.repository.FragmentRepository
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.annotation.DirtiesContext

@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class InstanceIntegrationTests {

    @Autowired
    lateinit var fragmentRepository: FragmentRepository

    @Autowired
    lateinit var metadataService: MetadataService

    @Autowired
    lateinit var modelService: ModelService

    @Autowired
    lateinit var instanceService: InstanceService

    @BeforeEach
    fun setupReferenceFragment(){
        val fragment = TestDataHelper.getSimpleFragmentOnlyModel()
        fragmentRepository.save(fragment)
        metadataService.setReferenceFragment(fragment.variantID, fragment.runningID)
    }

    @Test
    fun createAndAccessInstanceTest(){
        val referenceFragment = modelService.getReferenceFragment()!!

        //The URI is specified in TestDataHelper for the test setup model
        val projectNode = referenceFragment.model!!.findNode("modicio:demo.project")

        if(projectNode != null) {

            //create and save the new project instance:
            val result = instanceService.createInstance(projectNode.uri, "project1", "modicio:project1")!!
            //get the created project instance from the db:
            val projectInstance = instanceService.getInstanceFragment(result.dataID!!, fullType = true, autowire = true)

            //test that storing and loading did happen as expected:
            Assertions.assertNotNull(projectInstance)
            Assertions.assertEquals(projectInstance!!.dataID!!, result.dataID)

            //We can get the Accessor for easy access of instance fields:
            val accessor: Accessor = projectInstance.instance!!.accessor()

            //We know the project has an attribute "Description", so we can just get it:
            val descriptionAttribute = accessor.attributeByName("Description")!!
            descriptionAttribute.anyValue = "My first project"
            instanceService.setAttributes(descriptionAttribute)


            //Let's check if the assignment was successful:
            val projectInstanceCopy = instanceService.getInstanceFragment(projectInstance.dataID!!, fullType = true, autowire = true)!!
            Assertions.assertEquals("My first project",
                projectInstanceCopy.instance!!.accessor().attributeByName("Description")!!.anyValue)

        }else{
            Assertions.fail()
        }
    }
}