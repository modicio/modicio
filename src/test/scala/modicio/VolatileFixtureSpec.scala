package modicio

import modicio.codi.fixtures.VolatilePersistentRegistryFixture
import org.scalatest.FutureOutcome
import org.scalatest.flatspec.FixtureAsyncFlatSpec
import org.scalatest.matchers.should

abstract class VolatileFixtureSpec extends FixtureAsyncFlatSpec with should.Matchers{
  type FixtureParam = VolatilePersistentRegistryFixture

  override def withFixture(test: OneArgAsyncTest): FutureOutcome = {
    val theFixture = new FixtureParam()

    complete {
      super.withFixture(test.toNoArgAsyncTest(theFixture))
    } lastly {

    }
  }
}
