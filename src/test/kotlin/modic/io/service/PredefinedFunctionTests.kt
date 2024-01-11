package modic.io.service

import modic.io.TestDataHelper
import modic.io.logic.InstanceService
import modic.io.logic.MetadataService
import modic.io.logic.ModelService
import modic.io.logic.PredefinedFunctions
import modic.io.model.Fragment
import modic.io.model.Script
import modic.io.repository.FragmentRepository
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.annotation.DirtiesContext
import java.sql.Timestamp
import java.text.SimpleDateFormat

@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class PredefinedFunctionTests {

    @Autowired
    lateinit var modelService: ModelService

    @Autowired
    lateinit var instanceService: InstanceService

    @Autowired
    lateinit var metadataService: MetadataService

    @Autowired
    lateinit var fragmentRepository: FragmentRepository

    private lateinit var referenceFragment: Fragment


    @BeforeEach
    fun setupReferenceFragment(){
        val fragment1 = TestDataHelper.getFragmentForPredefinedFunctions()
        fragmentRepository.save(fragment1)
        metadataService.setReferenceFragment(fragment1.variantID, fragment1.runningID)
        referenceFragment = modelService.getReferenceFragment()!!
    }

    @Test
    fun checkDeadlineScript() {
        // Setup
        val projectNode = referenceFragment.model?.findNode("modicio:demo.task")
        val myScript = Script(0, "modicio:demo.myScript", "checkDeadline", "cronJob",
            "{startTime=StartTime, endTime=EndTime, deadline=Deadline}", "IsDeadLineCrossed")
        projectNode!!.addScript(myScript)

        val projectInstanceUri = instanceService.createInstance(projectNode.uri, "myNewProject", "modicio:instance.myNewProject")?.dataID
        val projectInstance = instanceService.getInstanceFragment(projectInstanceUri!!, fullType = true, autowire = true)

        // Set values
        val timestampFormat = SimpleDateFormat("dd.MM.yyyy HH:mm:ss")
        setAttributeValue(projectInstance!!, "Deadline", timestampFormat.format(Timestamp(timestampFormat.parse("01.01.2023 00:00:00").time)))
        setAttributeValue(projectInstance, "EndTime", timestampFormat.format(Timestamp(timestampFormat.parse("20.01.2023 00:00:00").time)))

        // Call function of script
        val predefinedFunction = PredefinedFunctions.callFunction(myScript, projectInstance, projectNode, instanceService)
        Assertions.assertEquals("Success", predefinedFunction)
        Assertions.assertEquals("true", projectInstance.getAttributeInstance("IsDeadLineCrossed").anyValue)
    }


    @Test
    fun calculateHours() {
        // Setup
        val projectNode = referenceFragment.model?.findNode("modicio:demo.employee")
        val myScript = Script(0, "modicio:demo.myScript", "calculateRemainingHours", "cronJob",
            "{hoursWorked=HoursWorked, totalHours=TotalHours}", "RemainingHours")
        projectNode!!.addScript(myScript)

        val projectInstanceUri = instanceService.createInstance(projectNode.uri, "myNewProject", "modicio:instance.myNewProject")?.dataID
        val projectInstance = instanceService.getInstanceFragment(projectInstanceUri!!, fullType = true, autowire = true)

        // Set values
        setAttributeValue(projectInstance!!, "HoursWorked", "100")
        setAttributeValue(projectInstance, "TotalHours", "150")

        // Call function of script
        val predefinedFunction = PredefinedFunctions.callFunction(myScript, projectInstance, projectNode, instanceService)
        Assertions.assertEquals("Success", predefinedFunction)
        Assertions.assertEquals("50", projectInstance.getAttributeInstance("RemainingHours").anyValue)
    }

    private fun setAttributeValue(fragment: Fragment, attributeName: String, value: String) {
        val attribute = fragment.getAttributeInstance(attributeName)
        attribute.anyValue = value
        instanceService.setAttributes(listOf(attribute))
    }
}