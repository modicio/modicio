package modic.io.service

import modic.io.TestDataHelper
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
    lateinit var fragmentRepository: FragmentRepository

    @Test
    fun pushFullVariantNoMetadataTest() {
        val fragment = TestDataHelper.getSimpleFragmentOnlyModel()
        modelService.pushFullVariant(fragment, null, null, "Some Name")
        val res = fragmentRepository.findFragmentByVariantID(fragment.variantID, 100)
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

}