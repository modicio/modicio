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

package modic.io.repository

import modic.io.model.Fragment
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface InstanceRepository: JpaRepository<Fragment, Long> {

    @Query(
        "SELECT f.dataid from Fragment f LEFT JOIN instance i on i.dataid = f.instance_dataid \n" +
                "WHERE i.model_root like :type_uri_pattern limit :limit"
        , nativeQuery = true
    )
    fun getInstancesByTypeURISoftMatch(
        @Param("type_uri_pattern") typeURIPattern: String,
        @Param("limit") limit: Int
    ): List<Long>

    @Query(
        "SELECT f.dataid from Fragment f LEFT JOIN instance i on i.dataid = f.instance_dataid \n" +
                "WHERE i.model_root like :type_uri_pattern AND f.variantid = :variantID limit :limit"
        , nativeQuery = true
    )
    fun getInstancesByTypeNameSoftMatchAndVariant(
        @Param("type_uri_pattern") typeURIPattern: String,
        @Param("variantID") variantID: String,
        @Param("limit") limit: Int
    ): List<Long>

    @Query(
        "SELECT f.dataid from Fragment f LEFT JOIN instance i on i.dataid = f.instance_dataid \n" +
                "WHERE i.model_root like :type_uri_pattern AND f.variantid = :variantID \n" +
                "AND f.runningid = :versionID limit :limit"
        , nativeQuery = true
    )
    fun getInstancesByTypeNameSoftMatchAndVariantAndVersion(
        @Param("type_uri_pattern") typeURIPattern: String,
        @Param("variantID") variantID: String,
        @Param("versionID") versionID: String,
        @Param("limit") limit: Int
    ): List<Long>
}