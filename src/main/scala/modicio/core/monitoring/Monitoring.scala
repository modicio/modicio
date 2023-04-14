/**
 * Copyright 2023 Minji Kim
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
	var minutes: Int = 30
	var size: Int = 10
	
	private def detectType(deepInstance: DeepInstance): DeepInstance = {
		val typeHandle: TypeHandle = deepInstance.getTypeHandle
		val timeIdentity = typeHandle.getTimeIdentity
		val versionTime = timeIdentity.versionTime
		val versionId = timeIdentity.versionId
		val variantTime = timeIdentity.variantTime
		val variantId = timeIdentity.variantId
		
		if (classes.exists(c => c.typeName == typeHandle.getTypeName)) {
			val c: Class = classes.find(c => c.typeName == typeHandle.getTypeName).get
			
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
		
		if (minutes > 0) {
			deleteObsoleteKnowledge(minutes)
		}
		
		if (classes.length > size && size > 0) {
			deleteObsoleteKnowledgeBySize(size)
		}
		deepInstance
	}
	
	def deleteObsoleteKnowledge(minutes: Int): Unit = {
		if (minutes != this.minutes) {
			this.minutes = minutes
		}
		if (minutes >= 0) {
			val expiryTime: Long = getExpiryTime(minutes)
			classes.foreach(c => {
				c.variants.foreach(vs => {
					for (vi <- vs.versions) {
							for (instance <- vi.instances) {
								if (instance.last.toLong <= expiryTime) {
									vi.decrease(instance)
								}
							}
							if (vi.instances.isEmpty) {
								vs.deleteVersion(vi)
							}
					}
					if (vs.versions.isEmpty) {
						c.deleteVariant(vs)
					}
				})
				this.classes = classes.filter(_.variants.nonEmpty)
			})
		}
	}
	
	def deleteObsoleteKnowledgeBySize(size: Int): Unit = {
		if (size != this.size) {
			this.size = size
		}
		if (size > 0) {
			while (classes.length > size) {
				classes.foreach(c => {
					c.variants.foreach(vs => {
						for (vi <- vs.versions) {
							var latestInstance = vi.instances.last
							for (instance <- vi.instances) {
								if (instance.last.toLong > latestInstance.last.toLong) {
									latestInstance = instance
								}
							}
							vi.decrease(latestInstance)
							if (vi.instances.isEmpty) {
								vs.deleteVersion(vi)
							}
						}
						if (vs.versions.isEmpty) {
							c.deleteVariant(vs)
						}
					})
					this.classes = classes.filter(_.variants.nonEmpty)
				})
			}
		}
	}
	
	def getExpiryTime(minutes: Long): Long = {
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
