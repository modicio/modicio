package modic.io.useCaseHelpers

import modic.io.model.*
import java.sql.Timestamp
import java.time.Instant
import java.util.*

class UseCase5Helper {

    companion object {

        fun getTestDataForward(
            preVariantID: String? = null,
            preRunningID: String? = null): Fragment {

            val variantTime = Timestamp.from(Instant.now())
            val variantId = preVariantID ?: UUID.randomUUID().toString()
            val runningId = preRunningID ?: UUID.randomUUID().toString()

            val model = Model(5, HashSet<Node>())

            val node1 = Node(name = "Alpha", uri = "modicio:Alpha")

            model.addNode(node1)

            return Fragment(
                8, null,false, "use case 5f", variantTime, variantId, Timestamp.from(Instant.now()), runningId,
                false, model, null
            )
        }

        fun getTestDataBackward(
            preVariantID: String? = null,
            preRunningID: String? = null): Fragment {

            val variantTime = Timestamp.from(Instant.now())
            val variantId = preVariantID ?: UUID.randomUUID().toString()
            val runningId = preRunningID ?: UUID.randomUUID().toString()

            val model = Model(6, HashSet<Node>())

            val node1 = Node(name = "Alpha", uri = "modicio:Alpha")

            val node2 = Node(name = "Zeta", uri = "modicio:Zeta", isAbstract = true)
            node2.addAttribute(Attribute(0, "modicio:Theta", "Theta", "Default"))
            node2.addParentRelation(ParentRelation(uri = "modicio:Alpha", node = node2))

            model.addNode(node1)
            model.addNode(node2)

            return Fragment(
                9, null,false, "use case 5b", variantTime, variantId, Timestamp.from(Instant.now()), runningId,
                false, model, null
            )
        }
    }
}