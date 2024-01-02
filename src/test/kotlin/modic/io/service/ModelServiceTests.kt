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

package modic.io.service

import modic.io.TestDataHelper
import modic.io.logic.InstanceService
import modic.io.logic.MetadataService
import modic.io.logic.ModelService
import modic.io.logic.PredefinedFunctions
import modic.io.model.AttributeInstance
import modic.io.model.IObject
import modic.io.model.Script
import modic.io.repository.FragmentRepository
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.annotation.DirtiesContext
import java.util.*

@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class ModelServiceTests {

    @Autowired
    lateinit var modelService: ModelService

    @Autowired
    lateinit var instanceService: InstanceService

    @Autowired
    lateinit var metadataService: MetadataService

    @Autowired
    lateinit var fragmentRepository: FragmentRepository

    @Test
    fun pushFullVariantNoMetadataTest() {
        val fragment = TestDataHelper.getSimpleFragmentOnlyModel()
        modelService.pushFullModel(fragment, null, "Some Name", false)
        val res = fragmentRepository.findFragmentByVariantID(fragment.variantID)
        Assertions.assertEquals(1, res.size)
        val resFragment = res.first()

        Assertions.assertEquals(fragment.variantID, resFragment.variantID)
        Assertions.assertEquals(fragment.model?.getNodes()?.size, resFragment.model?.getNodes()?.size)
        Assertions.assertNotNull(resFragment.model?.getNodes()?.first()?.annotation?.variantID)
        Assertions.assertNotEquals("", resFragment.model?.getNodes()?.first()?.annotation?.variantID)

        Assertions.assertEquals(
            resFragment.model?.getNodes()?.first()?.annotation?.variantID,
            resFragment.model?.getNodes()?.first()?.annotation?.variantID
        )

        Assertions.assertEquals(
            resFragment.model?.getNodes()?.first()?.annotation?.variantTime,
            resFragment.model?.getNodes()?.first()?.annotation?.variantTime
        )
    }

    @Test
    fun setAndGetReferenceFragmentTest(){
        /**
         * For evolution tests,
         * 1. create a fragment and save it
         * 2. activate it via setReferenceFragment
         * 3. do the evolution
         */
        val fragment1 = TestDataHelper.getSimpleFragmentOnlyModel()
        val fragment2 = TestDataHelper.getSimpleFragmentOnlyModel()
        val storedFragment1 = fragmentRepository.save(fragment1)
        val storedFragment2 = fragmentRepository.save(fragment2)
        metadataService.setReferenceFragment(fragment2.variantID, fragment2.runningID)
        val referenceFragment = modelService.getReferenceFragment()
        Assertions.assertNotNull(referenceFragment)
        Assertions.assertEquals(storedFragment2.dataID, referenceFragment!!.dataID)
    }

    @Test
    fun setAndResetReferenceFragmentTest(){
        val fragment1 = TestDataHelper.getSimpleFragmentOnlyModel()
        val fragment2 = TestDataHelper.getSimpleFragmentOnlyModel()
        val storedFragment1 = fragmentRepository.save(fragment1)
        val storedFragment2 = fragmentRepository.save(fragment2)
        metadataService.setReferenceFragment(fragment1.variantID, fragment1.runningID)
        var referenceFragment = modelService.getReferenceFragment()
        Assertions.assertEquals(storedFragment1.dataID, referenceFragment!!.dataID)

        metadataService.setReferenceFragment(fragment2.variantID, fragment2.runningID)
        referenceFragment = modelService.getReferenceFragment()
        Assertions.assertEquals(storedFragment2.dataID, referenceFragment!!.dataID)
    }

    @Test
    fun newVariantNoPredecessorTest(){
        modelService.newVariant(null, "foo")
        val allFragments = fragmentRepository.findAll()
        Assertions.assertEquals(1, allFragments.size)
        val newFragment = allFragments.first()
        Assertions.assertEquals("foo", newFragment.variantName)
        Assertions.assertFalse(newFragment.open)
        Assertions.assertNull(newFragment.instance)
        Assertions.assertNull(newFragment.predecessorID)
    }

    @Test
    fun newVariantWithPredecessorTest(){
        val predecessor = fragmentRepository.save(TestDataHelper.getSimpleFragmentOnlyModel())
        modelService.newVariant(predecessor.variantID, "foo")
        val allFragments = fragmentRepository.findAll()
        Assertions.assertEquals(2, allFragments.size)
        val fooFragments = allFragments.filter { f -> f.variantName == "foo" }
        Assertions.assertEquals(1, fooFragments.size)
        val newFragment = fooFragments.first()!!
        Assertions.assertNotNull(newFragment)
        Assertions.assertFalse(newFragment.open)
        Assertions.assertNull(newFragment.instance)
        Assertions.assertEquals(predecessor.globalID, newFragment.predecessorID)
        Assertions.assertEquals(predecessor.model!!.getNodes().size, newFragment.model!!.getNodes().size)
    }

    @Test
    fun newVariantWithPredecessorFailsTest(){
        try {
            modelService.newVariant(UUID.randomUUID().toString(), "foo")
            Assertions.fail<Any>("Exception Expected")
        }catch (e: Exception){
            Assertions.assertNotNull(e)
            Assertions.assertEquals("Predecessor variant not found", e.message)
        }
    }

    @Test
    fun someScriptEdit() {
        // JUST SETUP
        val fragment1 = TestDataHelper.getSimpleFragmentOnlyModel()
        val storedFragment1 = fragmentRepository.save(fragment1)
        metadataService.setReferenceFragment(fragment1.variantID, fragment1.runningID)

        //done by the admin:
        val referenceFragment = modelService.getReferenceFragment()
        val projectNode = referenceFragment!!.model!!.getNodes().find { n -> n.name == "Project" }

        // data to use in the predefined functions
        val myScript = Script(0, "", "addHelloToDescription", "button", "{description=Description}")
        projectNode!!.addScript(myScript)

        // Later the user
        val myNewProjectInstance = instanceService.createInstance(projectNode.uri, "My New Project", "")

        // Add AttributeInstances
        // todo maybe use instanceService.setAttributes()
        val o = IObject(instanceOf = "modicio:demo.project")
        val attributeInstance = AttributeInstance(attributeUri="modicio:demo.project.Description")
        attributeInstance.anyValue = "Example of Description"
        o.addAttributeInstance(attributeInstance)

        // call function of script
        val predefinedFunction = PredefinedFunctions.callFunction(myScript, myNewProjectInstance!!)
        println(predefinedFunction)
    }

}