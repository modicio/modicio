package modicio.nativelang.defaults

import modicio.core.datamappings._
import modicio.core.util.IODiff
import modicio.nativelang.util.LRUCache

import java.util.concurrent.TimeUnit
import scala.collection.mutable.ListBuffer
import scala.concurrent.duration.Duration
import scala.concurrent.{ExecutionContext, Future}

class PersistentRegistryOptimization(registry: AbstractPersistentRegistry,
                                     ttl: Duration = Duration(365, TimeUnit.DAYS),
                                     modelElementDataCacheSize: Int = 100,
                                     modelElementDataSetCacheSize: Int = 100,
                                     ruleDataSetCacheSize: Int = 100,
                                     instanceDataCacheSize: Int = 100,
                                     instanceDataSetCacheSize: Int = 100,
                                     pluginDataSetCacheSize: Int = 100,
                                     attributeDataSetCacheSize: Int = 100,
                                     parentRelationDataSetCacheSize: Int = 100,
                                     associationDataSetCacheSize: Int = 100,
                                    )
                                    (implicit executionContext: ExecutionContext)
  extends AbstractPersistentRegistry(registry.typeFactory, registry.instanceFactory)(executionContext) {
  private val modelElementDataCache = new LRUCache[(String, String), ModelElementData](modelElementDataCacheSize, ttl)
  private val modelElementDataSetCache = new LRUCache[String, Set[ModelElementData]](modelElementDataSetCacheSize, ttl)
  private val ruleDataSetCache = new LRUCache[(String, String), Set[RuleData]](ruleDataSetCacheSize, ttl)
  private val instanceDataCache = new LRUCache[String, InstanceData](instanceDataCacheSize, ttl)
  private val instanceDataSetCache = new LRUCache[String, Set[InstanceData]](instanceDataSetCacheSize, ttl)
  private val pluginDataSetCache = new LRUCache[(String, String), Set[PluginData]](pluginDataSetCacheSize, ttl)
  private val attributeDataSetCache = new LRUCache[String, Set[AttributeData]](attributeDataSetCacheSize, ttl)
  private val parentRelationDataSetCache = new LRUCache[String, Set[ParentRelationData]](parentRelationDataSetCacheSize, ttl)
  private val associationDataSetCache = new LRUCache[String, Set[AssociationData]](associationDataSetCacheSize, ttl)

  /**
   * Get the [[ModelElementData]] of a type matching the provided parameters.
   *
   * @param name     name of the [[ModelElement]]
   * @param identity identity of the [[ModelElement]]
   * @return Future option of [[ModelElementData]] or None if not found
   */

  override protected[modicio] def fetchModelElementData(name: String, identity: String): Future[Option[ModelElementData]] = {
    val local = modelElementDataCache.get((name, identity))
    if (local.isEmpty) {
      return for {
        remote <- registry.fetchModelElementData(name, identity)
      } yield {
        if (remote.isDefined) modelElementDataCache.set((name, identity), remote.get)
        remote
      }
    }
    Future.successful(local)
  }


  /**
   * Get the [[ModelElementData]] of a type matching the provided parameters.
   *
   * @param identity identity of the [[ModelElement]]
   * @return Future option of [[ModelElementData]] or None if not found
   */
  override protected[modicio] def fetchModelElementData(identity: String): Future[Set[ModelElementData]] = {
    val local = modelElementDataSetCache.get(identity)
    if (local.isEmpty) {
      return for {
        remote <- registry.fetchModelElementData(identity)
      } yield {
        modelElementDataSetCache.set(identity, remote)
        remote
      }
    }
    Future.successful(local.get)
  }

  /**
   * Get the [[InstanceData]] elements instantiation a given type ([[ModelElement]]) specified by its name.
   * <p> [[InstanceData]] refers to its type by [[InstanceData.instanceOf]]
   *
   * @param typeName name of the [[ModelElement]] which instances must be returned
   * @return Future set of [[InstanceData]] matching the given type name
   */
  override protected[modicio] def fetchInstanceDataOfType(typeName: String): Future[Set[InstanceData]] = {
    val local = instanceDataSetCache.get(typeName)
    if (local.isEmpty) {
      return for {
        remote <- registry.fetchInstanceDataOfType(typeName)
      } yield {
        instanceDataSetCache.set(typeName, remote)
        remote
      }
    }
    Future.successful(local.get)
  }

  /**
   * Get the exact match of an [[InstanceData]] object by its instanceId.
   *
   * @param instanceId the [[InstanceData.instanceId]] of an instance
   * @return Future option of [[InstanceData]] or None if not found
   */
  override protected[modicio] def fetchInstanceData(instanceId: String): Future[Option[InstanceData]] = {
    val local = instanceDataCache.get(instanceId)
    if (local.isEmpty) {
      return for {
        remote <- registry.fetchInstanceData(instanceId)
      } yield {
        if (remote.isDefined) instanceDataCache.set(instanceId, remote.get)
        remote
      }
    }
    Future.successful(local)
  }

  /**
   * Get all [[RuleData]] objects associated to a given [[ModelElement]] by its provided parameters.
   * <p> The [[RuleData]] object refers to its parent ModelElement directly by its attributes
   * [[RuleData.modelElementName]] and [[RuleData.identity]].
   *
   * @param modelElementName name of the parent [[ModelElement]]
   * @param identity         identity of the parent [[ModelElement]]
   * @return Future set of all [[RuleData]] associated by the given parameters
   */
  override protected[modicio] def fetchRuleData(modelElementName: String, identity: String): Future[Set[RuleData]] = {
    val local = ruleDataSetCache.get((modelElementName, identity))
    if (local.isEmpty) {
      return for {
        remote <- registry.fetchRuleData(modelElementName, identity)
      } yield {
        ruleDataSetCache.set((modelElementName, identity), remote)
        remote
      }
    }
    Future.successful(local.get)
  }

  /**
   * Get all [[PluginData]] objects associated to a given [[ModelElement]] by its provided parameters.
   * <p> The [[PluginData]] object refers to its parent ModelElement directly by its attributes
   * [[PluginData.modelElementName]] and [[PluginData.identity]].
   *
   * @param modelElementName name of the parent [[ModelElement]]
   * @param identity         identity of the parent [[ModelElement]]
   * @return Future set of all [[RuleData]] associated by the given parameters
   */
  override protected[modicio] def fetchPluginData(modelElementName: String, identity: String): Future[Set[PluginData]] = {
    val local = pluginDataSetCache.get((modelElementName, identity))
    if (local.isEmpty) {
      return for {
        remote <- registry.fetchPluginData(modelElementName, identity)
      } yield {
        pluginDataSetCache.set((modelElementName, identity), remote)
        remote
      }
    }
    Future.successful(local.get)
  }

  /**
   * Get all [[AttributeData]] referenced by a given instanceId which is provided by [[AttributeData.instanceId]].
   *
   * @param instanceId instanceId of the parent [[DeepInstance]]
   * @return Future set of all matching [[AttributeData]]
   */
  override protected[modicio] def fetchAttributeData(instanceId: String): Future[Set[AttributeData]] = {
    val local = attributeDataSetCache.get(instanceId)
    if (local.isEmpty) {
      return for {
        remote <- registry.fetchAttributeData(instanceId)
      } yield {
        attributeDataSetCache.set(instanceId, remote)
        remote
      }
    }
    Future.successful(local.get)
  }

  /**
   * Get all [[ParentRelationData]] referenced by a given instanceId which is provided by [[ParentRelationData.instanceId]].
   *
   * @param instanceId instanceId of the parent [[DeepInstance]]
   * @return Future set of all matching [[ParentRelationData]]
   */
  override protected[modicio] def fetchParentRelationData(instanceId: String): Future[Set[ParentRelationData]] = {
    val local = parentRelationDataSetCache.get(instanceId)
    if (local.isEmpty) {
      return for {
        remote <- registry.fetchParentRelationData(instanceId)
      } yield {
        parentRelationDataSetCache.set(instanceId, remote)
        remote
      }
    }
    Future.successful(local.get)
  }

  /**
   * Get all [[AssociationData]] referenced by a given instanceId which is provided by [[AssociationData.instanceId]].
   *
   * @param instanceId instanceId of the parent [[DeepInstance]]
   * @return Future set of all matching [[AssociationData]]
   */
  override protected[modicio] def fetchAssociationData(instanceId: String): Future[Set[AssociationData]] = {
    val local = associationDataSetCache.get(instanceId)
    if (local.isEmpty) {
      return for {
        remote <- registry.fetchAssociationData(instanceId)
      } yield {
        associationDataSetCache.set(instanceId, remote)
        remote
      }
    }
    Future.successful(local.get)
  }

  /**
   * Add [[ModelElementData]] to the storage. [[ModelElementData.name]] and [[ModelElementData.identity]] form the unique
   * primary key.
   * <p> Insert if new, replace if present.
   * <p> If not successfully, the Future must fail with an Exception.
   *
   * @param modelElementData [[ModelElementData]] to write.
   * @return Future of inserted data on success.
   */
  override protected[modicio] def writeModelElementData(modelElementData: ModelElementData): Future[ModelElementData] = {
    def writeModelElementData0(modelElementData: ModelElementData): Future[ModelElementData] = {
      modelElementDataCache.set((modelElementData.name, modelElementData.identity), modelElementData)
      modelElementDataSetCache.remove(modelElementData.identity)
      for {
        remote <- registry.writeModelElementData(modelElementData)
      } yield {
        remote
      }
    }
    modelElementDataCache.get((modelElementData.name, modelElementData.identity)) match {
      case Some(value) => if (value == modelElementData) {
        Future.successful(value)
      } else {
        writeModelElementData0(modelElementData)
      }
      case None => writeModelElementData0(modelElementData)
    }
  }

  /**
   * Add [[InstanceData]] to the storage. [[InstanceData.instanceId]] serves as the unique primary key.
   * <p> Insert if new, replace if present.
   * <p> If not successfully, the Future must fail with an Exception.
   *
   * @param instanceData [[InstanceData]] to write
   * @return Future of inserted data on success.
   */
  override protected[modicio] def writeInstanceData(instanceData: InstanceData): Future[InstanceData] = {
    def writeModelInstanceData0(instanceData: InstanceData): Future[InstanceData] = {
      instanceDataCache.set(instanceData.instanceId, instanceData)
      instanceDataSetCache.remove(instanceData.instanceId)
      for {
        remote <- registry.writeInstanceData(instanceData)
      } yield {
        remote
      }
    }
    instanceDataCache.get(instanceData.instanceId) match {
      case Some(value) => if (value == instanceData) {
        Future.successful(value)
      } else {
        writeModelInstanceData0(instanceData)
      }
      case None => writeModelInstanceData0(instanceData)
    }
  }

  /**
   * Add, Update and Delete [[RuleData]] as specified by a provided [[IODiff]].
   * <p> [[IODiff.toDelete]] must be removed from the storage
   * <p> [[IODiff.toAdd]] must be inserted in the storage.
   * <p> [[IODiff.toUpdate]] must be updated in the storage.
   * <p> [[RuleData]] has the [[RuleData.id]] as its primary key. This value can be empty or zero. In those cases,
   * the storage must assign globally unique values (UUIDs). Inserted [[RuleData]] with new ids must be returned on success.
   * <p> <strong>All operations part of the IODiff must be performed transactional! If one sub-operation fails, all
   * other operations must not be performed or rolled back.</strong>
   * <p> If not successfully, the Future must fail with an Exception.
   *
   * @param diff [[IODiff]] containing the [[RuleData]] to add, update and delete
   * @return Future of inserted [[RuleData]] on success.
   */
  override protected[modicio] def writeRuleData(diff: IODiff[RuleData]): Future[Set[RuleData]] = {
    val toAdd = ListBuffer[RuleData]()
    val toUpdate = ListBuffer[RuleData]()
//    filter unnecessary writes
    for (rule <- diff.toAdd) {
      val rules = ruleDataSetCache.get(rule.modelElementName, rule.identity)
      if (rules.isDefined) { if (!rules.get.contains(rule)) toAdd.addOne(rule) } else toAdd.addOne(rule)
    }
    for (rule <- diff.toUpdate) {
      val rules = ruleDataSetCache.get(rule.modelElementName, rule.identity)
      if (rules.isDefined) { if (!rules.get.contains(rule)) toUpdate.addOne(rule) } else toUpdate.addOne(rule)
    }
//    write
    for (set <- List(toAdd, diff.toDelete, toUpdate)) {
      set.map(data => (data.modelElementName, data.identity)).foreach(ruleDataSetCache.remove)
    }
    registry.writeRuleData(IODiff[RuleData](diff.toDelete, toAdd.toSet, toUpdate.toSet))
  }

  /**
   * Add, Update and Delete [[AttributeData]] as specified by a provided [[IODiff]].
   * <p> [[IODiff.toDelete]] must be removed from the storage
   * <p> [[IODiff.toAdd]] must be inserted in the storage.
   * <p> [[IODiff.toUpdate]] must be updated in the storage.
   * <p> [[AttributeData]] has the [[AttributeData.id]] as its primary key. This value can be empty or zero. In those cases,
   * the storage must assign globally unique values (UUIDs). Inserted [[AttributeData]] with new ids must be returned on success.
   * <p> <strong>All operations part of the IODiff must be performed transactional! If one sub-operation fails, all
   * other operations must not be performed or rolled back.</strong>
   * <p> If not successfully, the Future must fail with an Exception.
   *
   * @param diff [[IODiff]] containing the [[AttributeData]] to add, update and delete
   * @return Future of inserted [[AttributeData]] on success.
   */
  override protected[modicio] def writeAttributeData(diff: IODiff[AttributeData]): Future[Set[AttributeData]] = {
    val toAdd = ListBuffer[AttributeData]()
    val toUpdate = ListBuffer[AttributeData]()
    //    filter unnecessary writes
    for (attributeDate <- diff.toAdd) {
      val attributeData = attributeDataSetCache.get(attributeDate.instanceId)
      if (attributeData.isDefined) {
        if (!attributeData.get.contains(attributeDate)) toAdd.addOne(attributeDate)
      } else toAdd.addOne(attributeDate)
    }
    for (attributeDate <- diff.toUpdate) {
      val attributeData = attributeDataSetCache.get(attributeDate.instanceId)
      if (attributeData.isDefined) {
        if (!attributeData.get.contains(attributeDate)) toUpdate.addOne(attributeDate)
      } else toUpdate.addOne(attributeDate)
    }
    //    write
    for (set <- List(toAdd, diff.toDelete, toUpdate)) {
      set.map(data => data.instanceId).foreach(attributeDataSetCache.remove)
    }
    registry.writeAttributeData(IODiff[AttributeData](diff.toDelete, toAdd.toSet, toUpdate.toSet))
  }

  /**
   * Add, Update and Delete [[ParentRelationData]] as specified by a provided [[IODiff]].
   * <p> [[IODiff.toDelete]] must be removed from the storage
   * <p> [[IODiff.toAdd]] must be inserted in the storage.
   * <p> [[IODiff.toUpdate]] must be updated in the storage.
   * <p> [[ParentRelationData]] has the [[ParentRelationData.id]] as its primary key. This value can be empty or zero. In those cases,
   * the storage must assign globally unique values (UUIDs). Inserted [[ParentRelationData]] with new ids must be returned on success.
   * <p> <strong>All operations part of the IODiff must be performed transactional! If one sub-operation fails, all
   * other operations must not be performed or rolled back.</strong>
   * <p> If not successfully, the Future must fail with an Exception.
   *
   * @param diff [[IODiff]] containing the [[ParentRelationData]] to add, update and delete
   * @return Future of inserted [[ParentRelationData]] on success.
   */
  override protected[modicio] def writeParentRelationData(diff: IODiff[ParentRelationData]): Future[Set[ParentRelationData]] = {
    val toAdd = ListBuffer[ParentRelationData]()
    val toUpdate = ListBuffer[ParentRelationData]()
    //    filter unnecessary writes
    for (parentRelationDate <- diff.toAdd) {
      val parentRelationData = parentRelationDataSetCache.get(parentRelationDate.instanceId)
      if (parentRelationData.isDefined) {
        if (!parentRelationData.get.contains(parentRelationDate)) toAdd.addOne(parentRelationDate)
      } else toAdd.addOne(parentRelationDate)
    }
    for (parentRelationDate <- diff.toUpdate) {
      val parentRelationData = parentRelationDataSetCache.get(parentRelationDate.instanceId)
      if (parentRelationData.isDefined) {
        if (!parentRelationData.get.contains(parentRelationDate)) toUpdate.addOne(parentRelationDate)
      } else toUpdate.addOne(parentRelationDate)
    }
    //    write
    for (set <- List(toAdd, diff.toDelete, toUpdate)) {
      set.map(data => data.instanceId).foreach(parentRelationDataSetCache.remove)
    }
    registry.writeParentRelationData(IODiff[ParentRelationData](diff.toDelete, toAdd.toSet, toUpdate.toSet))
  }

  /**
   * Add, Update and Delete [[AssociationData]] as specified by a provided [[IODiff]].
   * <p> [[IODiff.toDelete]] must be removed from the storage
   * <p> [[IODiff.toAdd]] must be inserted in the storage.
   * <p> [[IODiff.toUpdate]] must be updated in the storage.
   * <p> [[AssociationData]] has the [[AssociationData.id]] as its primary key. This value can be empty or zero. In those cases,
   * the storage must assign globally unique values (UUIDs). Inserted [[AssociationData]] with new ids must be returned on success.
   * <p> <strong>All operations part of the IODiff must be performed transactional! If one sub-operation fails, all
   * other operations must not be performed or rolled back.</strong>
   * <p> If not successfully, the Future must fail with an Exception.
   *
   * @param diff [[IODiff]] containing the [[AssociationData]] to add, update and delete
   * @return Future of inserted [[AssociationData]] on success.
   */
  override protected[modicio] def writeAssociationData(diff: IODiff[AssociationData]): Future[Set[AssociationData]] = {
    val toAdd = ListBuffer[AssociationData]()
    val toUpdate = ListBuffer[AssociationData]()
    //    filter unnecessary writes
    for (associationDate <- diff.toAdd) {
      val associationData = associationDataSetCache.get(associationDate.instanceId)
      if (associationData.isDefined) {
        if (!associationData.get.contains(associationDate)) toAdd.addOne(associationDate)
      } else toAdd.addOne(associationDate)
    }
    for (associationDate <- diff.toUpdate) {
      val associationData = associationDataSetCache.get(associationDate.instanceId)
      if (associationData.isDefined) {
        if (!associationData.get.contains(associationDate)) toUpdate.addOne(associationDate)
      } else toUpdate.addOne(associationDate)
    }
    //    write
    for (set <- List(toAdd, diff.toDelete, toUpdate)) {
      set.map(data => data.instanceId).foreach(associationDataSetCache.remove)
    }
    registry.writeAssociationData(IODiff[AssociationData](diff.toDelete, toAdd.toSet, toUpdate.toSet))
  }

  /**
   * Add, Update and Delete [[PluginData]] as specified by a provided [[IODiff]].
   * <p> [[IODiff.toDelete]] must be removed from the storage
   * <p> [[IODiff.toAdd]] must be inserted in the storage.
   * <p> [[IODiff.toUpdate]] must be updated in the storage.
   * <p> [[AssociationData]] has the [[PluginData.id]] as its primary key. This value can be empty or zero. In those cases,
   * the storage must assign globally unique values (UUIDs). Inserted [[PluginData]] with new ids must be returned on success.
   * <p> <strong>All operations part of the IODiff must be performed transactional! If one sub-operation fails, all
   * other operations must not be performed or rolled back.</strong>
   * <p> If not successfully, the Future must fail with an Exception.
   *
   * @param diff [[IODiff]] containing the [[PluginData]] to add, update and delete
   * @return Future of inserted [[PluginData]] on success.
   */
  override protected[modicio] def writePluginData(diff: IODiff[PluginData]): Future[Set[PluginData]] = {
    val toAdd = ListBuffer[PluginData]()
    val toUpdate = ListBuffer[PluginData]()
    //    filter unnecessary writes
    for (pluginDate <- diff.toAdd) {
      val pluginData = pluginDataSetCache.get((pluginDate.modelElementName, pluginDate.identity))
      if (pluginData.isDefined) {
        if (!pluginData.get.contains(pluginDate)) toAdd.addOne(pluginDate)
      } else toAdd.addOne(pluginDate)
    }
    for (pluginDate <- diff.toUpdate) {
      val pluginData = pluginDataSetCache.get((pluginDate.modelElementName, pluginDate.identity))
      if (pluginData.isDefined) {
        if (!pluginData.get.contains(pluginDate)) toUpdate.addOne(pluginDate)
      } else toUpdate.addOne(pluginDate)
    }
    //    write
    for (set <- List(toAdd, diff.toDelete, toUpdate)) {
      set.map(data => (data.modelElementName, data.identity)).foreach(pluginDataSetCache.remove)
    }
    registry.writePluginData(IODiff[PluginData](diff.toDelete, toAdd.toSet, toUpdate.toSet))
  }

  /**
   * Remove a [[ModelElementData]] and all associated [[RuleData]] from the storage.
   * <p> The [[ModelElementData]] is given by its primary key values [[ModelElementData.name]] and [[ModelElementData.identity]].
   * <p> [[RuleData]] to delete matches those values in [[RuleData.modelElementName]] and [[RuleData.identity]].
   * <p> <strong>All operations must be performed transactional! If one sub-operation fails, all
   * other operations must not be performed or rolled back.</strong>
   * <p> If not successfully, the Future must fail with an Exception.
   *
   * @param modelElementName name of the [[ModelElementData]]
   * @param identity         identity of the [[ModelElementData]]
   * @return Future on success
   */
  override protected[modicio] def removeModelElementWithRules(modelElementName: String, identity: String): Future[Any] = {
    ruleDataSetCache.remove((modelElementName, identity))
    modelElementDataCache.remove((modelElementName, identity))
    modelElementDataSetCache.remove(identity)
    for {
      _ <- registry.removeModelElementWithRules(modelElementName, identity)
    } yield {
    }
  }

  /**
   * Remove a [[InstanceData]] with all associated [[AssociationData]], [[AttributeData]] and [[ParentRelationData]]
   * from the storage.
   * <p> The [[InstanceData]] is given by its primary key instanceId.
   * <p> All associated values named above can be found by their corresponding instanceId value.
   * <p> <strong>Note that his operation is explicitly not recursive on parent relations!</strong>
   * <p> <strong>All operations must be performed transactional! If one sub-operation fails, all
   * other operations must not be performed or rolled back.</strong>
   * <p> If not successfully, the Future must fail with an Exception.
   *
   * @param instanceId id of the [[InstanceData]] to remove
   * @return Future on success
   */
  override protected[modicio] def removeInstanceWithData(instanceId: String): Future[Any] = {
    instanceDataCache.remove(instanceId)
    instanceDataSetCache.clear()
    associationDataSetCache.remove(instanceId)
    attributeDataSetCache.remove(instanceId)
    parentRelationDataSetCache.remove(instanceId)
    for {
      _ <- registry.removeInstanceWithData(instanceId)
    } yield {
    }
  }

  override protected[modicio] def queryInstanceDataByIdentityPrefixAndTypeName(identityPrefix: String, typeName: String): Future[Set[InstanceData]] = {
    val local = instanceDataSetCache.get(typeName)
    if (local.isEmpty) {
      registry.queryInstanceDataByIdentityPrefixAndTypeName(identityPrefix, typeName)
    }
    val instances = ListBuffer[InstanceData]()
    for (instanceData <- local.get) {
      if (instanceData.identity.startsWith(identityPrefix)) instances.addOne(instanceData)
    }
    Future.successful(instances.toSet)
  }

  /**
   * Queries all types (ModelElements) present in the repository.
   * The query follows the following syntax:
   * "" -> empty query must return all
   * "identity=VALUE" -> identity must match a value
   * "name=VALUE" -> name must match a value
   * "EXPR1 & EXPR2 & ..." -> chain selectors
   *
   * @param query Query string as specified in the method description.
   * @return
   */
  override protected[modicio] def queryTypes(query: String): Future[Set[ModelElementData]] = {
    for {
      modelElementData <- registry.queryTypes(query)
    } yield {
      for (m <- modelElementData) {
        modelElementDataCache.set((m.name, m.identity), m)
      }
      modelElementData
    }
  }

  /**
   * Query all variants which are used by a known instance
   *
   * @return Future sequence of variant tuples in the format (variantTime, variantId)
   */
  override protected[modicio] def queryVariantsOfInstances(): Future[Seq[(Long, String)]] = {
    registry.queryVariantsOfInstances()
  }

  /**
   * Query all variants that are known. This includes all variants that are known by instances and the variant used by
   * the reference model which does not need to be instantiated.
   *
   * @return Future sequence of variant tuples in the format (variantTime, variantId)
   */
  override protected[modicio] def queryVariantsOfTypes(): Future[Seq[(Long, String)]] = {
    registry.queryVariantsOfInstances()
  }

  /**
   * Query all known variants together with the number of times the variant is references over
   * all known instances i.e, types including the reference model.
   * Note that if a (deep-)instances consists of n internal types naturally using the same variant, this leads to n
   * references of the variant although only one root instances (ESI) exists for the type.
   *
   * @return Future map of variant tuples with their number of occurrences (count) in the format
   *         {(variantTime, variantId) -> count}
   */
  override protected[modicio] def queryVariantOccurrencesAndCount(): Future[Map[(Long, String), Int]] = {
    registry.queryVariantOccurrencesAndCount()
  }
}
