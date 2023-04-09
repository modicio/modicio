package modicio.core.monitoring

import io.circe.generic.semiauto.deriveEncoder
import io.circe.{Encoder, Json}
import modicio.core.rules.ParentRelationRule
import io.circe.syntax._
import modicio.core.util.IdentityProvider
import modicio.core.{DeepInstance, InstanceFactory, Registry, RegistryDecorator, TypeFactory, TypeHandle}

import scala.collection.mutable.ListBuffer
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.{Failure, Success}
import java.time._

class Monitoring(registry: Registry, typeFactory: TypeFactory, instanceFactory: InstanceFactory) extends RegistryDecorator(registry, typeFactory, instanceFactory){
	
	var classes: ListBuffer[Class] = new ListBuffer[Class]
	
	private def detectType(deepInstance: DeepInstance): DeepInstance = {
		val typeHandle: TypeHandle = deepInstance.getTypeHandle
		val timeIdentity = typeHandle.getTimeIdentity
		val versionTime = timeIdentity.versionTime
		val versionId = timeIdentity.versionId
		val variantTime = timeIdentity.variantTime
		val variantId = timeIdentity.variantId
		
		println("Succeed with: " + typeHandle.getTypeName + ", " + typeHandle.getTypeIdentity)
		println("Succeed with: " + deepInstance.getIdentity + ", " + deepInstance.getInstanceId)
		//		println("variant: " + variant.variantId + ", " + variant.variantTime)
		//		println("version: " + version.versionId + ", " + version.versionTime)
		
		if (classes.exists(c => c.typeName == typeHandle.getTypeName)) {
			val c: Class = classes.find(c => c.typeName == typeHandle.getTypeName).get
//			if (classes.find(n => n.typeIdentity == typeHandle.getTypeIdentity).isDefined) {
//
//				val n: Class = classes.find(n => n.typeIdentity == typeHandle.getTypeIdentity).get
//				c.typeIdentity = typeHandle.getTypeIdentity
//				n.typeIdentity = "upperclass"
//			}
			
			var variant: Variant = null
			var version: Version = null
			//check variant and version
			if (c.getVariant(variantId).isDefined) {
				variant = c.getVariant(variantId).get
				if (variant.getVersion(versionId).isEmpty) {
					version = variant.addVersion(versionId, versionTime)
				}else {
					version = variant.getVersion(versionId).get
				}
			} else {
				// add new Variant with Version
				variant = classes.find(c => c.typeName == typeHandle.getTypeName).get.addVariant(variantId, variantTime)
				version = variant.addVersion(versionId, versionTime)
			}
			detectRelations(version, typeHandle.getParentRelations, typeHandle.getAssociated)
		} else {
			val newClass = Class(typeName = typeHandle.getTypeName, typeIdentity = typeHandle.getTypeIdentity)
			val newVariant = newClass.addVariant(variantId, variantTime)
			val newVersion = newVariant.addVersion(versionId, versionTime)
			classes.addOne(newClass)
			detectRelations(newVersion, typeHandle.getParentRelations, typeHandle.getAssociated)
		}
		detectInstance(deepInstance)
	}
	
	private def detectRelations(version: Version, parentRelations: Set[ParentRelationRule], associated: Set[TypeHandle]): Unit = {
		//add associations and parentRelations
		parentRelations.foreach(p => {
			version.addParentRelations(p.parentName, p.parentIdentity)
		})
		associated.foreach(t => {
			version.addAssociated(t.getTypeName, t.getTimeIdentity.variantId, t.getTimeIdentity.versionId)
		})
	}
	
	override def get(instanceId: String): Future[Option[DeepInstance]] = {
		val deepInstance = super.get(instanceId)
		deepInstance.onComplete({
			case Failure(exception) => println("Failed with: " + exception.getMessage)
			case Success(deepInstance) =>
				if (deepInstance.isDefined && !classes.exists(c => c.typeIdentity == deepInstance.get.getIdentity)) detectType(deepInstance.get)
		})
		deepInstance
	}
	
	override def getAll(typeName: String): Future[Set[DeepInstance]] = {
		val deepInstances = super.getAll(typeName)
		deepInstances.onComplete({
			case Failure(exception) => println("Failed with: " + exception.getMessage)
			case Success(deepInstances) => deepInstances.foreach(deepInstance => {
				if (!classes.exists(c => c.typeIdentity == deepInstance.getIdentity)) detectType(deepInstance)
			})
		})
		deepInstances
	}
	
	override def setInstance(deepInstance: DeepInstance): Future[Any] = {
		println("setInstance")
		println("deepInstance: ", deepInstance.typeHandle.getTypeName)
		super.setInstance(detectType(deepInstance))
	}
	
	private def detectInstance(deepInstance: DeepInstance): DeepInstance = {
		val instanceId = deepInstance.getInstanceId
		val versionId: String = deepInstance.getTypeHandle.getTimeIdentity.versionId
		val variantId = deepInstance.getTypeHandle.getTimeIdentity.variantId
		
		val version: Option[Version] = classes.find(c => c.typeName == deepInstance.typeHandle.getTypeName).get.getVariant(variantId).get.getVersion(versionId)
		if (version.isDefined) {
			version.get.increase(instanceId, IdentityProvider.newTimestampId())
		}
		
		val minutes: Int = 30
		deleteObsoleteKnowledge(minutes)
		
		val size: Int = 10
		if (classes.length > size) {
			deleteObsoleteKnowledgeBySize(size)
		}
		deepInstance
	}
	
	private def deleteObsoleteKnowledge(minutes: Int): Unit = {
		val expiryTime: Long = getExpiryTime(minutes)
		classes.foreach(c => {
			c.variants.foreach(vs => {
				for(vi <- vs.versions) {
					if (vi.versionTime <= expiryTime) {
						vs.deleteVersion(vi)
					}
				}
			})
			for(vs <- c.variants) {
				if(vs.variantTime <= expiryTime || vs.versions.isEmpty) {
					c.deleteVariant(vs)
				}
			}
			this.classes = classes.filter(_.variants.nonEmpty)
		})
	}
	
	private def deleteObsoleteKnowledgeBySize(size: Int): Unit = {
		while (classes.length > size) {
			classes.foreach(c => {
				c.variants.foreach(vs => {
					var latestVersion: Version = vs.versions.last
					for (vi <- vs.versions) {
						if (vi.versionTime > latestVersion.versionTime) {
							latestVersion = vi
						}
					}
					vs.deleteVersion(latestVersion)
				})
				for (vs <- c.variants) {
					if (vs.versions.isEmpty) {
						c.deleteVariant(vs)
					}
				}
				this.classes = classes.filter(_.variants.nonEmpty)
			})
		}
	}
	
	private def getExpiryTime(minutes: Long): Long = {
		val cvdate: LocalDateTime = LocalDateTime.now.minusMinutes(minutes)
		val zonedDateTime = ZonedDateTime.of(cvdate, ZoneId.systemDefault)
		zonedDateTime.toInstant.toEpochMilli
	}
	
	def produceJson(): Json = {
		implicit val versionEncoder: Encoder[Version] = deriveEncoder[Version]
		implicit val variantEncoder: Encoder[Variant] = deriveEncoder[Variant]
		implicit val classEncoder: Encoder[Class] = deriveEncoder[Class]
		
		val encodingResult = classes.asJson
		encodingResult
	}
}
