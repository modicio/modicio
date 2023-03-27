package modicio.core.monitoring

import modicio.core.TypeHandle

import scala.collection.mutable

/*
	A Version includes an unique versionId, versionTime, a number of Instances and a list of  associations with another classes
 */
class Version(var versionId: String, var versionTime: Long) {
	var instances: List[String] = _
	var associations: List[String] = _
	var parentRelations: mutable.Map[String, String] = _
	def increase(instanceId: String): Unit = {
		if (!this.instances.contains(instanceId)) {
			instances :+= instanceId
		}
	}
	
	def addParentRelations(parentName: String, parentIdentity: String): Unit = {
		if (!parentRelations.contains(parentName)) {
			parentRelations.addOne(parentName, parentIdentity)
		}
	}
	
	def addAssocaited(identity: String): Unit = {
		if (!associations.contains(identity)) {
			associations :+= identity
		}
	}
	
}
