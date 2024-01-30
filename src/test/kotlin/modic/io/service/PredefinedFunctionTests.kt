package modic.io.service

import modic.io.TestDataHelper
import modic.io.logic.*
import modic.io.model.*
import modic.io.repository.FragmentRepository
import org.junit.jupiter.api.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.annotation.DirtiesContext
import java.text.SimpleDateFormat
import java.sql.Timestamp

@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class PredefinedFunctionTests {

    @Autowired
    private lateinit var modelService: ModelService
    @Autowired
    private lateinit var instanceService: InstanceService
    @Autowired
    private lateinit var metadataService: MetadataService
    @Autowired
    private lateinit var fragmentRepository: FragmentRepository

    private lateinit var referenceFragment: Fragment

    @BeforeEach
    fun setupReferenceFragment() {
        referenceFragment = TestDataHelper.getFragmentForPredefinedFunctions().also {
            fragmentRepository.save(it)
            metadataService.setReferenceFragment(it.variantID, it.runningID)
            modelService.getReferenceFragment()!!
        }
    }

    @Test
    fun checkDeadlineScript() {
        testScript(
            nodeUri = "modicio:demo.task",
            script = Script(0, "modicio:demo.myScript", "checkDeadline", "RealTimeUpdater",
                "{startTime=StartTime, endTime=EndTime, deadline=Deadline}", "IsDeadLineCrossed"),
            attributeSetter = { projectInstance ->
                val timestampFormat = SimpleDateFormat("dd.MM.yyyy HH:mm:ss")
                setAttributeValue(projectInstance, "Deadline", timestampFormat.format(Timestamp(timestampFormat.parse("01.01.2023 00:00:00").time)))
                setAttributeValue(projectInstance, "EndTime", timestampFormat.format(Timestamp(timestampFormat.parse("20.01.2023 00:00:00").time)))
            },
            expectedAttributeValue = "true"
        )
    }

    @Test
    fun calculateHours() {
        testScript(
            nodeUri = "modicio:demo.employee",
            script = Script(0, "modicio:demo.myScript", "calculateRemainingHours", "RealTimeUpdater",
                "{hoursWorked=HoursWorked, totalHours=TotalHours}", "RemainingHours"),
            attributeSetter = { projectInstance ->
                setAttributeValue(projectInstance, "HoursWorked", "100")
                setAttributeValue(projectInstance, "TotalHours", "150")
            },
            expectedAttributeValue = "50"
        )
    }

    @Test
    fun resetInt() {
        // reset HoursWorked
        testScript(
            nodeUri = "modicio:demo.employee",
            script = Script(0, "modicio:demo.myScript", "resetInt", "SingleAssignment",
                "{}", "HoursWorked"),
            attributeSetter = { projectInstance ->
                setAttributeValue(projectInstance, "HoursWorked", "100")
            },
            expectedAttributeValue = "0"
        )
    }

    private fun testScript(
        nodeUri: String,
        script: Script,
        attributeSetter: (Fragment) -> Unit,
        expectedAttributeValue: String
    ) {
        val projectNode = referenceFragment.model?.findNode(nodeUri).apply {
            this?.addScript(script)
        }

        val projectInstanceUri = instanceService.createInstance(projectNode!!.uri, "myNewProject", "modicio:instance.myNewProject")?.dataID
        val projectInstance = instanceService.getInstanceFragment(projectInstanceUri!!, fullType = true, autowire = true)

        attributeSetter(projectInstance!!)

        PredefinedFunctions.callFunction(script, projectInstance, instanceService)
        // script.anyValue is where the output will be written
        Assertions.assertEquals(expectedAttributeValue, projectInstance.getAttributeInstance(script.anyValue).anyValue)
    }

    private fun setAttributeValue(fragment: Fragment, attributeName: String, value: String) {
        fragment.getAttributeInstance(attributeName).apply {
            anyValue = value
            instanceService.setAttributes(listOf(this))
        }
    }
}
