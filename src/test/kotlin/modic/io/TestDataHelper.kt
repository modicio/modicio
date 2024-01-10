/**
 * Copyright 2023 Karl Kegel
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package modic.io

import modic.io.model.*
import modic.io.model.Annotation
import java.sql.Timestamp
import java.time.Instant
import java.util.*

class TestDataHelper {

    companion object {

        fun getSimpleFragmentOnlyModel(
            preVariantID: String? = null,
            preRunningID: String? = null): Fragment {

            val variantTime = Timestamp.from(Instant.now())
            val variantId = preVariantID ?: UUID.randomUUID().toString()
            val runningId = preRunningID ?: UUID.randomUUID().toString()

            val trace = Trace(null, LinkedList<Delta>())

            val d1 = Delta(null, "foo", "t1")
            val d2 = Delta(null, "bar", "t2")
            trace.addDelta(d1)
            trace.addDelta(d2)

            val model = Model(0, HashSet<Node>())

            val node1 = Node(
                0, "Todo", "modicio:demo.todo", false,
                Annotation(0, Timestamp.from(Instant.now()), UUID.randomUUID().toString(), variantTime, variantId),
                LinkedList<Attribute>(),
                LinkedList<AssociationRelation>(),
                HashSet<ParentRelation>(),
                HashSet<Plugin>(),
                HashSet<Concretization>(),
                HashSet<Composition>(),
                LinkedList<Script>()
            )

            node1.addAttribute(Attribute(0, "modicio:demo.todo.Title", "Title", "string"))
            node1.addAttribute(Attribute(0, "modicio:demo.todo.Due", "Due", "datetime"))

            val node2 = Node(
                0, "Project", "modicio:demo.project", false,
                Annotation(0, Timestamp.from(Instant.now()), UUID.randomUUID().toString(), variantTime, variantId),
                LinkedList<Attribute>(),
                LinkedList<AssociationRelation>(),
                HashSet<ParentRelation>(),
                HashSet<Plugin>(),
                HashSet<Concretization>(),
                HashSet<Composition>(),
                LinkedList<Script>()
            )

            node2.addAttribute(Attribute(0, "modicio:demo.project.Description", "Description", "string"))

            node2.addAssociationRelation(
                AssociationRelation(
                0, "modicio:demo.project.related", "Related Todos", "modicio:demo.todo",
                    Interface(0)
            ))
            node2.getAssociationRelations().first().cInterface!!.addPointDelimiter(
                Point(0, variantTime, variantId)
            )

            model.addNode(node1)
            model.addNode(node2)

            return Fragment(
                0, null,false, "main", variantTime, variantId, Timestamp.from(Instant.now()), runningId,
                false, model, null, trace
            )
        }

        fun getFragmentForPredefinedFunctions(): Fragment {
            val variantTime = Timestamp.from(Instant.now())
            val variantId = UUID.randomUUID().toString()
            val runningId = UUID.randomUUID().toString()

            val model = Model(0, HashSet<Node>())
            val node1 = Node(
                0, "Todo", "modicio:demo.task", false,
                Annotation(0, Timestamp.from(Instant.now()), UUID.randomUUID().toString(), variantTime, variantId),
                LinkedList<Attribute>(),
                LinkedList<AssociationRelation>(),
                HashSet<ParentRelation>(),
                HashSet<Plugin>(),
                HashSet<Concretization>(),
                HashSet<Composition>(),
                LinkedList<Script>()
            )
            node1.addAttribute(Attribute(0, "modicio:demo.task.Title", "Title", "string"))
            node1.addAttribute(Attribute(0, "modicio:demo.task.StartTime", "StartTime", "datetime"))
            node1.addAttribute(Attribute(0, "modicio:demo.task.EndTime", "EndTime", "datetime"))
            node1.addAttribute(Attribute(0, "modicio:demo.task.Deadline", "Deadline", "datetime"))
            node1.addAttribute(Attribute(0, "modicio:demo.task.IsDeadLineCrossed", "IsDeadLineCrossed", "boolean"))
            model.addNode(node1)

            return Fragment(
                0, null,false, "main", variantTime, variantId, Timestamp.from(Instant.now()), runningId,
                false, model, null, Trace(null, LinkedList<Delta>())
            )
        }


    }

}