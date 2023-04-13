package modicio.codi

import modicio.FixtureForMonitoringSpec
import modicio.core.ModelElement
import modicio.core.rules.{AssociationRule, ConnectionInterface, ParentRelationRule}

import scala.concurrent.Future

class MonitoringPerformanceSpec extends FixtureForMonitoringSpec {

	"Monitoring" must "add types and instances" in { fixture => {
		val newRule = AssociationRule.create("dueBy", fixture.DEADLINE, fixture.SINGLE, ConnectionInterface.parseInterface(fixture.TIME_IDENTITY.variantTime.toString, fixture.DEADLINE))
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

	"Monitoring" must "delete knowledge longer than input" in { fixture => {
		fixture.importProjectSetupFromFile("model_02.json") flatMap (_ =>
			for {
				todoInstance1 <- fixture.registry.instanceFactory.newInstance("Todo")
				todoInstance2 <- fixture.registry.instanceFactory.newInstance("Todo")
				projectInstance1 <- fixture.registry.instanceFactory.newInstance("Project")
				_ <- todoInstance1.unfold()
				_ <- todoInstance2.unfold()
				_ <- projectInstance1.unfold()
			} yield {
				fixture.registry.deleteObsoleteKnowledge(0)
				fixture.registry.classes.isEmpty should be(true)
			}
			)
	}
	}
	
	"Monitoring" must "reduce knowledge size" in { fixture => {
		fixture.importProjectSetupFromFile("model_02.json") flatMap (_ =>
			for {
				todoInstance1 <- fixture.registry.instanceFactory.newInstance("Todo")
				todoInstance2 <- fixture.registry.instanceFactory.newInstance("Todo")
				projectInstance1 <- fixture.registry.instanceFactory.newInstance("Project")
				_ <- todoInstance1.unfold()
				_ <- todoInstance2.unfold()
				_ <- projectInstance1.unfold()
			} yield {
				fixture.registry.deleteObsoleteKnowledgeBySize(2)
				fixture.registry.classes.length should be(2)
			}
			)
	}
	}
}
