package modic.io.logic

import io.mockk.every
import io.mockk.mockk
import io.mockk.spyk
import io.mockk.verify
import modic.io.model.Fragment
import modic.io.model.Model
import modic.io.model.Script
import modic.io.repository.FragmentRepository
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.assertThrows
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class BehaviorServiceTest {

    lateinit var fragmentRepository : FragmentRepository
    lateinit var behaviourService: BehaviourService
    val EXISTING_FRAGMENT_ID : Long = 123456
    val NON_EXISTING_FRAGMENT_ID : Long = 0
    val EXISTING_SCRIPT : String = "ScriptURI"

    @BeforeAll
    fun setup(){
        fragmentRepository = mockk()
        behaviourService = BehaviourService(fragmentRepository)
    }

    @Test
    fun testTriggerStoredActionScript(){

        val fragment: Fragment = createMocks()

        val returnedFragment = behaviourService.triggerStoredActionScript(EXISTING_FRAGMENT_ID, EXISTING_SCRIPT)

        verify(atLeast = 1) { fragmentRepository.getFragmentByDataID(EXISTING_FRAGMENT_ID) }
        assert(returnedFragment == fragment)
    }

    @Test
    fun testTriggerStoredActionScriptNoFragment(){

        createMocks()

        val returnedFragment = behaviourService.triggerStoredActionScript(0, EXISTING_SCRIPT)

        verify(atLeast = 1) { fragmentRepository.getFragmentByDataID(0) }
        assert(returnedFragment == null)
    }

    @Test
    fun testTriggerStoredActionScriptNoScript(){

        createMocks()

        assertThrows<Exception> { behaviourService.triggerStoredActionScript(EXISTING_FRAGMENT_ID, "") }
        verify(atLeast = 1) { fragmentRepository.getFragmentByDataID(EXISTING_FRAGMENT_ID) }
    }

    private fun createMocks(): Fragment {
        val script: Script = mockk()

        val model: Model = mockk()
        every { model.findScript(EXISTING_SCRIPT) } returns script

        val fragment: Fragment = spyk(Fragment(), recordPrivateCalls = true)
        every { fragment.model } returns model
        every { fragmentRepository.getFragmentByDataID(EXISTING_FRAGMENT_ID) } returns fragment
        every { fragmentRepository.getFragmentByDataID(NON_EXISTING_FRAGMENT_ID) } returns null

        return fragment
    }

}