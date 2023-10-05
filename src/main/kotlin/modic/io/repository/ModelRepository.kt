package modic.io.repository

import modic.io.model.Fragment
import modic.io.model.Model
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.query.Param

interface ModelRepository : CrudRepository<Model, Long> {

    @Query("SELECT DISTINCT ON (m.runningid) m.* FROM Model m \n" +
            "LEFT JOIN Fragment f ON m.dataid = f.model_dataid\n" +
            "WHERE f.variantid = :variantID\n" +
            "LIMIT :limit",
        nativeQuery = true)
    fun findAllRunningVersionsOfVariant(
        @Param("variantID") variantID: String,
        @Param("limit") limit: Int): List<Model>

}