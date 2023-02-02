package modicio

import modicio.nativelang.defaults.{VolatilePersistentRegistry, VolatilePersistentRegistryCore}

import scala.concurrent.ExecutionContext.Implicits.global

class VolatilePersistentRegistryFixture extends RegistryFixture {
  val core: VolatilePersistentRegistryCore = new VolatilePersistentRegistryCore()
  val registry: VolatilePersistentRegistry = new VolatilePersistentRegistry(typeFactory, instanceFactory, core)
  typeFactory.setRegistry(registry)
  instanceFactory.setRegistry(registry)
}
