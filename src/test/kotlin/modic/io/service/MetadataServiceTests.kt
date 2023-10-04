package modic.io.service

import modic.io.TestDataHelper
import modic.io.logic.MetadataService
import modic.io.messages.MetaData
import modic.io.repository.FragmentRepository
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.annotation.DirtiesContext

@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class MetadataServiceTests {

    @Autowired
    lateinit var fragmentRepository: FragmentRepository

    @Autowired
    lateinit var metadataService: MetadataService

    @Test
    fun getMetadataByVariantIDTest(){
        val fragment = fragmentRepository.save(TestDataHelper.getSimpleFragmentOnlyModel())

        val metadata: MetaData? = metadataService.getVariantMetadata(null, fragment.variantID, null)
        Assertions.assertEquals(metadata?.uuid,  fragment.variantID)
        Assertions.assertEquals(metadata?.timestamp,  fragment.variantTime)
        Assertions.assertEquals(metadata?.name,  fragment.variantName)
    }

    @Test
    fun getMetadataByVariantTimeTest(){
        val fragment = fragmentRepository.save(TestDataHelper.getSimpleFragmentOnlyModel())

        val metadata: MetaData? = metadataService.getVariantMetadata(fragment.variantTime, null, null)
        Assertions.assertEquals(metadata?.uuid,  fragment.variantID)
        Assertions.assertEquals(metadata?.timestamp,  fragment.variantTime)
        Assertions.assertEquals(metadata?.name,  fragment.variantName)
    }

}