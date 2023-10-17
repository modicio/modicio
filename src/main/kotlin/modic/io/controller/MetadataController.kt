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
import java.time.Instant

@RestController
class MetadataController(val metadataService: MetadataService) {

    /**
     * Get the metadata of all known variants.
     * The result size can be delimited to the k most recently created variants.
     * The default value for delimiter is set to 1000
     *
     * URL Params
     *   - `delimiter?=INT`
     *
     * Returns
     *   - JSON Body
     *
     * @param delimiter
     */
    @GetMapping("model/metadata/variants", produces=[MediaType.APPLICATION_JSON_VALUE])
    fun getVariantsMetadata(
        @RequestParam(required = false, name = "delimiter") delimiter: Int = 1000,
        @RequestParam(required = false, name = "closed_only") closedOnly: Boolean = true
    ): List<MetaData> {
        return metadataService.getAllVariantsMetadata(delimiter, true)
    }

    /**
     * Get the complete metadata of a variant. At least one of the URL params must be provided.
     * If the URL params are not sufficient to determine a variant, an error is thrown.
     * This returns the first variants that are found matching one of the parameters in order. Please look at the
     * behaviour of [MetadataService.getVariantMetadata] for more details.
     *
     * URL Params
     *   - `variant_timestamp?=STRING`
     *   - `variant_UUID?=String`
     *   - `variant_name?=String`
     *
     * Returns
     *   - JSON Body
     *
     * @param timestamp
     * @param variantUID
     * @param name
     */
    @GetMapping("model/metadata/variant", produces=[MediaType.APPLICATION_JSON_VALUE])
    fun getVariantMetadata(
        @RequestParam(required = false, name = "variant_timestamp") timestamp: String?,
        @RequestParam(required = false, name = "variant_UUID") variantUID: String?,
        @RequestParam(required = false, name = "variant_name") name: String?,
        @RequestParam(required = false, name = "limit") limit: Int = 1,
        @RequestParam(required = false, name = "closed_only") closedOnly: Boolean = true
    ): List<MetaData> {

        if(timestamp == null && variantUID == null && name == null){
            //TODO return error
        }

        val metadata = metadataService.getVariantMetadata(Instant.parse(timestamp), variantUID, name, limit, closedOnly)

        if(metadata == null){
            //TODO return error code
        }

        return metadata
    }

    /**
     * Get the metadata of all known running versions of the specified variant.
     * The response size can be delimited to the k most recent versions.
     * The default value for delimiter is set to 1000
     *
     * URL Params
     *   - `variant_UUID=String`
     *   - `delimiter?=INT`
     *
     * Returns
     *   - JSON Body
     *
     *  @param variantID
     *  @param delimiter
     */
    @GetMapping("model/metadata/variant/versions", produces=[MediaType.APPLICATION_JSON_VALUE])
    fun getVersionsOfVariantMetadata(
        @RequestParam(required = true, name = "variant_UUID") variantID: String,
        @RequestParam(required = false, name = "delimiter") delimiter: Int = 1000,
        @RequestParam(required = false, name = "closed_only") closedOnly: Boolean = true
    ): List<MetaData> {
        return metadataService.getAllRunningVersionsOfVariant(variantID, delimiter, closedOnly)
    }

}