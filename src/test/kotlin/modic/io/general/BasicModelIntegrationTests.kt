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

package modic.io.general

import modic.io.TestDataHelper
import modic.io.repository.FragmentRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.annotation.DirtiesContext

@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class BasicModelIntegrationTests {

    @Autowired
    lateinit var fragmentRepository: FragmentRepository

    @Test
    fun repositoryWriteReadFragmentTest(){
        val fragment = TestDataHelper.getSimpleFragmentOnlyModel()
        val fragment2 = fragmentRepository.save(fragment)
        assertEquals(fragment2.dataID, 1)
        assertEquals(fragment2.trace?.getDeltas()?.size, 2)
        assertEquals(fragment2.model?.getNodes()?.size, 2)

        val fragment3 = fragmentRepository.findById(fragment2.dataID!!).get()
        assertEquals(fragment3.trace?.getDeltas()?.size, 2)
        assertEquals(fragment3.model?.getNodes()?.size, 2)
    }

}