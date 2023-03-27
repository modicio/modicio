package modicio.core.monitoring
import modicio.core.rules.ParentRelationRule
import modicio.core.{DeepInstance, Registry, RegistryDecorator, TypeHandle}

import scala.collection.mutable.ListBuffer
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.{Failure, Success, Try}

class Monitoring(registry: Registry) extends RegistryDecorator(registry){
	
	var classes: ListBuffer[Class] = _
	
	override def get(instanceId: String): Future[Option[DeepInstance]] = {
		val deepInstance = super.get(instanceId)
		deepInstance.onComplete(extractor)
		deepInstance
	}
	
	def detectType(deepInstance: DeepInstance): Unit = {
		val identity = deepInstance.getIdentity
		val typeHandle: TypeHandle = deepInstance.getTypeHandle
		val timeIdentity = deepInstance.getTypeHandle.getTimeIdentity
		val versionTime = timeIdentity.versionTime
		val versionId: String = timeIdentity.versionId
		val variantTime = timeIdentity.variantTime
		val variantId = timeIdentity.variantId
		val parentRelations: Set[ParentRelationRule] = typeHandle.getParentRelations
		val associated: Set[TypeHandle] = typeHandle.getAssociated
		
		println("Successed with: ")
		println(deepInstance.toString + identity)
		println("TimeIdentity: " + versionTime + versionId + variantTime + variantId)
		
		if (classes.exists(c => c.identity == identity)) {
			//check variant and version
			val variant: Option[Variant] = classes.find(c => c.variants.contains(variantId)).get.getVariant(variantId)
			val version: Option[Version] = variant.get.getVersion(versionId)
			if (variant.isDefined && version.isEmpty) {
				// add new Version
				variant.get.addVersion(versionId, versionTime)
			} else {
				// add new Variant with Version
				val newVariant = classes.find(c => c.identity == identity).get.addVariant(variantId, variantTime)
				newVariant.addVersion(versionId, versionTime)
			}
			//add associations and parentRelations
			parentRelations.foreach(p => {
				version.get.addParentRelations(p.parentName, p.parentIdentity)
			} )
			associated.foreach(t => {
				version.get.addAssocaited(t.getTypeIdentity)
			})
		}else {
			//unknown class
			val newClass: Class = new Class(identity)
			val newVariant = newClass.addVariant(variantId, variantTime)
			val newVersion = newVariant.addVersion(versionId, versionTime)
			classes.addOne(newClass)
			//add associations and parentRelations
			parentRelations.foreach(p => {
				newVersion.addParentRelations(p.parentName, p.parentIdentity)
			})
			associated.foreach(t => {
				newVersion.addAssocaited(t.getTypeIdentity)
			})
		}
		detectInstance(deepInstance)
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
		println("setInstance")
		detectType(deepInstance)
		super.setInstance(deepInstance)
	}
	
	def detectInstance(deepInstance: DeepInstance): Unit = {
		val instanceId = deepInstance.getInstanceId
		val versionId: String = deepInstance.getTypeHandle.getTimeIdentity.versionId
		val variantId = deepInstance.getTypeHandle.getTimeIdentity.variantId
		
		val version: Option[Version] = classes.find(c => c.variants.contains(variantId)).get.getVariant(variantId).get.getVersion(versionId)
		if (version.isDefined) {
			version.get.increase(instanceId)
		}
		classes.foreach(c => c.accept(ConcreteVisitor))
	}
	
	def getRegistry(): Registry = this.registry
}
