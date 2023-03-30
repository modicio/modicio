package modicio.codi.fixtures

import modicio.RegistryFixture
import modicio.nativelang.defaults.SimpleMapRegistry

class SimpleMapRegistryFixture extends RegistryFixture {
  val registry: SimpleMapRegistry = new SimpleMapRegistry (typeFactory, instanceFactory)
  typeFactory.setRegistry(registry)
  instanceFactory.setRegistry(registry)
}
