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