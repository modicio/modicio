package modic.io.useCaseHelpers

import modic.io.model.*
import java.sql.Timestamp
import java.time.Instant
import java.util.*

class UseCase1Helper {

    companion object {

        fun getTestDataForward(
            preVariantID: String? = null,
            preRunningID: String? = null): Fragment {

            val variantTime = Timestamp.from(Instant.now())
            val variantId = preVariantID ?: UUID.randomUUID().toString()
            val runningId = preRunningID ?: UUID.randomUUID().toString()


            val model = Model(1, HashSet<Node>())

            return Fragment(
                0, null,false, "use case 1f", variantTime, variantId, Timestamp.from(Instant.now()), runningId,
                false, model, null
            )
        }

        fun getTestDataBackward(
            preVariantID: String? = null,
            preRunningID: String? = null): Fragment {

            val variantTime = Timestamp.from(Instant.now())
            val variantId = preVariantID ?: UUID.randomUUID().toString()
            val runningId = preRunningID ?: UUID.randomUUID().toString()

            val node = Node(
                0, "Alpha", "modicio:Alpha", false,
                Annotation(0, Timestamp.from(Instant.now()), UUID.randomUUID().toString(), variantTime, variantId),
                LinkedList<Attribute>(),
                LinkedList<AssociationRelation>(),
                HashSet<ParentRelation>(),
                HashSet<Plugin>(),
                HashSet<Concretization>(),
                HashSet<Composition>(),
                LinkedList<Script>()
            )

            node.addAttribute(Attribute(0, "modicio:Beta", "Beta", "String"))


            val model = Model(1, HashSet<Node>())

            model.addNode(node)

            return Fragment(
                0, null,false, "use case 1b", variantTime, variantId, Timestamp.from(Instant.now()), runningId,
                false, model, null
            )
        }

    }
}