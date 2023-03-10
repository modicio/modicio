package modicio

import modicio.codi.fixtures.{OptimizedRegistryFixture, SimpleMapRegistryFixture, VolatilePersistentRegistryFixture}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should

class Spec extends AnyFlatSpec with should.Matchers{
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
