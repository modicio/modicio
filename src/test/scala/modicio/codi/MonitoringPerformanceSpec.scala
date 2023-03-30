package modicio.codi

import modicio.FixtureIntegrationForMonitoringSpec
import modicio.core.ModelElement

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
				fixture.registry.classes.length should be(2)
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
				println(fixture.registry.classes.toString())
				fixture.registry.classes.isEmpty should be(true)
			}
			)
	}
	}
}
