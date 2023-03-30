package modicio.core.monitoring

import io.circe.generic.JsonCodec

import scala.collection.mutable.ListBuffer

/*
	A variant contains an unique variantId, variantTime and a list of versions.
 */
@JsonCodec
case class Variant(var variantTime: Long, var variantId: String, var versions: ListBuffer[Version] = new ListBuffer[Version]) {
	
	def addVersion(versionId: String, versionTime: Long): Version = {
		val newVersion = Version(versionId, versionTime)
		versions :+= newVersion
		newVersion
	}
	
	def deleteVersion(version: Version): Unit = {
		versions = versions.filter(_ != version)
	}
	
	def getVersion(versionId: String): Option[Version] = versions.find(vi => vi.versionId == versionId)
	
	override def toString: String = "variant: " + variantId + ", "+ variantTime
	
}
