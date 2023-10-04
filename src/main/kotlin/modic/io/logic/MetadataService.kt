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

package modic.io.logic

import modic.io.messages.MetaData
import modic.io.model.Fragment
import modic.io.repository.FragmentRepository
import org.springframework.stereotype.Service
import java.time.Instant

@Service
class MetadataService(val fragmentRepository: FragmentRepository) {

    fun getVariantMetadata(timestamp: Instant?, uuid: String?, name: String?): MetaData? {

        //TODO name-only not supported yet

        var fragment: Fragment? = null
        var metadata: MetaData? = null

        if(uuid != null){
            fragment = fragmentRepository.findFirstFragmentByVariantID(uuid)
        } else if (timestamp != null){
            fragment = fragmentRepository.findFirstFragmentByTimestamp(timestamp)
        }

        if(fragment != null){
            metadata = MetaData(fragment.variantTime, fragment.variantID, fragment.variantName)
        }

        return metadata
    }

    fun setReferenceFragment() {
        //TODO
    }
}