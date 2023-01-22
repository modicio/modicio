package modicio

import modicio.nativelang.defaults.VolatilePersistentRegistry
import scala.concurrent.ExecutionContext.Implicits.global

class VolatilePersistentRegistryFixture extends RegistryFixture {
  val registry: VolatilePersistentRegistry = new VolatilePersistentRegistry(typeFactory, instanceFactory)
  typeFactory.setRegistry(registry)
  instanceFactory.setRegistry(registry)
}
