package modic.io.repository

import modic.io.model.Fragment
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.query.Param
import java.time.Instant

interface FragmentRepository : CrudRepository<Fragment, Long> {

    @Query("SELECT * FROM Fragment f WHERE f.variant_time = :variantTime limit 1", nativeQuery = true)
    fun findFirstFragmentByTimestamp(
        @Param("variantTime") variantTime: Instant): Fragment?

    @Query("SELECT * FROM Fragment f WHERE f.variantID = :variantID limit 1", nativeQuery = true)
    fun findFirstFragmentByVariantID(
        @Param("variantID") variantID: String): Fragment?

}
