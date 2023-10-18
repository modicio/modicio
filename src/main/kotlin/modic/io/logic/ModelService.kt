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

import jakarta.persistence.EntityManager
import jakarta.persistence.PersistenceContext
import jakarta.transaction.Transactional
import modic.io.model.Fragment
import modic.io.model.Model
import modic.io.repository.FragmentRepository
import org.springframework.stereotype.Service
import java.sql.Timestamp
import java.time.Instant
import java.util.*


@Service
class ModelService(
    val fragmentRepository: FragmentRepository,
    val metadataService: MetadataService) {

    @PersistenceContext
    private val entityManager: EntityManager? = null

    /**
     * TODO doc & TESTS
     */
    @Transactional
    fun newVariant(preVariantID: String?, name: String?){

        val now = Timestamp.from(Instant.now())

        if(preVariantID != null){
            val newestFragmentWithID = fragmentRepository.findMostRecentFragmentByVariantIDLazy(preVariantID, isOpen = false)
            if(newestFragmentWithID != null){
                val templateFragment = fragmentRepository.getFragmentByDataID(newestFragmentWithID.dataID!!)
                entityManager!!.detach(templateFragment)

                templateFragment!!.variantID = UUID.randomUUID().toString()
                templateFragment.variantTime = now
                templateFragment.variantName = name ?: templateFragment.variantName
                templateFragment.predecessorID = newestFragmentWithID.globalID
                templateFragment.globalID = UUID.randomUUID().toString()
                templateFragment.open = false
                templateFragment.instance = null
                templateFragment.trace?.clearTrace()
                templateFragment.initializeZeroIDs()

                fragmentRepository.save(templateFragment)
            }else{
                throw Exception("Predecessor variant not found")
            }
        }else{
            val newFragment = Fragment(0, null, false, name ?: "", now,
                UUID.randomUUID().toString(), now, UUID.randomUUID().toString(), false,
                Model(0), null, null)
            fragmentRepository.save(newFragment)
        }

    }

    /**
     * Possibilities:
     * - a variant is specified and exists.
     * - a variant is specified, but it does not exist.
     * - a variant is not specified.
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
    fun pushFullModel(newFragment: Fragment, variantID: String?, name: String?, asVersion: Boolean) {

        //if the fragment has an instance part, it can not be used as model
        if(newFragment.instance != null){
            //TODO Throw error
        }
        //TODO newFragment.validate

        if(variantID == null && name != null){
            //if no variant is specified but a name is given, create a new variant with the given name
            //the name is not checked for uniqueness
            //asVersion is ignored in this case
            newFullVariantWithNameFromFragment(newFragment, name)
        }else if(variantID != null){

            //check if a variant with the given data exists
            val newestFragmentWithID = fragmentRepository.findMostRecentFragmentByVariantIDLazy(variantID, isOpen = false)

            //If yes, take the fragment and use it as template for the new pushed variant
            if(newestFragmentWithID != null){
                val oldFragment = fragmentRepository.findById(newestFragmentWithID.dataID!!).get()
                newVariantFromExistingTrunk(newFragment, oldFragment, asVersion)

            }else{
                //TODO throw error because operation is not executable
                //no or more then one target results found
            }

        }
    }

    /**
     * Initialize a given [Fragment] ...TODO
     */
    private fun newFullVariantWithNameFromFragment(newFragment: Fragment, name: String){
        val now = Timestamp.from(Instant.now())
        newFragment.isReference = false
        newFragment.variantID = UUID.randomUUID().toString()
        newFragment.variantTime = now
        newFragment.runningID = UUID.randomUUID().toString()
        newFragment.runningTime = now
        newFragment.initializeZeroIDs()
        fragmentRepository.save(newFragment)
    }

    private fun newVariantFromExistingTrunk(newFragment: Fragment, oldFragment: Fragment, asVersion: Boolean){
        val now = Timestamp.from(Instant.now())
        newFragment.isReference = false
        if(asVersion){
            newFragment.variantID = oldFragment.variantID
            newFragment.variantTime = oldFragment.variantTime
        }else{
            newFragment.variantID = UUID.randomUUID().toString()
            newFragment.variantTime = now
        }
        newFragment.runningID = UUID.randomUUID().toString()
        newFragment.runningTime = now
        newFragment.predecessorID = oldFragment.globalID
        newFragment.initializeZeroIDs()
        fragmentRepository.save(newFragment)
    }

    /**
     * TODO doc
     */
    fun getReferenceFragment(): Fragment? {
        return fragmentRepository.findFragmentByIsReferenceIsTrue().firstOrNull()
    }

}