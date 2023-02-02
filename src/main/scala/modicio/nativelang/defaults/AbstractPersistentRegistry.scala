/**
 * Copyright 2022 Karl Kegel
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
package modicio.nativelang.defaults

import modicio.core.datamappings.{AssociationData, AttributeData, InstanceData, ModelElementData, ParentRelationData, PluginData, RuleData}
import modicio.core.util.IdentityProvider
import modicio.core.{DeepInstance, ImmutableShape, InstanceFactory, ModelElement, Registry, Shape, TimeIdentity, TypeFactory, TypeHandle}

import scala.collection.mutable
import scala.concurrent.{ExecutionContext, Future}

abstract class AbstractPersistentRegistry(typeFactory: TypeFactory, instanceFactory: InstanceFactory)
                                         (implicit executionContext: ExecutionContext)
  extends Registry(typeFactory, instanceFactory) {

  protected case class IODiff[T](toDelete: Set[T], toAdd: Set[T], toUpdate: Set[T])

  /*
   * ***********************************************************
   * Fetch Methods
   * ***********************************************************
   */

  /**
   * Get the [[ModelElementData]] of a type matching the provided parameters.
   *
   * @param name name of the [[ModelElement]]
   * @param identity identity of the [[ModelElement]]
   * @return Future option of [[ModelElementData]] or None if not found
   */
  protected[modicio] def fetchModelElementData(name: String, identity: String): Future[Option[ModelElementData]]

  /**
   * Get the [[ModelElementData]] of a type matching the provided parameters.
   *
   * @param identity identity of the [[ModelElement]]
   * @return Future option of [[ModelElementData]] or None if not found
   */
  protected[modicio] def fetchModelElementData(identity: String): Future[Set[ModelElementData]]

  /**
   * Get the [[InstanceData]] elements instantiation a given type ([[ModelElement]]) specified by its name.
   * <p> [[InstanceData]] refers to its type by [[InstanceData.instanceOf]]
   *
   * @param typeName name of the [[ModelElement]] which instances must be returned
   * @return Future set of [[InstanceData]] matching the given type name
   */
  protected[modicio] def fetchInstanceDataOfType(typeName: String): Future[Set[InstanceData]]

  /**
   * Get the exact match of an [[InstanceData]] object by its instanceId.
   *
   * @param instanceId the [[InstanceData.instanceId]] of an instance
   * @return Future option of [[InstanceData]] or None if not found
   */
  protected[modicio] def fetchInstanceData(instanceId: String): Future[Option[InstanceData]]

  /**
   * Get all [[RuleData]] objects associated to a given [[ModelElement]] by its provided parameters.
   * <p> The [[RuleData]] object refers to its parent ModelElement directly by its attributes
   * [[RuleData.modelElementName]] and [[RuleData.identity]].
   *
   * @param modelElementName name of the parent [[ModelElement]]
   * @param identity identity of the parent [[ModelElement]]
   * @return Future set of all [[RuleData]] associated by the given parameters
   */
  protected[modicio] def fetchRuleData(modelElementName: String, identity: String): Future[Set[RuleData]]

  /**
   * Get all [[PluginData]] objects associated to a given [[ModelElement]] by its provided parameters.
   * <p> The [[PluginData]] object refers to its parent ModelElement directly by its attributes
   * [[PluginData.modelElementName]] and [[PluginData.identity]].
   *
   * @param modelElementName name of the parent [[ModelElement]]
   * @param identity         identity of the parent [[ModelElement]]
   * @return Future set of all [[RuleData]] associated by the given parameters
   */
  protected[modicio] def fetchPluginData(modelElementName: String, identity: String): Future[Set[PluginData]]

  /**
   * Get all [[AttributeData]] referenced by a given instanceId which is provided by [[AttributeData.instanceId]].
   *
   * @param instanceId instanceId of the parent [[DeepInstance]]
   * @return Future set of all matching [[AttributeData]]
   */
  protected[modicio] def fetchAttributeData(instanceId: String): Future[Set[AttributeData]]

  /**
   * Get all [[ParentRelationData]] referenced by a given instanceId which is provided by [[ParentRelationData.instanceId]].
   *
   * @param instanceId instanceId of the parent [[DeepInstance]]
   * @return Future set of all matching [[ParentRelationData]]
   */
  protected[modicio] def fetchParentRelationData(instanceId: String): Future[Set[ParentRelationData]]

  /**
   * Get all [[AssociationData]] referenced by a given instanceId which is provided by [[AssociationData.instanceId]].
   *
   * @param instanceId instanceId of the parent [[DeepInstance]]
   * @return Future set of all matching [[AssociationData]]
   */
  protected[modicio] def fetchAssociationData(instanceId: String): Future[Set[AssociationData]]

  /*
   * ***********************************************************
   * Write Methods
   * ***********************************************************
   */

  /**
   * Add [[ModelElementData]] to the storage. [[ModelElementData.name]] and [[ModelElementData.identity]] form the unique
   * primary key.
   * <p> Insert if new, replace if present.
   * <p> If not successfully, the Future must fail with an Exception.
   *
   * @param modelElementData [[ModelElementData]] to write.
   * @return Future of inserted data on success.
   */
  protected[modicio] def writeModelElementData(modelElementData: ModelElementData): Future[ModelElementData]

  /**
   * Add [[InstanceData]] to the storage. [[InstanceData.instanceId]] serves as the unique primary key.
   * <p> Insert if new, replace if present.
   * <p> If not successfully, the Future must fail with an Exception.
   *
   * @param instanceData [[InstanceData]] to write
   * @return Future of inserted data on success.
   */
  protected[modicio] def writeInstanceData(instanceData: InstanceData): Future[InstanceData]

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
  protected[modicio] def writeRuleData(diff: IODiff[RuleData]): Future[Set[RuleData]]

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
  protected[modicio] def writeAttributeData(diff: IODiff[AttributeData]): Future[Set[AttributeData]]

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
  protected[modicio] def writeParentRelationData(diff: IODiff[ParentRelationData]): Future[Set[ParentRelationData]]

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
  protected[modicio] def writeAssociationData(diff: IODiff[AssociationData]): Future[Set[AssociationData]]

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
  protected[modicio] def writePluginData(diff: IODiff[PluginData]): Future[Set[PluginData]]

  /*
   * ***********************************************************
   * Remove Methods
   * ***********************************************************
   */

  /**
   * Remove a [[ModelElementData]] and all associated [[RuleData]] from the storage.
   * <p> The [[ModelElementData]] is given by its primary key values [[ModelElementData.name]] and [[ModelElementData.identity]].
   * <p> [[RuleData]] to delete matches those values in [[RuleData.modelElementName]] and [[RuleData.identity]].
   * <p> <strong>All operations must be performed transactional! If one sub-operation fails, all
   * other operations must not be performed or rolled back.</strong>
   * <p> If not successfully, the Future must fail with an Exception.
   *
   * @param modelElementName name of the [[ModelElementData]]
   * @param identity identity of the [[ModelElementData]]
   * @return Future on success
   */
  protected[modicio] def removeModelElementWithRules(modelElementName: String, identity: String): Future[Any]


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
  protected[modicio] def removeInstanceWithData(instanceId: String): Future[Any]


  /*
   * ***********************************************************
   * Query Methods
   * ***********************************************************
   */

  protected[modicio] def queryInstanceDataByIdentityPrefixAndTypeName(identityPrefix: String, typeName: String): Future[Set[InstanceData]]

  /**
   * Queries all types (ModelElements) present in the repository.
   * The query follows the following syntax:
   * "" -> empty query must return all
   * "identity=VALUE" -> identity must match a value
   * "name=VALUE" -> name must match a value
   * "EXPR1 & EXPR2 & ..." -> chain selectors
   * @param query Query string as specified in the method description.
   * @return
   */
  protected[modicio] def queryTypes(query: String): Future[Set[ModelElementData]]

  /**
   * Query all variants which are used by a known instance
   *
   * @return Future sequence of variant tuples in the format (variantTime, variantId)
   */
  protected[modicio] def queryVariantsOfInstances(): Future[Seq[(Long, String)]]

  /**
   * Query all variants that are known. This includes all variants that are known by instances and the variant used by
   * the reference model which does not need to be instantiated.
   *
   * @return Future sequence of variant tuples in the format (variantTime, variantId)
   */
  protected[modicio] def queryVariantsOfTypes(): Future[Seq[(Long, String)]]

  /**
   * Query all known variants together with the number of times the variant is references over
   * all known instances i.e, types including the reference model.
   * Note that if a (deep-)instances consists of n internal types naturally using the same variant, this leads to n
   * references of the variant although only one root instances (ESI) exists for the type.
   *
   * @return Future map of variant tuples with their number of occurrences (count) in the format
   *         {(variantTime, variantId) -> count}
   */
  protected[modicio] def queryVariantOccurrencesAndCount(): Future[Map[(Long, String), Int]]

  /*
   * ***********************************************************
   * Implementation of abstract members
   * ***********************************************************
   */

  override final def getReferenceTimeIdentity: Future[TimeIdentity] = {
    getRoot flatMap (rootOption => {
      if(rootOption.isDefined){
        Future.successful(rootOption.get.getTimeIdentity)
      }else{
        Future.failed(new IllegalAccessException("No ROOT reference element present"))
      }
    })
  }

  override final def incrementVariant: Future[Any] = {
    val variantTime = IdentityProvider.newTimestampId()
    val variantId = IdentityProvider.newRandomId()
    getReferences map (referenceHandles => {
      referenceHandles.foreach(_.getModelElement.incrementVariant(variantTime, variantId))

      Future.sequence(referenceHandles.map(handle => {
        val modelElementData = handle.getModelElement.toData._1
        writeModelElementData(modelElementData)
      }))

    })
  }


  override final def incrementRunning: Future[Any] = {
    val runningTime = IdentityProvider.newTimestampId()
    val runningId = IdentityProvider.newRandomId()
    getReferences map (referenceHandles => {
      referenceHandles.foreach(_.getModelElement.incrementRunning(runningTime, runningId))

      Future.sequence(referenceHandles.map(handle => {
        val modelElementData = handle.getModelElement.toData._1
        writeModelElementData(modelElementData)
      }))

    })
  }

  override final def getReferenceTypes: Future[Set[String]] =
    queryTypes(query = "identity="+ModelElement.REFERENCE_IDENTITY) map (_.map(_.name))

  override final def getAllTypes: Future[Set[String]] =
    queryTypes(query = "") map (_.map(_.name))

  /**
   * Get all variants which are used by a known instance
   *
   * @return Future sequence of variant tuples in the format (variantTime, variantId)
   */
  override final def getInstanceVariants: Future[Seq[(Long, String)]] = queryVariantsOfInstances()

  /**
   * Get all variants that are known. This includes all variants that are known by instances and the variant used by
   * the reference model which does not need to be instantiated.
   *
   * @return Future sequence of variant tuples in the format (variantTime, variantId)
   */
  override final def getTypeVariants: Future[Seq[(Long, String)]] = queryVariantsOfTypes()

  /**
   * Get all known variants together with the number of times the variant is references over
   * all known instances i.e, types including the reference model.
   * Note that if a (deep-)instances consists of n internal types naturally using the same variant, this leads to n
   * references of the variant although only one root instances (ESI) exists for the type.
   *
   * @return Future map of variant tuples with their number of occurrences (count) in the format
   *         {(variantTime, variantId) -> count}
   */
  override final def getVariantMap: Future[Map[(Long, String), Int]] = queryVariantOccurrencesAndCount()

  override final def containsRoot: Future[Boolean] = {
    getRoot map (_.isDefined)
  }

  final def getRoot: Future[Option[TypeHandle]] = getType(ModelElement.ROOT_NAME, ModelElement.REFERENCE_IDENTITY)

  override final def getSingletonRefsOf(name: String): Future[Set[DeepInstance]] = {
    queryInstanceDataByIdentityPrefixAndTypeName(ModelElement.SINGLETON_PREFIX, name) flatMap (instanceData =>
      Future.sequence(instanceData.map(_.instanceId).map(get))) map (results => results.filter(_.isDefined).map(_.get))
  }

  override final def getType(name: String, identity: String): Future[Option[TypeHandle]] = {
    for {
      modelElementDataOption <- fetchModelElementData(name, identity)
      ruleData <- fetchRuleData(name, identity)
      pluginData <- fetchPluginData(name, identity)
    } yield {
      if(modelElementDataOption.isDefined){
        val modelElementData = modelElementDataOption.get
        Some(typeFactory.loadType(modelElementData, ruleData, pluginData))
      }else {
        None
      }
    }
  }

  override final def getReferences: Future[Set[TypeHandle]] = {
    for {
      modelElementDataSet <- fetchModelElementData(ModelElement.REFERENCE_IDENTITY)
      ruleDataSet <- Future.sequence(modelElementDataSet.map(f => fetchRuleData(f.name, f.identity)))
      pluginDataSet <- Future.sequence(modelElementDataSet.map(f => fetchPluginData(f.name, f.identity)))
    } yield {
      modelElementDataSet.map(modelElementData => (modelElementData, {
        ruleDataSet.find(rules => rules.exists(ruleData =>
          ruleData.modelElementName == modelElementData.name && ruleData.identity == modelElementData.identity))
      },
        pluginDataSet.find(plugins => plugins.exists(pluginData =>
          pluginData.modelElementName == modelElementData.name && pluginData.identity == modelElementData.identity))
      )).map(modelTuple => {
        val (modelElementData, ruleDataOption, pluginDataOption) = modelTuple
        val ruleData: Set[RuleData] = ruleDataOption.getOrElse(Set())
        val pluginData: Set[PluginData] = pluginDataOption.getOrElse(Set())
        typeFactory.loadType(modelElementData, ruleData, pluginData)
      })
    }
  }

  override protected final def setNode(typeHandle: TypeHandle, importMode: Boolean = false): Future[Unit] = {
    for{
      oldRuleData <- fetchRuleData(typeHandle.getModelElement.name, typeHandle.getTypeIdentity)
      oldPluginData <- fetchPluginData(typeHandle.getModelElement.name, typeHandle.getTypeIdentity)
      (modelElementData, ruleData, pluginData) = typeHandle.getModelElement.toData
      _ <- writeRuleData(applyRules(oldRuleData, ruleData))
      _ <- writeModelElementData(modelElementData)
      _ <- writePluginData(applyPlugins(oldPluginData, pluginData))
    } yield {}
  }


  /**
   * FIXME - not implemented yet
   *
   * @param set
   * @return
   */
  override def exchangeModel(set: Set[TypeHandle]): Future[Any] = ???

  /**
   * FIXME - not implemented yet
   *
   * Remove parts of the model in a way producing a minimal number of overall deletions while trying to retain integrity
   * <p> <strong>Experimental Feature</strong>
   * <p> In case of a user-space identity, the deep instance and its types are deleted i.e., the ESI is removed. Note that
   * associated ESI are not modified leading to associations pointing to nothing. This may introduce invalid related instances
   * and must be taken care of manually.
   *
   * @param instanceId id of the root instance element of the ESI to delete
   * @return Future[Any] - if successful
   */
  override def autoRemove(instanceId: String): Future[Any] = {
    get(instanceId) flatMap (instanceOption => {
      if(instanceOption.isDefined){
        val instance = instanceOption.get
        val typeHandle = instance.typeHandle
        val parents = instance.shape.getParentRelations.map(_.parentInstanceId)
        for {
          _ <- removeModelElementWithRules(typeHandle.getTypeName, typeHandle.getTypeIdentity)
          _ <- removeInstanceWithData(instanceId)
          _ <- Future.sequence(parents.map(autoRemove))
        } yield {}
      }else{
        Future.failed(new IllegalArgumentException("No such instance found"))
      }
    })

  }


  override final def get(instanceId: String): Future[Option[DeepInstance]] = {
    fetchInstanceData(instanceId) flatMap (instanceDataOption => {
      if(instanceDataOption.isDefined){
        val instanceData = instanceDataOption.get
        for {
          attributeData <- fetchAttributeData(instanceId)
          parentRelationData <- fetchParentRelationData(instanceId)
          associationData <- fetchAssociationData(instanceId)
          typeOption <- getType(instanceData.instanceOf, instanceData.identity)
        } yield {
          if(typeOption.isDefined){
            val associations = mutable.Set[AssociationData]()
            associations.addAll(associationData)
            val shape = new Shape(attributeData,  associations, parentRelationData)
            instanceFactory.loadInstance(instanceData, shape, typeOption.get)
          }else{
            None
          }
        }
      }else{
        Future.successful(None)
      }
    })
  }

  override final def getAll(typeName: String): Future[Set[DeepInstance]] = {
    fetchInstanceDataOfType(typeName) flatMap (instanceDataSet =>
      Future.sequence(instanceDataSet.map(
        instanceData => get(instanceData.instanceId))) map (results =>
        results.filter(_.isDefined).map(_.get)))
  }

  /**
   *
   * @param deepInstance the [[DeepInstance]] to add to the [[Registry]].
   * @return
   */
  override final def setInstance(deepInstance: DeepInstance): Future[Unit] = {
    val data = deepInstance.toData
    val instanceData = data.instanceData
    val attributeData = data.attributes
    val associationData = data.associations
    val parentRelationData = data.parentRelations
    get(deepInstance.getInstanceId) flatMap (oldInstanceOption => {
      val oldData = {
        if(oldInstanceOption.isDefined){
          oldInstanceOption.get.toData
        }else{
          ImmutableShape(null, Set[AttributeData](), Set[AssociationData](), Set[ParentRelationData]())
        }
      }
      for {
        _ <- writeInstanceData(instanceData)
        _ <- writeParentRelationData(applyUpdate[ParentRelationData](oldData.parentRelations, parentRelationData))
        _ <- writeAssociationData(applyUpdate[AssociationData](oldData.associations, associationData))
        _ <- writeAttributeData(applyUpdate[AttributeData](oldData.attributes, attributeData))
      } yield {}
    })
  }

  /**
   * Remove parts of the model in a way producing a minimal number of overall deletions while trying to retain integrity
   * <p> <strong>Experimental Feature</strong>
   * <p> In case of a reference-identity ModelElement, the ModelElement is deleted only. In consequence, children pointing to that ModelElement
   * and other ModelElements associating this ModelElement become invalid and must be repaired manually.
   * <p> In case of a singleton-identity ModelElement, the whole singleton-fork of the ModelElement tree and the corresponding
   * [[DeepInstance DeepInstance]] tree are removed.
   * <p> In case of a user-space identity, nothing happens yet => TODO
   *
   * @param name     of the [[ModelElement ModelElement]] trying to remove
   * @param identity of the [[ModelElement ModelElement]] trying to remove
   * @return
   */
  override final def autoRemove(name: String, identity: String): Future[Any] = {

    if (identity == ModelElement.REFERENCE_IDENTITY) {
      //In case of reference identity, remove model-element locally. FIXME The model may become invalid
      removeModelElementWithRules(name, identity)

    } else if (identity == ModelElement.SINGLETON_IDENTITY) {
      //In case of a singleton identity modelElement

      val singletonInstanceId = DeepInstance.deriveSingletonInstanceId(identity, name)

      //get the associated singleton deep instance
      get(singletonInstanceId) flatMap  (deepInstanceOption => {
        if (deepInstanceOption.isDefined) {
          //unfold the singleton deep-instance

          deepInstanceOption.get.unfold() flatMap (unfoldedInstance => {
            val parentRelations = unfoldedInstance.getTypeHandle.getModelElement.getParents

            //delete all parent model-elements of the singleton deep-instance
            //delete the actual deep-instance and trigger deletion of its parents
            for {
              _ <- removeInstanceWithData(singletonInstanceId)
              _ <- Future.sequence(parentRelations.map(parentRelation => autoRemove(parentRelation.name, ModelElement.SINGLETON_IDENTITY)))
              _ <- removeModelElementWithRules(name, identity)
            } yield {}
          })

        } else {
          Future.failed(new IllegalArgumentException("AUTO DELETE: No such singleton instance found"))
        }
      })

    } else {
      //TODO
      Future.successful((): Unit)
    }
  }


  private final def applyRules(old: Set[RuleData], in: Set[RuleData]): IODiff[RuleData] = {
    applyUpdate(old, in)
  }

  type GenericData = Any{
    val id: Long
  }

  private final def applyPlugins(old: Set[PluginData], in: Set[PluginData]): IODiff[PluginData] = {
    applyUpdate(old, in)
  }

  /**
   *
   * @param old Set of old values serving as the base
   * @param in Set of incoming values to apply to the old values
   * @param isNew decider function if a values if a new value
   * @tparam T generic parameter
   * @return
   */
  private final def applyUpdate[GenericData](old: Set[GenericData], in: Set[GenericData]): IODiff[GenericData] = {
    val toUpdate = in.intersect(old)
    val toAdd = in.diff(toUpdate)
    val toDelete = old.diff(toUpdate)
    IODiff(toDelete, toAdd, toUpdate)
  }
}
