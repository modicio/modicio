package modic.io.repository

import jakarta.persistence.Transient
import modic.io.model.Fragment
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.query.Param
import java.time.Instant

interface FragmentRepository : JpaRepository<Fragment, Long> {

    @Query("SELECT * FROM Fragment f WHERE f.variant_time = :variantTime limit :limit", nativeQuery = true)
    fun findFragmentByTimestampLazy(
        @Param("variantTime") variantTime: Instant,
        @Param("limit") limit: Int): List<Fragment>

    @Query("SELECT * FROM Fragment f WHERE f.variantID = :variantID limit :limit", nativeQuery = true)
    fun findFragmentByVariantIDLazy(
        @Param("variantID") variantID: String,
        @Param("limit") limit: Int): List<Fragment>

    @Query("SELECT * FROM Fragment f WHERE f.variant_name = :variantName limit :limit", nativeQuery = true)
    fun findFragmentByVariantNameLazy(
        @Param("variantName") variantName: String,
        @Param("limit") limit: Int): List<Fragment>

    @Query("SELECT DISTINCT ON (variantID) * FROM Fragment f limit :limit",
        nativeQuery = true)
    fun findOneFragmentOfEachVariantLazy(@Param("limit") limit: Int): List<Fragment>

    //Auto-generated
    fun findFragmentByIsReferenceIsTrue(): List<Fragment>

    //Auto-generated
    fun findFragmentByVariantID(variantID: String): Fragment?

    //Auto-generated
    fun findFragmentByVariantTime(variantTime: Instant): List<Fragment>

    //Auto-generated
    fun findFragmentByVariantIDAndVariantTime(variantID: String, variantTime: Instant): Fragment?

    @Transient
    fun getFragmentByIdentifiers(variantID: String?, variantTime: String?): Fragment? {
        var fragment: Fragment? = null
        if(variantID != null){
            fragment = if(variantTime != null){
                findFragmentByVariantIDAndVariantTime(variantID, Instant.parse(variantTime))
            }else{
                findFragmentByVariantID(variantID)
            }
        }else if(variantTime != null){
            fragment = findFragmentByVariantTime( Instant.parse(variantTime)).firstOrNull()
        }
        return fragment
    }

}
