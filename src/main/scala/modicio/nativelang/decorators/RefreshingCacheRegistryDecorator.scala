package modicio.nativelang.decorators

import modicio.core.{DeepInstance, Registry, TimeIdentity, TypeHandle}

import scala.collection.mutable.ListBuffer
import scala.concurrent.Future

class RefreshingCacheRegistryDecorator(decoratedRegistry: Registry) extends Registry{
  private val typeHandleCache = new ListBuffer[(TypeHandle, Long)]()
  private val deepInstanceCache = new ListBuffer[(DeepInstance, Long)]()

  def getReferenceTimeIdentity: Future[TimeIdentity] = decoratedRegistry.getReferenceTimeIdentity

  def incrementVariant: Future[Any] = decoratedRegistry.incrementVariant

  def incrementRunning: Future[Any] = decoratedRegistry.incrementRunning

  def containsRoot: Future[Boolean] = decoratedRegistry.containsRoot

  def getType(name: String, identity: String): Future[Option[TypeHandle]] = decoratedRegistry.getType(name, identity)

  def getReferences: Future[Set[TypeHandle]] = decoratedRegistry.getReferences

  def exchangeModel(set: Set[TypeHandle]): Future[Any] = decoratedRegistry.exchangeModel(set)

  def getReferenceTypes: Future[Set[String]] = decoratedRegistry.getReferenceTypes

  def getAllTypes: Future[Set[String]] = decoratedRegistry.getAllTypes

  def getInstanceVariants: Future[Seq[(Long, String)]] = decoratedRegistry.getInstanceVariants

  def getTypeVariants: Future[Seq[(Long, String)]] = decoratedRegistry.getTypeVariants

  def getVariantMap: Future[Map[(Long, String), Int]] = decoratedRegistry.getVariantMap

  def getSingletonRefsOf(name: String): Future[Set[DeepInstance]] = decoratedRegistry.getSingletonRefsOf(name)

  def setType(typeHandle: TypeHandle, importMode: Boolean = false): Future[Any] = decoratedRegistry.setType(typeHandle, importMode)

  def autoRemove(name: String, identity: String): Future[Any] = decoratedRegistry.autoRemove(name, identity)

  def autoRemove(instanceId: String): Future[Any] = decoratedRegistry.autoRemove(instanceId)

  def get(instanceId: String): Future[Option[DeepInstance]] = decoratedRegistry.get(instanceId)

  def getAll(typeName: String): Future[Set[DeepInstance]] = decoratedRegistry.getAll(typeName)

  def setInstance(deepInstance: DeepInstance): Future[Any] = decoratedRegistry.setInstance(deepInstance)
}

