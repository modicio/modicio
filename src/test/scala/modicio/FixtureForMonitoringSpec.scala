package modicio

import modicio.codi.fixtures.MonitoringRegistryFixture
import org.scalatest.FutureOutcome
import org.scalatest.flatspec.FixtureAsyncFlatSpec
import org.scalatest.matchers.should

class FixtureForMonitoringSpec extends FixtureAsyncFlatSpec with should.Matchers {
	type FixtureParam = MonitoringRegistryFixture
	
	override def withFixture(test: OneArgAsyncTest): FutureOutcome = {
		val theFixture = new FixtureParam()
		
		complete {
			super.withFixture(test.toNoArgAsyncTest(theFixture))
		} lastly {
		
		}
	}
}
