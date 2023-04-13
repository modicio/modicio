package modicio.core.monitoring

import io.circe.generic.JsonCodec

import scala.collection.mutable

/*
	A Version includes an unique versionId, versionTime, a number of Instances and a list of  associations with another classes
 */
@JsonCodec
case class Version(var versionId: String,
									 var versionTime: Long,
									 var instances: mutable.Set[Set[String]] = mutable.Set[Set[String]](),
									 var associations: mutable.Set[Set[String]] = mutable.Set[Set[String]](),
									 var parentRelations: mutable.Set[Set[String]] = mutable.Set[Set[String]](),
									) {
	def increase(instanceId: String, instanceTime: Long): Unit = {
		val newInstance = Set(instanceId, instanceTime.toString)
		if (!this.instances.exists(i => i.contains(instanceId))) {
			instances.addOne(newInstance)
		}
	}
	
	def decrease(instance: Set[String]): Unit = {
		this.instances = instances.filter(i => !i.equals(instance))
	}
	
	def addParentRelations(parentName: String, parentIdentity: String): Unit = {
		val newParentRelation = Set(parentName, parentIdentity)
		if (!parentRelations.contains(newParentRelation)) {
			parentRelations.addOne(newParentRelation)
		}
	}
	
	def addAssociated(typeName: String, variantId: String, versionId: String): Unit = {
		val newAssociation = Set(typeName, variantId, versionId)
		if (!associations.contains(newAssociation)) {
			associations.addOne(newAssociation)
		}
	}
}
