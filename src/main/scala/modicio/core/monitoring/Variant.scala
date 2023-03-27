package modicio.core.monitoring

import scala.collection.mutable.ListBuffer

/*
	A variant contains an unique variantId, variantTime and a list of versions.
 */
class Variant(var variantTime: Long, var variantId: String) {
	var versions: ListBuffer[Version] = new ListBuffer[Version]
	
	def addVersion(versionId: String, versionTime: Long): Version = {
		val newVersion = new Version(versionId, versionTime)
		versions :+= newVersion
		newVersion
	}
	
	def deleteVersion(versionId: String): Unit = {
		versions = versions.filter(_.versionId != versionId)
	}
	
	def getVersion(versionId: String): Option[Version] = versions.find(v => v.versionId == versionId)
	
	override def toString(): String = "variant: " + variantId + ", "+ variantTime
}
