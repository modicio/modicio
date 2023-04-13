package modicio.codi.fixtures

import modicio.RegistryFixture
import modicio.core.monitoring.Monitoring
class MonitoringRegistryFixture extends RegistryFixture {
	val registry = new Monitoring(new SimpleMapRegistryFixture().registry, typeFactory, instanceFactory)
	typeFactory.setRegistry(registry)
	instanceFactory.setRegistry(registry)
}
