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
import modic.io.logic.MetadataService
import modic.io.messages.MetaData
import modic.io.model.Fragment
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
        val dbFragment = createFragment()
        val metadata: MetaData? = metadataService.getVariantMetadata(null, dbFragment.variantID, null, 10000, true).firstOrNull()
        compareMetadataOfFragment(dbFragment, metadata)
    }

    @Test
    fun getMetadataByVariantTimeTest(){
        val dbFragment = createFragment()
        val metadata: MetaData? = metadataService.getVariantMetadata(dbFragment.variantTime, null, null).firstOrNull()
        compareMetadataOfFragment(dbFragment, metadata)
    }

    @Test
    fun getMetadataWithNullUUID(){
        val dbFragment = createFragment()
        val metadata = metadataService.getVariantMetadata(dbFragment.variantTime, null, "").firstOrNull()
        compareMetadataOfFragment(dbFragment, metadata)
    }

    @Test
    fun getMetadataWithNullTimestamp(){
        val dbFragment = createFragment()
        val metadata = metadataService.getVariantMetadata(dbFragment.variantTime, null, "").firstOrNull()
        compareMetadataOfFragment(dbFragment, metadata)
    }

    @Test
    fun getMetadataWithOnlyName(){
        val dbFragment = createFragment()
        val metadata = metadataService.getVariantMetadata(null, null, dbFragment.variantName).firstOrNull()
        compareMetadataOfFragment(dbFragment, metadata)
    }

    @Test
    fun getMetadataAllNullValues(){
        val metadata = metadataService.getVariantMetadata(null, null, null)
        Assertions.assertTrue(metadata.isEmpty())
    }

    @Test
    fun getMetadataForNonexistentFragment(){
        val metadata = metadataService.getVariantMetadata(null, "", null)
        Assertions.assertTrue(metadata.isEmpty())
    }

    private fun compareMetadataOfFragment(dbFragment: Fragment, metadata: MetaData?) {
        Assertions.assertEquals(dbFragment.variantID, metadata?.uuid)
        Assertions.assertEquals(dbFragment.variantTime, metadata?.timestamp)
        Assertions.assertEquals(dbFragment.variantName, metadata?.name)
    }

    private fun createFragment(): Fragment {
        val savedFragmentID = fragmentRepository.save(TestDataHelper.getSimpleFragmentOnlyModel()).dataID
        val dbFragment = fragmentRepository.getFragmentByDataID(savedFragmentID!!)!!
        return dbFragment
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