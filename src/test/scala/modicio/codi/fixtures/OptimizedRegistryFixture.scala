package modicio.codi.fixtures

import modicio.RegistryFixture
import modicio.nativelang.defaults.{PersistentRegistryOptimization, VolatilePersistentRegistry, VolatilePersistentRegistryCore}
import modicio.nativelang.util.AccessCounting

import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.Try

class OptimizedRegistryFixture extends RegistryFixture with AccessCounting {
  val core: VolatilePersistentRegistryCore = new VolatilePersistentRegistryCore()
  val internalRegistry = new VolatilePersistentRegistry(typeFactory, instanceFactory, core)
  val registry: PersistentRegistryOptimization = new PersistentRegistryOptimization(internalRegistry)
  typeFactory.setRegistry(registry)
  instanceFactory.setRegistry(registry)

  override def writeAccessCounts(fileName: String, path: String = "."): Try[Unit] = {
    internalRegistry.writeAccessCounts(fileName, path)
  }
}
