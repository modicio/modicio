package modicio.core

import scala.concurrent.Future

class RegistryDecorator(registry: Registry)
	extends Registry(registry.typeFactory, registry.instanceFactory){
	
	override def get(instanceId: String): Future[Option[DeepInstance]] = registry.get(instanceId)

	override def getAll(typeName: String): Future[Set[DeepInstance]] = registry.getAll(typeName)

	override def setInstance(deepInstance: DeepInstance): Future[Any] = registry.setInstance(deepInstance)
	
	override def getReferenceTimeIdentity: Future[TimeIdentity] = registry.getReferenceTimeIdentity
	
	override def incrementVariant: Future[Any] =registry.incrementVariant
	
	override def incrementRunning: Future[Any] = registry.incrementRunning
	
	override def containsRoot: Future[Boolean] = registry.containsRoot
	
	override def getType(name: String, identity: String): Future[Option[TypeHandle]] = registry.getType(name, identity)
	
	override def getReferences: Future[Set[TypeHandle]] = registry.getReferences
	
	override def exchangeModel(set: Set[TypeHandle]): Future[Any] = registry.exchangeModel(set)
	
	override def getReferenceTypes: Future[Set[String]] = registry.getReferenceTypes
	
	override def getAllTypes: Future[Set[String]] = registry.getAllTypes
	
	override def getInstanceVariants: Future[Seq[(Long, String)]] = registry.getInstanceVariants
	
	override def getTypeVariants: Future[Seq[(Long, String)]] = registry.getTypeVariants
	
	override def getVariantMap: Future[Map[(Long, String), Int]] = registry.getVariantMap
	
	/**
	 *
	 * @param name
	 * @return
	 */
	override def getSingletonRefsOf(name: String): Future[Set[DeepInstance]] = registry.getSingletonRefsOf(name)
	
	/**
	 * Remove parts of the model in a way producing a minimal number of overall deletions while trying to retain integrity
	 * <p> <strong>Experimental Feature</strong>
	 * <p> In case of a reference-identity ModelElement, the ModelElement is deleted only. In consequence, children pointing to that ModelElement
	 * and other ModelElements associating this ModelElement become invalid and must be repaired manually.
	 * <strong>
	 * If this case is called, the running version must be incremented in ALL TimeIdentities part of reference ModelElements.
	 * </strong>
	 * <p> If the TypeHandle to add has a not yet wired (check local id) TimeIdentity, this must be applied as well.
	 * <p> In case of a singleton-identity ModelElement, the whole singleton-fork of the ModelElement tree and the corresponding
	 * [[DeepInstance DeepInstance]] tree are removed.
	 *
	 * @param name     of the [[ModelElement ModelElement]] trying to remove
	 * @param identity of the [[ModelElement ModelElement]] trying to remove
	 * @return
	 */
	override def autoRemove(name: String, identity: String): Future[Any] = registry.autoRemove(name, identity)
	
	override def autoRemove(instanceId: String): Future[Any] = registry.autoRemove(instanceId)
	
	/**
	 * Template-method called by [[Registry#setType setType()]] exclusively.
	 * <p> This operation is implemented by a concrete registry and stores a new dynamic or forked model-element.
	 *
	 * <p><strong>Check setType documentation!</strong>
	 *
	 * @param typeHandle [[TypeHandle TypeHandle]] of a dynamic or forked model-element to store/register
	 * @return TODO doc
	 */
	override protected final def setNode(typeHandle: TypeHandle, importMode: Boolean = false): Future[Any] = registry.setNode(typeHandle, importMode)
	
	override def getRootOf(instance: DeepInstance): Future[DeepInstance] = registry.getRootOf(instance)
}
