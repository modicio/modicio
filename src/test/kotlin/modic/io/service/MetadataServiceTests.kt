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
import java.util.*

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

        val metadata: MetaData? = metadataService.getVariantMetadata(
            null, fragment.variantID, null, 10000, true).firstOrNull()
        Assertions.assertEquals(metadata?.uuid,  fragment.variantID)
        Assertions.assertEquals(metadata?.timestamp,  fragment.variantTime)
        Assertions.assertEquals(metadata?.name,  fragment.variantName)
    }

    @Test
    fun getMetadataByVariantTimeTest(){
        val fragment = fragmentRepository.save(TestDataHelper.getSimpleFragmentOnlyModel())

        val metadata: MetaData? = metadataService.getVariantMetadata(
            fragment.variantTime, null, null).firstOrNull()
        Assertions.assertEquals(metadata?.uuid,  fragment.variantID)
        Assertions.assertEquals(metadata?.timestamp,  fragment.variantTime)
        Assertions.assertEquals(metadata?.name,  fragment.variantName)
    }

    @Test
    fun getAllVariantsMetadataTest(){
        val someID1 = UUID.randomUUID().toString()
        val someID2 = UUID.randomUUID().toString()
        val someID3 = UUID.randomUUID().toString()
        fragmentRepository.save(TestDataHelper.getSimpleFragmentOnlyModel(someID1))
        fragmentRepository.save(TestDataHelper.getSimpleFragmentOnlyModel(someID2))
        fragmentRepository.save(TestDataHelper.getSimpleFragmentOnlyModel(someID1))
        fragmentRepository.save(TestDataHelper.getSimpleFragmentOnlyModel(someID3))
        val metadata = metadataService.getAllVariantsMetadata(1000)
        Assertions.assertEquals(3, metadata.size)
        Assertions.assertNotNull(metadata.find { f -> f.uuid == someID1 })
        Assertions.assertNotNull(metadata.find { f -> f.uuid == someID2 })
        Assertions.assertNotNull(metadata.find { f -> f.uuid == someID3 })
    }

    @Test
    fun getAllRunningVersionsOfVariantMetadataTest(){
        val someVariantID1 = UUID.randomUUID().toString()
        val someRunningID2 = UUID.randomUUID().toString()
        val someRunningID3 = UUID.randomUUID().toString()
        fragmentRepository.save(TestDataHelper.getSimpleFragmentOnlyModel(someVariantID1, someRunningID3))
        fragmentRepository.save(TestDataHelper.getSimpleFragmentOnlyModel(someVariantID1, someRunningID2))
        fragmentRepository.save(TestDataHelper.getSimpleFragmentOnlyModel(someVariantID1, someRunningID2))
        fragmentRepository.save(TestDataHelper.getSimpleFragmentOnlyModel(null, someVariantID1))
        val metadata = metadataService.getAllRunningVersionsOfVariant(someVariantID1, 1000)
        Assertions.assertEquals(2, metadata.size)
        Assertions.assertNotNull(metadata.find { f -> f.uuid == someRunningID2 })
        Assertions.assertNotNull(metadata.find { f -> f.uuid == someRunningID3 })
    }

}