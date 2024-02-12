package modic.io.useCaseHelpers

import modic.io.model.*
import modic.io.model.Annotation
import java.sql.Timestamp
import java.time.Instant
import java.util.*

class UseCase2Helper {

    companion object {

        fun getTestDataForward(
            preVariantID: String? = null,
            preRunningID: String? = null): Fragment {

            val variantTime = Timestamp.from(Instant.now())
            val variantId = preVariantID ?: UUID.randomUUID().toString()
            val runningId = preRunningID ?: UUID.randomUUID().toString()

            val model = Model(2, HashSet<Node>())

            val node1 = Node(name = "Gamma", uri = "modicio:Gamma")

            val node2 = Node(name = "Delta", uri = "modicio:Delta")

            val node3 = Node(name = "Epsilon", uri = "modicio:Epsilon")

            model.addNode(node1)
            model.addNode(node2)
            model.addNode(node3)

            return Fragment(
                1, null,false, "use case 2f", variantTime, variantId, Timestamp.from(Instant.now()), runningId,
                false, model, null
            )
        }

        fun getTestDataBackward(
            preVariantID: String? = null,
            preRunningID: String? = null): Fragment {

            val variantTime = Timestamp.from(Instant.now())
            val variantId = preVariantID ?: UUID.randomUUID().toString()
            val runningId = preRunningID ?: UUID.randomUUID().toString()

            val model = Model(3, HashSet<Node>())

            val node1 = Node(name = "Gamma", uri = "modicio:Gamma")

            val node2 = Node(name = "Delta", uri = "modicio:Delta")

            val node3 = Node(name = "Epsilon", uri = "modicio:Epsilon")

            node1.addComposition(Composition(uri = "modicio:GammaDeltaComposition", role = "GammaDeltaComposition",
                target = "modicio:Delta", isPublic = true))

            node1.addAssociationRelation(
                AssociationRelation(
                    0, "modicio:GammaEpsilonAssociation", "GammaEpsilonAssociation", "modicio:Epsilon",
                    Interface(0)
                ))

            node1.getAssociationRelations().first().cInterface!!.addPointDelimiter(
                Point(0, variantTime, variantId, Timestamp.from(Instant.now()))
            )
            node1.getAssociationRelations().first().cInterface!!.addOLeftOpenDelimiter(LeftOpen(borderVersionTime = Timestamp.from(Instant.now())))

            model.addNode(node1)
            model.addNode(node2)
            model.addNode(node3)

            return Fragment(
                2, null,false, "use case 2b", variantTime, variantId, Timestamp.from(Instant.now()), runningId,
                false, model, null
            )
        }
    }
}