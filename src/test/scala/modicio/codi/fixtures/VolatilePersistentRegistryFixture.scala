package modicio.codi.fixtures

import modicio.RegistryFixture
import modicio.nativelang.defaults.{VolatilePersistentRegistry, VolatilePersistentRegistryCore}
import modicio.nativelang.util.AccessCounting

import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.Try

class VolatilePersistentRegistryFixture extends RegistryFixture with AccessCounting {
  val core: VolatilePersistentRegistryCore = new VolatilePersistentRegistryCore()
  val registry: VolatilePersistentRegistry = new VolatilePersistentRegistry(typeFactory, instanceFactory, core)
  typeFactory.setRegistry(registry)
  instanceFactory.setRegistry(registry)

  override def writeAccessCounts(fileName: String, path: String = "."): Try[Unit] = {
    registry.writeAccessCounts(fileName, path)
  }
}
