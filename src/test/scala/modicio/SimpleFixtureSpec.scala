package modicio

import modicio.codi.fixtures.SimpleMapRegistryFixture
import org.scalatest.FutureOutcome
import org.scalatest.flatspec.FixtureAsyncFlatSpec
import org.scalatest.matchers.should

class SimpleFixtureSpec extends FixtureAsyncFlatSpec with should.Matchers{
  type FixtureParam = SimpleMapRegistryFixture

  override def withFixture(test: OneArgAsyncTest): FutureOutcome = {
    val theFixture = new FixtureParam

    complete {
      super.withFixture(test.toNoArgAsyncTest(theFixture))
    } lastly {

    }
  }
}
