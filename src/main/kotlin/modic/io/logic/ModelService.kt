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

import jakarta.transaction.Transactional
import modic.io.model.Fragment
import modic.io.repository.FragmentRepository
import org.springframework.stereotype.Service
import java.time.Instant
import java.util.*

@Service
class ModelService(
    val fragmentRepository: FragmentRepository,
    val metadataService: MetadataService) {

    /**
     * Possibilities:
     * - a variant is specified, but it does not exist
     * - a variant is not specified
     *
     * 1. Check if a variant is specified. If only the name is given, a new variant will be created.
     * 2. If nothing is given, return an error
     * 3. Check if the given variant exists. If yes, retrieve the most recent running version.
     *      - take the new variant and set the new running version
     *      - for all nodes, check their existence. If existent set the according version.
     *        if not existent, initialize the version with 1 (independent of the provided version)
     *        use URI for matching
     * 4. If the variant does not exist, take the given model and initialize every version to 1
     * 5. Validate the Fragment
     * 6. Store the Fragment (and delete the predecessor)
     * 7. If the predecessor was the active reference, the new variant will become active reference
     */
    @Transactional
    fun pushFullVariant(newFragment: Fragment, variantUID: String?, name: String?) {

        //if the fragment has an instance part, it can not be used as model
        if(newFragment.instance != null){
            //TODO Throw error
        }
        //TODO newFragment.validate

        //if no variant is specified but a name is given, create a new variant with the given name
        //the name is not checked for uniqueness
        if(variantUID == null && name != null){
            newVariantWithNameFromFragment(newFragment, name)
        }else{

            //check if a variant with the given data exists
            val metaData = metadataService.getVariantMetadata(null, variantUID, name).firstOrNull()

            if( metaData != null){

                //TODO get Fragment and check if only one exists
                //newVariantFromExistingTrunk(newFragment, oldFragment)

            }else{
                //TODO throw error because operation is not executable
                //no or more then one target results found
            }

        }
    }

    private fun newVariantWithNameFromFragment(newFragment: Fragment, name: String){
        newFragment.isReference = false
        newFragment.variantID = UUID.randomUUID().toString()
        newFragment.variantTime = Instant.now()
        newFragment.initializeZeroIDs()
        fragmentRepository.save(newFragment)
    }

    private fun newVariantFromExistingTrunk(newFragment: Fragment, oldFragment: Fragment){
        //TODO
    }

    /**
     * TODO doc
     */
    fun getReferenceFragment(): Fragment? {
        return fragmentRepository.findFragmentByIsReferenceIsTrue().firstOrNull()
    }

}