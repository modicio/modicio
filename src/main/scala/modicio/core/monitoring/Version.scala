package modicio.core.monitoring

import io.circe.generic.JsonCodec

import scala.collection.mutable
import scala.collection.mutable.ListBuffer

/*
	A Version includes an unique versionId, versionTime, a number of Instances and a list of  associations with another classes
 */
@JsonCodec
case class Version(var versionId: String,
									 var versionTime: Long,
									 var instances: ListBuffer[String] = new ListBuffer[String],
									 var associations: ListBuffer[String] = new ListBuffer[String],
									 var parentRelations: mutable.Map[String, String] = mutable.Map[String, String]()
									) {
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
	
	def addAssociated(identity: String): Unit = {
		if (!associations.contains(identity)) {
			associations :+= identity
		}
	}
	
	override def toString(): String = "version: " + versionId + ", " + associations.toString()
}
