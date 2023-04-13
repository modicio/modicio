package modicio

import modicio.core.monitoring.Monitoring
import modicio.SimpleMapRegistryFixture
class MonitoringRegistryFixture extends RegistryFixture {
	val simpleMapRegistryFixture = new SimpleMapRegistryFixture
	val registry = new Monitoring(simpleMapRegistryFixture.registry, typeFactory, instanceFactory)
	typeFactory.setRegistry(registry)
	instanceFactory.setRegistry(registry)
}
