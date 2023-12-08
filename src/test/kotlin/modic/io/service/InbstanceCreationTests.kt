package modic.io.service

import modic.io.TestDataHelper
import modic.io.logic.InstanceService
import modic.io.logic.MetadataService
import modic.io.logic.ModelService
import modic.io.repository.FragmentRepository
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.annotation.DirtiesContext

@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class InbstanceCreationTests {

    @Autowired
    lateinit var modelService: ModelService

    @Autowired
    lateinit var metadataService: MetadataService

    @Autowired
    lateinit var fragmentRepository: FragmentRepository

    @Autowired
    lateinit var instanceService: InstanceService

    @Test
    fun setAndGetReferenceFragmentTest(){
        /**
         * For evolution tests,
         * 1. create a fragment and save it
         * 2. activate it via setReferenceFragment
         * 3. do the evolution
         */
        val fragment = TestDataHelper.getSimpleFragmentOnlyModel()
        // <--- Here you can add scripts to nodes


        val storedFragment = fragmentRepository.save(fragment)
        metadataService.setReferenceFragment(fragment.variantID, fragment.runningID)
        //------
        val referenceFragment = modelService.getReferenceFragment()
        Assertions.assertNotNull(referenceFragment)
        //------
        val someInstance = instanceService.createInstance("modicio:demo.todo", "myTODO", "modicio:myTodo")
        //------
        val savedInstance = instanceService.getInstanceFragment(someInstance!!.dataID!!, true)
        // <--- here you can do something with the scripts and the instance
        Assertions.assertNotNull(savedInstance)
    }
    /**
    fun executeScript(fragment: Fragment, scriptName: String){
        val todoNode = fragment.model!!.getNodes().find { n -> n.name == "Todo" }!!
        if(scriptName == "isLate"){
            val id1 = todoNode.getAttributes().find { a -> a.uri == "modicio:demo.todo.Due" }
            isLate(id1)
        }
    }

    fun isLate(dateString: String): Boolean {
        //...
        return true
    }
    */
}