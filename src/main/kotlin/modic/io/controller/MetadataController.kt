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
import modic.io.messages.MetaData
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
class MetadataController(val metadataService: MetadataService) {

    /**
     * Get the metadata of all known variants.
     * The result size can be delimited to the k most recently created variants.
     */
    @GetMapping("model/metadata/variants", produces=[MediaType.APPLICATION_JSON_VALUE])
    fun getVariantsMetadata(
        @RequestParam(required = false, name = "delimiter") delimiter: Int?
    ): List<MetaData> {
        //TODO
        return listOf(MetaData("foo", "bar", "baz"))
    }

    /**
     * Get the complete metadata of a variant. At least one of the URL params must be provided.
     * If the URL params are not sufficient to determine a variant, an error code is thrown.
     */
    @GetMapping("model/metadata/variant", produces=[MediaType.APPLICATION_JSON_VALUE])
    fun getVariantMetadata(
        @RequestParam(required = false, name = "variant_timestamp") timestamp: String?,
        @RequestParam(required = false, name = "variant_UUID") variantUID: String?,
        @RequestParam(required = false, name = "variant_name") name: String?
    ): MetaData {
       //TODO
        return MetaData("foo", "bar", "baz")
    }

    /**
     * Get the metadata of all known running versions of the specified variant.
     * The response size can be delimited to the k most recent versions.
     */
    @GetMapping("model/metadata/variant", produces=[MediaType.APPLICATION_JSON_VALUE])
    fun getVersionsOfVariantMetadata(
        @RequestParam(required = true, name = "variant_timestamp") timestamp: String,
        @RequestParam(required = true, name = "variant_UUID") variantUID: String,
        @RequestParam(required = false, name = "delimiter") delimiter: Int?
    ): List<MetaData> {
        //TODO
        return listOf(MetaData("foo", "bar", "baz"))
    }

}