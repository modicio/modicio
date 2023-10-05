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

import modic.io.logic.ModelService
import modic.io.model.Delta
import modic.io.model.Fragment
import modic.io.model.Model
import modic.io.model.Trace
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.*
import java.time.Instant

@RestController
class ModelController(val modelService: ModelService) {

    /**
     * Create a new variant. If an existing variant is specified, the new variant will be initialized with a copy of the model.
     * If no existing variant is specified, a new empty variant with the given name is constructed.
     *
     */
    @PostMapping("model/variant", produces=[MediaType.APPLICATION_JSON_VALUE])
    fun postNewVariant(
        @RequestParam(required = false, name = "variant_timestamp") timestamp: String?,
        @RequestParam(required = false, name = "variant_UUID") variantUID: String?,
        @RequestParam(required = false, name = "variant_name") name: String?
    ): String {
        return "TODO"
    }

    /**
     * Activate the specified variant as reference model.
     */
    @PostMapping("model/variant/reference", produces=[MediaType.APPLICATION_JSON_VALUE])
    fun postReference(
        @RequestParam(required = false, name = "variant_timestamp") timestamp: String,
        @RequestParam(required = false, name = "variant_UUID") variantUID: String
    ): String {
        return "TODO"
    }

    /**
     * Get the active reference model (fragment) as closed model.
     */
    @GetMapping("model/reference", produces = [MediaType.APPLICATION_XML_VALUE])
    fun getReferenceModel(): Fragment {
        // FIXME just da simple demo here
        return Fragment(
            null, false, "xxx", Instant.now(),  "0123", true,
            Model(null, Instant.now(), "123ad", HashSet(), null), null,
            Trace(null, mutableListOf(Delta(null, "hi", "ho")), null)
        )
    }

    /**
     * Set a complete model ([Fragment]). If the specified variant is existent, a new running version will be created.
     * if the variant is not present,
     * a new variant with the given `name` is initialized with the specified model as an initial version
     *
     * URL Params
     * - `variant_timestamp?=STRING`
     * - `variant_UUID?=String`
     * - `variant_name=String`
     *
     * Body
     * - XML Body (closed fragment)
     *
     * Checks
     * - XML fragment verification
     *
     * @param timestamp
     * @param variantUID
     * @param name
     * @param fragment
     */
    @PutMapping("model", produces=[MediaType.APPLICATION_JSON_VALUE], consumes = [MediaType.APPLICATION_XML_VALUE])
    fun putModelOfVariant(
        @RequestParam(required = false, name = "variant_timestamp") timestamp: String?,
        @RequestParam(required = false, name = "variant_UUID") variantUID: String?,
        @RequestParam(required = false, name = "variant_name") name: String?,
        @RequestBody fragment: Fragment
    ): String {

        if(timestamp == null && variantUID == null && name == null){
            //TODO Not executable
            return "Error"
        }

        modelService.pushFullVariant(fragment, timestamp, variantUID, name)

        return "TODO"
    }

}