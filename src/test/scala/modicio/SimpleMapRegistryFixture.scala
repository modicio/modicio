package modicio

import modicio.nativelang.defaults.SimpleMapRegistry
import scala.concurrent.ExecutionContext.Implicits.global

class SimpleMapRegistryFixture extends RegistryFixture {
  val registry: SimpleMapRegistry = new SimpleMapRegistry (typeFactory, instanceFactory)
  typeFactory.setRegistry(registry)
  instanceFactory.setRegistry(registry)
}
