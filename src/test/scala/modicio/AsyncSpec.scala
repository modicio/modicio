package modicio

import modicio.codi.fixtures.{OptimizedRegistryFixture, SimpleMapRegistryFixture, VolatilePersistentRegistryFixture}
import org.scalatest.flatspec.{AnyFlatSpec, AsyncFlatSpec}
import org.scalatest.matchers.should

abstract class AsyncSpec extends AsyncFlatSpec with should.Matchers{
  def volatilePersistentRegistry = {
    new VolatilePersistentRegistryFixture
  }

  def simpleMapRegistry = {
    new SimpleMapRegistryFixture
  }

  def cachedRegistry = {
    new OptimizedRegistryFixture
  }
}
