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
import modic.io.messages.MetaData
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.*

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
    @GetMapping("model/reference", produces=[MediaType.APPLICATION_XML_VALUE])
    fun getReferenceModel(): List<MetaData> {
        //TODO
        return listOf(MetaData("foo", "bar", "baz"))
    }

    /**
     * Create a new variant. If an existing variant is specified, the new variant will be initialized with a copy of the model.
     * If no existing variant is specified, a new empty variant with the given name is constructed.
     *
     */
    @PutMapping("model", produces=[MediaType.APPLICATION_JSON_VALUE])
    fun putModelOfVariant(
        @RequestParam(required = false, name = "variant_timestamp") timestamp: String?,
        @RequestParam(required = false, name = "variant_UUID") variantUID: String?,
        @RequestParam(required = false, name = "variant_name") name: String?
    ): String {
        return "TODO"
    }

}