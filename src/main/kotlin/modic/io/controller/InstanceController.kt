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

package modic.io.controller

import modic.io.logic.InstanceService
import modic.io.model.Fragment
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.*

@RestController
class InstanceController(val instanceService: InstanceService) {

    /**
     * TODO doc
     */
    @PostMapping("instance/create", produces = [MediaType.APPLICATION_XML_VALUE])
    fun createNewInstance(
        @RequestParam(required = true, name = "type_node_uri") nodeURI: String,
        @RequestParam(required = true, name = "instance_name") instanceName: String,
        @RequestParam(required = true, name = "instance_uri") instanceURI: String
    ): Fragment?{
        return instanceService.createInstance(nodeURI, instanceName, instanceURI)
    }

    /**
     * TODO doc
     */
    @GetMapping("instance", produces = [MediaType.APPLICATION_XML_VALUE])
    fun getInstance(
        @RequestParam(required = true, name = "fragment_data_id") fragmentDataID: Long,
        @RequestParam(required = true, name = "full_type") fullType: Boolean
    ): Fragment? {
        return instanceService.getInstanceFragment(fragmentDataID, fullType)
    }

    /**
     * TODO doc
     */
    @GetMapping("instance/all", produces = [MediaType.APPLICATION_XML_VALUE])
    fun getInstances(
        @RequestParam(required = true, name = "type_name_pattern") typeNamePattern: String,
        @RequestParam(required = false, name = "variant_id") variantID: String?,
        @RequestParam(required = false, name = "version_id") versionID: String?,
        @RequestParam(required = true, name = "limit") limit: Int,
    ): List<Fragment> {
        return instanceService.getAllInstances(typeNamePattern, variantID, versionID, limit)
    }

    /**
     * TODO doc
     */
    @DeleteMapping("instance", produces = [MediaType.APPLICATION_XML_VALUE])
    fun deleteInstance(
        @RequestParam(required = true, name = "fragment_data_id") fragmentDataID: Long
    ) {
        instanceService.deleteInstance(fragmentDataID)
    }



}