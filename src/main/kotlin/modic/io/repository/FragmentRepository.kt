package modic.io.repository

import modic.io.model.Fragment
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.query.Param
import java.time.Instant

interface FragmentRepository : CrudRepository<Fragment, Long> {

    @Query("SELECT * FROM Fragment f WHERE f.variant_time = :variantTime limit :limit", nativeQuery = true)
    fun findFragmentByTimestamp(
        @Param("variantTime") variantTime: Instant,
        @Param("limit") limit: Int): List<Fragment>

    @Query("SELECT * FROM Fragment f WHERE f.variantID = :variantID limit :limit", nativeQuery = true)
    fun findFragmentByVariantID(
        @Param("variantID") variantID: String,
        @Param("limit") limit: Int): List<Fragment>

    @Query("SELECT * FROM Fragment f WHERE f.variant_name = :variantName limit :limit", nativeQuery = true)
    fun findFragmentByVariantName(
        @Param("variantName") variantName: String,
        @Param("limit") limit: Int): List<Fragment>

    @Query("SELECT DISTINCT ON (variantID) * FROM Fragment f limit :limit",
        nativeQuery = true)
    fun findOneFragmentOfEachVariant(@Param("limit") limit: Int): List<Fragment>

}
