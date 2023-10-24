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
import modic.io.model.Header
import modic.io.model.Instance
import modic.io.repository.FragmentRepository
import org.springframework.stereotype.Service
import java.util.*

@Service
class InstanceService(
    val modelService: ModelService,
    val fragmentRepository: FragmentRepository
) {

    @PersistenceContext
    private val entityManager: EntityManager? = null

    @Transactional
    fun createInstance(nodeURI: String, name: String, uri: String): Fragment? {

        val referenceFragment = modelService.getReferenceFragment() ?: throw Exception("Undefined reference model")
        referenceFragment.autowire()
        entityManager!!.detach(referenceFragment)

        val typeNodes = referenceFragment.model!!.sliceDeep(nodeURI)
        val rootNode = typeNodes.find { n -> n.uri == nodeURI }!!
        val instanceObjects = Instance.constructIObjects(rootNode, typeNodes)

        val newFragment = Fragment(
            0,
            referenceFragment.predecessorID,
            true,
            referenceFragment.variantName,
            referenceFragment.variantTime,
            referenceFragment.variantID,
            referenceFragment.runningTime,
            referenceFragment.runningID,
            false,
            referenceFragment.model,
            Instance(0, name, nodeURI, uri, Header(), instanceObjects.toMutableList())
        )

        newFragment.initializeZeroIDs()
        newFragment.instance!!.updateHeader()
        return fragmentRepository.save(newFragment)
    }

    @Transactional
    fun deleteInstance(fragmentDataID: Long) {
        fragmentRepository.deleteById(fragmentDataID)
    }

    @Transactional
    fun createCompositeInstance(){

    }

    @Transactional
    fun deleteCompositionInstance(){

    }

    @Transactional
    fun getInstanceFragment(fragmentDataID: Long, fullType: Boolean = true): Fragment? {
        //TODO
        return null
    }

    @Transactional
    fun getAllInstances(typeName: String?, variantID: String?, versionID: String?, limit: Int): List<Fragment> {
        //TODO
        return LinkedList()
    }


    @Transactional
    fun updateInstance(fragment: Fragment): Fragment? {
        //TODO
        return null
    }


}

