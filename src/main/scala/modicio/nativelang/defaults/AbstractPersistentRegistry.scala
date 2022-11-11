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

import modicio.core.datamappings.{AssociationData, AttributeData, ParentRelationData, InstanceData, ModelElementData, RuleData}
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

  protected def fetchModelElementData(name: String, identity: String): Future[Option[ModelElementData]]

  protected def fetchModelElementData(identity: String): Future[Set[ModelElementData]]

  protected def fetchInstanceDataOfType(typeName: String): Future[Set[InstanceData]]

  protected def fetchInstanceData(instanceId: String): Future[Option[InstanceData]]

  protected def fetchRuleData(modelElementName: String, identity: String): Future[Set[RuleData]]

  protected def fetchAttributeData(instanceId: String): Future[Set[AttributeData]]

  protected def fetchParentRelationData(instanceId: String): Future[Set[ParentRelationData]]

  protected def fetchAssociationData(instanceId: String): Future[Set[AssociationData]]

  /*
   * ***********************************************************
   * Write Methods
   * ***********************************************************
   */

  protected def writeModelElementData(modelElementData: ModelElementData): Future[ModelElementData]

  protected def writeInstanceData(instanceData: InstanceData): Future[InstanceData]

  protected def writeRuleData(diff: IODiff[RuleData]): Future[Set[RuleData]]

  protected def writeAttributeData(diff: IODiff[AttributeData]): Future[Set[AttributeData]]

  protected def writeParentRelationData(diff: IODiff[ParentRelationData]): Future[Set[ParentRelationData]]

  protected def writeAssociationData(diff: IODiff[AssociationData]): Future[Set[AssociationData]]

  /*
   * ***********************************************************
   * Remove Methods
   * ***********************************************************
   */

  protected def removeModelElementWithRules(modelElementName: String, identity: String): Future[Any]

  protected def removeInstanceWithData(instanceId: String): Future[Any]


  /*
   * ***********************************************************
   * Query Methods
   * ***********************************************************
   */

  protected def queryInstanceDataByIdentityPrefixAndTypeName(identityPrefix: String, typeName: String): Future[Set[InstanceData]]

  /**
   * Queries all types (ModelElements) present in the repository.
   * The query follows the following syntax:
   * "" -> empty query must return all
   * "identity=VALUE" -> identity must match a value
   * "name=VALUE" -> name must match a value
   * "EXPR1 & EXPR2 & ..." -> chain selectors
   * @param query
   * @return
   */
  protected def queryTypes(query: String): Future[Set[ModelElementData]]


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
      Future.successful(referenceHandles.map(_.commit()))
    })
  }


  override final def incrementRunning: Future[Any] = {
    val runningTime = IdentityProvider.newTimestampId()
    val runningId = IdentityProvider.newRandomId()
    getReferences map (referenceHandles => {
      referenceHandles.foreach(_.getModelElement.incrementRunning(runningTime, runningId))
      Future.successful(referenceHandles.map(_.commit()))
    })
  }

  override final def getReferenceTypes: Future[Set[String]] =
    queryTypes(query = "identity="+ModelElement.REFERENCE_IDENTITY) map (_.map(_.name))

  override final def getAllTypes: Future[Set[String]] =
    queryTypes(query = "") map (_.map(_.name))

  /**
   * Get all variants which are used by a known instance
   *
   * @return
   */
  override final def getInstanceVariants: Future[Seq[(Long, String)]] = {
    //TODO
  }

  /**
   * Get all variants that are known. This includes all variants that are known by instances and the variant used by
   * the reference model which does not need to be instantiated.
   *
   * @return
   */
  override final def getTypeVariants: Future[Seq[(Long, String)]] = {
    //TODO
  }

  /**
   * Get all known variants from getTypeVariants together with the number of times the variant is references over
   * all known instances.
   * Note that if a (deep-)instances consists of n internal types naturally using the same variant, this leads to n
   * references of the variant although only one root instances (ESI) exists for the type.
   *
   * @return
   */
  override final def getVariantMap: Future[Map[(Long, String), Int]] = {
    //TODO
  }

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
    } yield {
      if(modelElementDataOption.isDefined){
        val modelElementData = modelElementDataOption.get;
        Some(typeFactory.loadType(modelElementData, ruleData))
      }else {
        None
      }
    }
  }

  override final def getReferences: Future[Set[TypeHandle]] = {
    for {
      modelElementDataSet <- fetchModelElementData(ModelElement.REFERENCE_IDENTITY)
      ruleDataSet <- Future.sequence(modelElementDataSet.map(f => fetchRuleData(f.name, f.identity)))
    } yield {
      if(modelElementDataSet.size != ruleDataSet.size){
        Future.failed(new Exception("Not matching modelElement and rule-set relations"))
      }
      modelElementDataSet.map(modelElementData => (modelElementData, {
        ruleDataSet.find(rules => rules.exists(ruleData =>
          ruleData.modelElementName == modelElementData.name && ruleData.identity == modelElementData.identity))
      })).map(modelTuple => {
        val (modelElementData, ruleDataOption) = modelTuple
        val ruleData: Set[RuleData] = ruleDataOption.getOrElse(Set())
        typeFactory.loadType(modelElementData, ruleData)
      }) //++ baseModels.values.map(_.createHandle).toSet
    }
  }

  override protected final def setNode(typeHandle: TypeHandle): Future[Any] = {
    fetchRuleData(typeHandle.getModelElement.name, typeHandle.getTypeIdentity) flatMap (oldRuleData => {
      val (modelElementData, ruleData) = typeHandle.getModelElement.toData
      for {
        _ <- writeRuleData(applyRules(oldRuleData, ruleData))
        _ <- writeModelElementData(modelElementData)
        _ <- incrementRunning if modelElementData.identity == ModelElement.REFERENCE_IDENTITY
      } yield {}
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
   * @param deepInstance
   * @return
   */
  override final def setInstance(deepInstance: DeepInstance): Future[Unit] = {
    val data = deepInstance.toData
    val (instanceData, attributeData, associationData, parentRelationData) = (ImmutableShape unapply data).get
    get(deepInstance.getInstanceId) flatMap (oldInstanceOption => {
      val (_, oldParentRelationData: Set[ParentRelationData], oldAttributeData: Set[AttributeData], oldAssociationData: Set[AssociationData]) = {
        if(oldInstanceOption.isDefined){
          oldInstanceOption.get.toData
        }else{
          (null, Set[ParentRelationData](), Set[AttributeData](), Set[AssociationData]())
        }
      }
      for {
        _ <- writeInstanceData(instanceData)
        _ <- writeParentRelationData(applyUpdate[ParentRelationData](oldParentRelationData, parentRelationData, _.id == 0))
        _ <- writeAssociationData(applyUpdate[AssociationData](oldAssociationData, associationData, _.id == 0))
        _ <- writeAttributeData(applyUpdate[AttributeData](oldAttributeData, attributeData, _.id == 0))
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
    applyUpdate(old, in, e => !old.exists(_.id == e.id))
  }

  /**
   *
   * @param old
   * @param in
   * @param isNew
   * @tparam T
   * @return
   */
  private final def applyUpdate[T](old: Set[T], in: Set[T], isNew: T => Boolean): IODiff[T] = {
    val toAdd = in.filter(isNew)
    val toChange = in.diff(toAdd)
    val toDelete = old.diff(toChange)
    val toUpdate = toChange.diff(toDelete)
    IODiff(toDelete, toAdd, toUpdate)
  }

}
