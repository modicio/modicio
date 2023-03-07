package modicio

import modicio.nativelang.defaults.{PersistentRegistryOptimization, VolatilePersistentRegistry, VolatilePersistentRegistryCore}

import scala.concurrent.ExecutionContext.Implicits.global

class OptimizedRegistryFixture extends RegistryFixture {
  val core: VolatilePersistentRegistryCore = new VolatilePersistentRegistryCore()
  val internalRegistry = new VolatilePersistentRegistry(typeFactory, instanceFactory, core)
  val registry: PersistentRegistryOptimization = new PersistentRegistryOptimization(internalRegistry)
  typeFactory.setRegistry(registry)
  instanceFactory.setRegistry(registry)
}
