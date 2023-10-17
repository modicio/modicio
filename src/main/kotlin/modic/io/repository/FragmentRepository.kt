package modic.io.repository

import jakarta.persistence.Transient
import modic.io.model.Fragment
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.query.Param
import java.time.Instant

interface FragmentRepository : JpaRepository<Fragment, Long> {

    @Query("SELECT * from Fragment f WHERE f.variant_time = :variantTime " +
            "AND running_time = ( SELECT MAX(running_time) FROM Fragment f1 WHERE f1.variant_time = :variantTime)\n" +
            "ORDER BY running_time DESC\n" +
            "LIMIT :limit", nativeQuery = true)
    fun findMostRecentFragmentsByVariantTimeLazy(
        @Param("variantTime") variantTime: Instant,
        @Param("limit") limit: Int): List<Fragment>

    @Query("SELECT DISTINCT ON (running_time, variantID) * from Fragment f WHERE f.variantID = :variantID " +
            "AND running_time = ( SELECT MAX(running_time) FROM Fragment f1 WHERE f1.variantID = :variantID)\n" +
            "ORDER BY running_time DESC, variantID\n" +
            "LIMIT 1", nativeQuery = true)
    fun findMostRecentFragmentByVariantIDLazy(@Param("variantID") variantID: String): Fragment?

    @Query("SELECT  DISTINCT ON (runing_time, variantID) f.* from Fragment f WHERE f.variant_name = :variantName " +
            "AND running_time = ( SELECT MAX(running_time) FROM Fragment f1 WHERE f1.variant_name = :variantName)\n" +
            "ORDER BY running_time DESC, variantID\n" +
            "LIMIT :limit", nativeQuery = true)
    fun findMostRecentFragmentsByVariantNameLazy(
        @Param("variantName") variantName: String,
        @Param("limit") limit: Int): List<Fragment>

    @Query("SELECT DISTINCT ON (variantID) * FROM Fragment f \n" +
            "limit :limit",
        nativeQuery = true)
    fun findOneFragmentOfEachVariantLazy(@Param("limit") limit: Int): List<Fragment>

    @Query("SELECT f.* from Fragment f\n" +
            "WHERE f.variantID = :variantID AND f.runningID = :runningID AND f.instance_dataid is NULL",
        nativeQuery = true)
    fun findModelOnlyFragmentWithVariantAndRunningIDFirstLazy(
        @Param("variantID") variantID: String,
        @Param("runningID") runningID: String): Fragment?

    @Query("SELECT DISTINCT ON (runningid) * from Fragment WHERE variantid = :variantID \n" +
            "ORDER BY runningid DESC\n" +
            "LIMIT :limit",
        nativeQuery = true)
    fun findAllRunningVersionsOfVariant(
        @Param("variantID") variantID: String,
        @Param("limit") limit: Int): List<Fragment>

    //Auto-generated
    fun getFragmentByDataID(dataID: Long): Fragment?

    //Auto-generated
    fun getFragmentByRunningID(runningID: String): List<Fragment>

    fun getFragmentsByRunningTime(runningTime: Instant): List<Fragment>

    //Auto-generated
    fun findFragmentByIsReferenceIsTrue(): List<Fragment>

    //Auto-generated
    fun findFragmentByVariantID(variantID: String): List<Fragment>

    //Auto-generated
    fun findFragmentByVariantTime(variantTime: Instant): List<Fragment>

    //Auto-generated
    fun findFragmentByVariantIDAndVariantTime(variantID: String, variantTime: Instant): List<Fragment>

}
