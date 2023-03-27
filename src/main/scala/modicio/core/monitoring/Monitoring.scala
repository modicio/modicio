package modicio.core.monitoring
import modicio.core.rules.ParentRelationRule
import modicio.core.{DeepInstance, InstanceFactory, Registry, RegistryDecorator, TypeFactory, TypeHandle}

import scala.collection.mutable.ListBuffer
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.{Failure, Success, Try}

class Monitoring(registry: Registry, typeFactory: TypeFactory, instanceFactory: InstanceFactory) extends RegistryDecorator(registry, typeFactory, instanceFactory){
	
	var classes: ListBuffer[Class] = new ListBuffer[Class]
	
	override def get(instanceId: String): Future[Option[DeepInstance]] = {
		val deepInstance = super.get(instanceId)
		deepInstance.onComplete(extractor)
		deepInstance
	}
	
	def detectType(deepInstance: DeepInstance): DeepInstance = {
		val typeHandle: TypeHandle = deepInstance.getTypeHandle
		val timeIdentity = typeHandle.getTimeIdentity
		val versionTime = timeIdentity.versionTime
		val versionId = timeIdentity.versionId
		val variantTime = timeIdentity.variantTime
		val variantId = timeIdentity.variantId

		println("Successed with: " + typeHandle.getTypeName + ", " + typeHandle.getTypeIdentity)
		println("variant: " + variantTime + ", " + variantId)
		println("version: " + versionTime + ", " + versionId)


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
			val newClass: Class = new Class(typeName = typeHandle.getTypeName, typeIdentity = typeHandle.getTypeIdentity)
			val newVariant = newClass.addVariant(variantId, variantTime)
			val newVersion = newVariant.addVersion(versionId, versionTime)
			classes.addOne(newClass)
			detectRelations(newVersion, typeHandle.getParentRelations, typeHandle.getAssociated)
		}
		detectInstance(deepInstance)
	}
	
	def detectRelations(version: Version, parentRelations: Set[ParentRelationRule], associated: Set[TypeHandle]): Unit = {
		//add associations and parentRelations
		parentRelations.foreach(p => {
			version.addParentRelations(p.parentName, p.parentIdentity)
		})
		associated.foreach(t => {
			version.addAssocaited(t.getTypeIdentity)
		})
	}
	def extractor(result: Try[Option[DeepInstance]]): Unit = result match {
		case Failure(exception) => println("Failed with: " + exception.getMessage)
		case Success(deepInstance) =>
			if (deepInstance.isDefined) detectType(deepInstance.get)
	}
	
	
	override def getAll(typeName: String): Future[Set[DeepInstance]] = {
		val deepInstances = super.getAll(typeName)
		super.getAll(typeName).onComplete(extractorAll)
		deepInstances
	}
	
	def extractorAll(result: Try[Set[DeepInstance]]): Unit = result match {
		case Failure(exception) => println("Failed with: " + exception.getMessage)
		case Success(deepInstances) => deepInstances.foreach(d => detectType(d))
	}
	
	
	override def setInstance(deepInstance: DeepInstance): Future[Any] = {
		super.setInstance(detectType(deepInstance))
	}
	
	def detectInstance(deepInstance: DeepInstance): DeepInstance = {
		val instanceId = deepInstance.getInstanceId
		val versionId: String = deepInstance.getTypeHandle.getTimeIdentity.versionId
		val variantId = deepInstance.getTypeHandle.getTimeIdentity.variantId
		
		val version: Option[Version] = classes.find(c => c.typeName == deepInstance.typeHandle.getTypeName).get.getVariant(variantId).get.getVersion(versionId)
		if (version.isDefined) {
			version.get.increase(instanceId)
		}
		
//		println(version.get.versionId + "instances: " + version.get.instances.toString())
//		println(version.get.versionId + "parentRelations:" + version.get.parentRelations.toString())
		
		classes.foreach(c => c.accept(ConcreteVisitor))
		deepInstance
	}
	
	override def toString(): String = {
		"classes: " + classes.foreach(c => {
			println(c.typeName +c.variants.foreach(vs => {
				println( vs.toString() + vs.versions.foreach(vi => println(vi.toString())))
			})
			)})
	}
}
