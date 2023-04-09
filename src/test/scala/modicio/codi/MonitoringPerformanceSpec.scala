package modicio.codi

import modicio.FixtureIntegrationForMonitoringSpec
import modicio.core.ModelElement
import modicio.core.rules.ParentRelationRule

import scala.concurrent.Future

class MonitoringPerformanceSpec extends FixtureIntegrationForMonitoringSpec {

	"Monitoring" must "add types and instances" in { fixture => {
		fixture.initProjectSetup() flatMap (_ =>
			for {
				todoInstance1 <- fixture.registry.instanceFactory.newInstance("Todo")
				todoInstance2 <- fixture.registry.instanceFactory.newInstance("Todo")
				projectInstance1 <- fixture.registry.instanceFactory.newInstance("Project")
				_ <- todoInstance1.unfold()
				_ <- todoInstance2.unfold()
				_ <- projectInstance1.unfold()
			} yield {
				println(fixture.registry.produceJson())
				fixture.registry.classes.length should be(2)
			}
			)
	}
	}
	
	"Monitoring" must "..." in { fixture => {
		fixture.initProjectSetup() flatMap (_ =>
			for {
				typeOption <- fixture.registry.getType(fixture.SPECIAL_TODO, ModelElement.REFERENCE_IDENTITY)
				specialTodo <- typeOption.get.unfold()
				typeOption <- fixture.registry.getType(fixture.TODO, ModelElement.REFERENCE_IDENTITY)
				todo <- typeOption.get.unfold()
				_ <- Future(specialTodo.applyRule(ParentRelationRule.create(todo.getTypeName, todo.getTypeIdentity)))
				_ <- specialTodo.commit()
				typeOption <- fixture.registry.getType(fixture.SPECIAL_TODO, ModelElement.REFERENCE_IDENTITY)
				_ <- typeOption.get.unfold()
				todoInstance1 <- fixture.registry.instanceFactory.newInstance("Todo")
				todoInstance2 <- fixture.registry.instanceFactory.newInstance("Todo")
				todoInstance3 <- fixture.registry.instanceFactory.newInstance("Todo")
				specialtodoInstance1 <- fixture.registry.instanceFactory.newInstance("SpecialTodo")
				_ <- todoInstance1.unfold()
				_ <- todoInstance2.unfold()
				_ <- todoInstance3.unfold()
				_ <- specialtodoInstance1.unfold()
			} yield {
				println(fixture.registry.produceJson())
				fixture.registry.classes.isEmpty should be(false)
			}
			)
	}
	}
	
	"Monitoring" must "delete knowledge longer than 7 days" in { fixture => {
		fixture.importProjectSetupFromFile("model_02.json") flatMap (_ =>
			for {
				todoInstance1 <- fixture.registry.instanceFactory.newInstance("Todo")
				todoInstance2 <- fixture.registry.instanceFactory.newInstance("Todo")
				projectInstance1 <- fixture.registry.instanceFactory.newInstance("Project")
				_ <- todoInstance1.unfold()
				_ <- todoInstance2.unfold()
				_ <- projectInstance1.unfold()
			} yield {
				println(fixture.registry.produceJson())
				fixture.registry.classes.isEmpty should be(true)
			}
			)
	}
	}
}
