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

import modic.io.logic.MetadataService
import modic.io.logic.ModelService
import modic.io.model.Fragment
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.*

@RestController
class ModelController(
    val modelService: ModelService,
    val metadataService: MetadataService) {

    /**
     * Create a new variant. If an existing variant is specified, the new variant will be initialized with a copy of the model.
     * If no existing variant is specified, a new empty variant with the given name is constructed.
     *
     */
    @PostMapping("model/variant", produces=[MediaType.APPLICATION_XML_VALUE])
    fun postNewVariant(
        @RequestParam(required = false, name = "variant_id") variantID: String?,
        @RequestParam(required = false, name = "variant_name") name: String?
    ): String {


        modelService.newVariant(variantID, name)

        return "OK"
    }

    /**
     * Activate the specified variant as reference model.
     */
    @PostMapping("model/variant/reference", produces=[MediaType.APPLICATION_XML_VALUE])
    fun postReference(
        @RequestParam(required = false, name = "running_UUID" ) runningUID: String,
        @RequestParam(required = false, name = "variant_UUID") variantUID: String
    ): String {
        metadataService.setReferenceFragment(variantUID, runningUID)

        //TODO useful response
        return "OK"
    }

    /**
     * Get the active reference model (fragment) as closed model.
     */
    @GetMapping("model/reference", produces = [MediaType.APPLICATION_XML_VALUE])
    fun getReferenceModel(): Fragment? {
        return modelService.getReferenceFragment()
    }

    /**
     * Set a complete model ([Fragment]). If the specified variant is existent, a new running version will be created.
     * if the variant is not present,
     * a new variant with the given `name` is initialized with the specified model as an initial version
     *
     * URL Params
     * - `variant_UUID?=String`
     * - `variant_name=String`
     *
     * Body
     * - XML Body (closed fragment)
     *
     * Checks
     * - XML fragment verification
     *
     * @param variantUID
     * @param name
     * @param fragment
     */
    @PutMapping("model", produces=[MediaType.APPLICATION_XML_VALUE], consumes = [MediaType.APPLICATION_XML_VALUE])
    fun putModelOfVariant(
        @RequestParam(required = false, name = "variant_UUID") variantUID: String?,
        @RequestParam(required = false, name = "variant_name") name: String?,
        @RequestParam(required = false, name = "as_version") asVersion: Boolean = false,
        @RequestBody fragment: Fragment
    ): String {

        if(variantUID == null && name == null){
            //TODO Not executable
            return "Error"
        }

        modelService.pushFullModel(fragment, variantUID, name, asVersion)

        return "OK"
    }


}