package modic.io.useCaseHelpers

import modic.io.model.Fragment
import modic.io.model.Model
import modic.io.model.Node
import java.sql.Timestamp
import java.time.Instant
import java.util.*

class UseCase3Helper {

    companion object {

        fun getTestDataForward(
            preVariantID: String? = null,
            preRunningID: String? = null): Fragment {

            val variantTime = Timestamp.from(Instant.now())
            val variantId = preVariantID ?: UUID.randomUUID().toString()
            val runningId = preRunningID ?: UUID.randomUUID().toString()

            val model = Model(2, HashSet<Node>())

            val node1 = Node(name = "Tau", uri = "modicio:Tau")

            model.addNode(node1)

            return Fragment(
                1, null,false, "use case 3f", variantTime, variantId, Timestamp.from(Instant.now()), runningId,
                false, model, null
            )
        }
    }
}