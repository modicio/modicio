/**
 * Copyright 2023 Minji Kim
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

package modicio.codi

import modicio.FixtureForMonitoringSpec
import modicio.core.ModelElement
import modicio.core.rules.{AssociationRule, ConnectionInterface, ParentRelationRule}

import scala.concurrent.Future

class MonitoringPerformanceSpec extends FixtureForMonitoringSpec {

	"Monitoring" must "add types and instances" in { fixture => {
		val newRule = AssociationRule.create("dueBy", fixture.DEADLINE, ConnectionInterface.parseInterface(fixture.TIME_IDENTITY.variantTime.toString, fixture.DEADLINE))
		fixture.importProjectSetupFromFile("model_02.json") flatMap (_ =>
			for {
				todoInstance1 <- fixture.registry.instanceFactory.newInstance("Todo")
				_ <- todoInstance1.unfold()
				typeOption <- fixture.registry.getType(fixture.TODO, ModelElement.REFERENCE_IDENTITY)
				todo <- typeOption.get.unfold()
				_ <- Future(todo.applyRule(newRule))
				_ <- todo.commit()
				todoInstance2 <- fixture.registry.instanceFactory.newInstance("Todo")
				_ <- todoInstance2.unfold()
			} yield {
				println(fixture.registry.produceJson())
				fixture.registry.classes.foreach(c => c.variants.length should be(1))
				fixture.registry.classes.find(c => c.typeName == "Todo").get.variants.last.versions.length should be (2)
				fixture.registry.classes.length should be(4)
			}
			)
	}
	}

}
