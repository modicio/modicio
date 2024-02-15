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
import modic.io.messages.MetaData
import modic.io.model.Fragment
import modic.io.repository.FragmentRepository
import org.springframework.stereotype.Service
import java.sql.Timestamp
import java.util.*

@Service
class MetadataService(
    val fragmentRepository: FragmentRepository) {

    /**
     * Get the [MetaData] of a set of [Fragment]s. The result size can be limited.
     * All three identifiers are optional. if no identifier is provided, an empty list is returned.
     * If exactly one identifier is provided, it is used to find the Fragments.
     * If more than one identifier is provided, ONLY ONE identifier is used to find the fragments. This
     * identifier is chosen by priority:
     * 1: variantID (uuid)
     * 2: variantTime (timestamp)
     * 3: variantName (name)
     *
     * @param timestamp Optional timestamp of the variant(s) to find Fragments
     * @param uuid Optional variantID of the variant to find Fragments: In case the ID is given, at most one fragment is
     * returned
     * @param name Optional name of the variant(s) to find Fragments
     * @param limit The max result size. Default value is one.
     */
    fun getVariantMetadata(timestamp: Timestamp?, uuid: String?, name: String?, limit: Int = 1,  closedOnly: Boolean = true): List<MetaData> {

        var fragments: List<Fragment> = LinkedList()

        if (uuid != null) {
            val fragment = fragmentRepository.findMostRecentFragmentByVariantIDLazy(uuid, !closedOnly)
            fragments = if(fragment != null) {
                listOf(fragment)
            }else{
                LinkedList()
            }
        } else if (timestamp != null) {
            fragments = fragmentRepository.findMostRecentFragmentsByVariantTimeLazy(timestamp, limit, !closedOnly)
        } else if (name != null) {
            fragments = fragmentRepository.findMostRecentFragmentsByVariantNameLazy(name, limit, !closedOnly)
        }

        return fragments.map { f -> MetaData(f.variantTime, f.variantID, f.variantName) }
    }

    fun getAllVariantsMetadata(limit: Int = 1, closedOnly: Boolean = true): List<MetaData> {
        return fragmentRepository.findOneFragmentOfEachVariantLazy(limit, !closedOnly).map {
            f -> MetaData(f.variantTime, f.variantID, f.variantName)
        }
    }


    fun getAllRunningVersionsOfVariant(variantID: String, limit: Int = 1, closedOnly: Boolean = true): List<MetaData> {
        return fragmentRepository.findAllRunningVersionsOfVariant(variantID, limit, !closedOnly).map {
            m -> MetaData(m.runningTime, m.runningID, null)
        }
    }

    @Transactional
    fun setReferenceFragment(variantID: String, runningID: String) {
        val oldReferenceFragments = fragmentRepository.findFragmentByIsReferenceIsTrue()
        val newReferenceFragment = fragmentRepository.findModelOnlyFragmentWithVariantAndRunningIDFirstLazy(variantID, runningID)
        if(newReferenceFragment != null) {
            val fragment = fragmentRepository.getFragmentByDataID(newReferenceFragment.dataID!!)
            if(fragment != null){
                oldReferenceFragments.forEach { f ->
                    f.isReference = false
                }
                fragment.isReference = true
            }
        }

    }
}