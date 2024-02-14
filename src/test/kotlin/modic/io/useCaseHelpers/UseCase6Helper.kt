package modic.io.useCaseHelpers

import modic.io.model.*
import java.sql.Timestamp
import java.time.Instant
import java.util.*

class UseCase6Helper {

    companion object {

        fun getTestDataForward(
            preVariantID: String? = null,
            preRunningID: String? = null): Fragment {

            val variantTime = Timestamp.from(Instant.now())
            val variantId = preVariantID ?: UUID.randomUUID().toString()
            val runningId = preRunningID ?: UUID.randomUUID().toString()

            val model = Model(6, HashSet<Node>())

            val node1 = Node(name = "Omega", uri = "modicio:Omega")

            model.addNode(node1)

            return Fragment(
                10, null,false, "use case 6f", variantTime, variantId, Timestamp.from(Instant.now()), runningId,
                false, model, null
            )
        }

        fun getTestDataBackward(
            preVariantID: String? = null,
            preRunningID: String? = null): Fragment {

            val variantTime = Timestamp.from(Instant.now())
            val variantId = preVariantID ?: UUID.randomUUID().toString()
            val runningId = preRunningID ?: UUID.randomUUID().toString()

            val model = Model(11, HashSet<Node>())

            val node1 = Node(name = "Omega", uri = "modicio:Omega")

            val node2 = Node(name = "Chi", uri = "modicio:Chi")

            node2.addAttribute(Attribute(0, "modicio:Psi", "Kappa", "String"))


            node2.addAssociationRelation(
                AssociationRelation(
                    0, "modicio:ChiOmegaAssociation", "ChiOmegaAssociation", "modicio:Omega",
                    Interface(0)
                )
            )

            node2.getAssociationRelations().first().cInterface!!.addIntervalDelimiter(Region(leftBorderVersionTime = Timestamp.valueOf("2022-10-01 12:00:00"),
                rightBorderVersionTime = Timestamp.valueOf("2022-11-10 12:00:00")))

            node2.getAssociationRelations().first().cInterface!!.addRightOpenDelimiter(RightOpen(borderVersionTime = Timestamp.valueOf("2021-09-08 12:00:00")))

            model.addNode(node1)
            model.addNode(node2)

            return Fragment(
                2, null,false, "use case 6b", variantTime, variantId, Timestamp.from(Instant.now()), runningId,
                false, model, null
            )
        }
    }
}