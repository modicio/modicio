package modic.io.service

import modic.io.TestDataHelper
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
class ModelServiceTests {

    @Autowired
    lateinit var modelService: ModelService

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

}