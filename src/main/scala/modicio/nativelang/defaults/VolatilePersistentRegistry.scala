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

import modicio.core.datamappings.{AssociationData, AttributeData, InstanceData, ModelElementData, ParentRelationData, RuleData}
import modicio.core.util.IdentityProvider
import modicio.core.{DeepInstance, InstanceFactory, ModelElement, TypeFactory}

import java.util.concurrent.locks.{Lock, ReentrantReadWriteLock}
import scala.collection.mutable
import scala.collection.mutable.ListBuffer
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

/**
 * Non-persistent implementation of the [[AbstractPersistentRegistry]] simulating database behaviour via
 * Scala Buffer implementations.
 * <p> This class is used for testing and benchmarking mainly, but can also be used in prototypical scenarios where
 * the [[SimpleMapRegistry]] is not sufficient due to their superficial behaviour.
 */
class VolatilePersistentRegistry(typeFactory: TypeFactory, instanceFactory: InstanceFactory)
                                (implicit executionContext: ExecutionContext)
  extends AbstractPersistentRegistry(typeFactory, instanceFactory)(executionContext) {


  /*
    #########################################################################
    FIXME implement all methods of this class!
     use mutable.Buffer[T] datastructures to simulate database tables.
     Benchmarking functionality can be realised by additional counter and methods.
    ##########################################################################
   */

  /**
   * To avoid deadlocks, acquire locks in the following order:
   * 1. modelElementDataLock
   * 2. instanceDataLock
   * 3. ruleDataLock
   * 4. attributeDataLock
   * 5. parentRelationDataLock
   * 6. associationDataLock
   */
  private var modelElementDataBuffer: ListBuffer[ModelElementData] = new ListBuffer[ModelElementData]()
  private val modelElementDataLock: ReentrantReadWriteLock = new ReentrantReadWriteLock()

  private var instanceDataBuffer: ListBuffer[InstanceData] = new ListBuffer[InstanceData]()
  private val instanceDataLock: ReentrantReadWriteLock = new ReentrantReadWriteLock()

  private var ruleDataBuffer: ListBuffer[RuleData] = new ListBuffer[RuleData]()
  private val ruleDataLock: ReentrantReadWriteLock = new ReentrantReadWriteLock()

  private var attributeDataBuffer: ListBuffer[AttributeData] = new ListBuffer[AttributeData]()
  private val attributeDataLock: ReentrantReadWriteLock = new ReentrantReadWriteLock()

  private var parentRelationDataBuffer: ListBuffer[ParentRelationData] = new ListBuffer[ParentRelationData]()
  private val parentRelationDataLock: ReentrantReadWriteLock = new ReentrantReadWriteLock()

  private var associationDataBuffer: ListBuffer[AssociationData] = new ListBuffer[AssociationData]()
  private val associationDataLock: ReentrantReadWriteLock = new ReentrantReadWriteLock()

  /**
   * Get the [[ModelElementData]] of a type matching the provided parameters.
   *
   * @param name     name of the [[ModelElement]]
   * @param identity identity of the [[ModelElement]]
   * @return Future option of [[ModelElementData]] or None if not found
   */
  override protected def fetchModelElementData(name: String, identity: String): Future[Option[ModelElementData]] = {
    modelElementDataLock.readLock().lock()

    try {
      val data = modelElementDataBuffer.find((datum) => datum.name.equals(name) && datum.identity.equals(identity))
      Future.successful(data)
    } catch {
      case e: Exception => Future.failed(e)
    } finally {
      modelElementDataLock.readLock().unlock()
    }
  }

  /**
   * Get the [[ModelElementData]] of a type matching the provided parameters.
   *
   * @param identity identity of the [[ModelElement]]
   * @return Future option of [[ModelElementData]] or None if not found
   */
  override protected def fetchModelElementData(identity: String): Future[Set[ModelElementData]] = {
    modelElementDataLock.readLock().lock()

    try {
      val data = modelElementDataBuffer.filter((datum) => datum.identity.equals(identity))
      Future.successful(data.toSet)
    } catch {
      case e: Exception => Future.failed(e)
    } finally {
      modelElementDataLock.readLock().unlock()
    }
  }

  /**
   * Get the [[InstanceData]] elements instantiation a given type ([[ModelElement]]) specified by its name.
   * <p> [[InstanceData]] refers to its type by [[InstanceData.instanceOf]]
   *
   * @param typeName name of the [[ModelElement]] which instances must be returned
   * @return Future set of [[InstanceData]] matching the given type name
   */
  override protected def fetchInstanceDataOfType(typeName: String): Future[Set[InstanceData]] = {
    instanceDataLock.readLock().lock()

    try {
      val data = instanceDataBuffer.filter((datum) => datum.instanceOf.equals(typeName))
      Future.successful(data.toSet)
    } catch {
      case e: Exception => Future.failed(e)
    } finally {
      instanceDataLock.readLock().unlock()
    }
  }

  /**
   * Get the exact match of an [[InstanceData]] object by its instanceId.
   *
   * @param instanceId the [[InstanceData.instanceId]] of an instance
   * @return Future option of [[InstanceData]] or None if not found
   */
  override protected def fetchInstanceData(instanceId: String): Future[Option[InstanceData]] = {
    instanceDataLock.readLock().lock()

    try {
      val data = instanceDataBuffer.find((datum) => datum.instanceId.equals(instanceId))
      Future.successful(data)
    } catch {
      case e: Exception => Future.failed(e)
    } finally {
      instanceDataLock.readLock().unlock()
    }
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
  override protected def fetchRuleData(modelElementName: String, identity: String): Future[Set[RuleData]] = {
    ruleDataLock.readLock().lock()

    try {
      val data = ruleDataBuffer.filter((datum) => datum.modelElementName.equals(modelElementName) && datum.identity.equals(identity))
      Future.successful(data.toSet)
    } catch {
      case e: Exception => Future.failed(e)
    } finally {
      ruleDataLock.readLock().unlock()
    }
  }

  /**
   * Get all [[AttributeData]] referenced by a given instanceId which is provided by [[AttributeData.instanceId]].
   *
   * @param instanceId instanceId of the parent [[DeepInstance]]
   * @return Future set of all matching [[AttributeData]]
   */
  override protected def fetchAttributeData(instanceId: String): Future[Set[AttributeData]] = {
    attributeDataLock.readLock().lock()

    try {
      val data = attributeDataBuffer.filter((datum) => datum.instanceId.equals(instanceId))
      Future.successful(data.toSet)
    } catch {
      case e: Exception => Future.failed(e)
    } finally {
      attributeDataLock.readLock().unlock()
    }
  }

  /**
   * Get all [[ParentRelationData]] referenced by a given instanceId which is provided by [[ParentRelationData.instanceId]].
   *
   * @param instanceId instanceId of the parent [[DeepInstance]]
   * @return Future set of all matching [[ParentRelationData]]
   */
  override protected def fetchParentRelationData(instanceId: String): Future[Set[ParentRelationData]] = {
    parentRelationDataLock.readLock().lock()

    try {
      val data = parentRelationDataBuffer.filter((datum) => datum.instanceId.equals(instanceId))
      Future.successful(data.toSet)
    } catch {
      case e: Exception => Future.failed(e)
    } finally {
      parentRelationDataLock.readLock().unlock()
    }
  }

  /**
   * Get all [[AssociationData]] referenced by a given instanceId which is provided by [[AssociationData.instanceId]].
   *
   * @param instanceId instanceId of the parent [[DeepInstance]]
   * @return Future set of all matching [[AssociationData]]
   */
  override protected def fetchAssociationData(instanceId: String): Future[Set[AssociationData]] = {
    associationDataLock.readLock().lock()

    try {
      val data = associationDataBuffer.filter((datum) => datum.instanceId.equals(instanceId))
      Future.successful(data.toSet)
    } catch {
      case e: Exception => Future.failed(e)
    } finally {
      associationDataLock.readLock().unlock()
    }
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
  override protected def writeModelElementData(modelElementData: ModelElementData): Future[ModelElementData] = {
    modelElementDataLock.writeLock().lock()
    val _modelElementDataBuffer = modelElementDataBuffer.clone()

    try {
      val datum = modelElementDataBuffer.zipWithIndex.find((datum) => datum._1.name.equals(modelElementData.name) && datum._1.identity.equals(modelElementData.identity))
      datum match {
        case Some((_, index)) => {
          modelElementDataBuffer.update(index, modelElementData)
          Future.successful(modelElementData)
        }
        case None => {
          modelElementDataBuffer += modelElementData
          Future.successful(modelElementData)
        }
      }
    } catch {
      case e: Exception => {
        modelElementDataBuffer = _modelElementDataBuffer
        Future.failed(e)
      }
    } finally {
     modelElementDataLock.writeLock().unlock()
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
  override protected def writeInstanceData(instanceData: InstanceData): Future[InstanceData] = {
    instanceDataLock.writeLock().lock()
    val _instanceDataBuffer = instanceDataBuffer.clone()

    try {
      val datum = instanceDataBuffer.zipWithIndex.find((datum) => datum._1.instanceId.equals(instanceData.instanceId))
      datum match {
        case Some((_, index)) => {
          instanceDataBuffer.update(index, instanceData)
          Future.successful(instanceData)
        }
        case None => {
          instanceDataBuffer += instanceData
          Future.successful(instanceData)
        }
      }
    } catch {
      case e: Exception => {
        instanceDataBuffer = _instanceDataBuffer
        Future.failed(e)
      }
    } finally {
      instanceDataLock.writeLock().unlock()
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
  override protected def writeRuleData(diff: IODiff[RuleData]): Future[Set[RuleData]] = {
    def findRuleDataIndex(id: String): Future[Int] = {
      val data = ruleDataBuffer.zipWithIndex.filter((datum) => datum._1.id.equals(id)).map(datumWithIndex => datumWithIndex._2)
      if (data.isEmpty) {
        Future.failed(new NoSuchElementException("Could not find RuleData with the requested id!"))
      } else if (data.size == 1) {
        Future.successful(data.head)
      } else {
        throw new IllegalStateException("Found more than one RuleData with the requested id!")
      }
    }

    def addRuleData(datum: RuleData): Future[RuleData] = {
      if (datum.id.isEmpty || datum.id == "") {
        val fDatum = RuleData(IdentityProvider.newRandomId(), datum.modelElementName, datum.identity, datum.nativeValue, datum.typeOf)
        this.ruleDataBuffer += fDatum
        Future.successful(fDatum)
      } else {
        findRuleDataIndex(datum.id).transform {
          case Success(value) => Failure(new IllegalArgumentException("Id already exists!"))
          case Failure(cause) => {
            val fDatum = datum
            ruleDataBuffer += fDatum
            Success(fDatum)
          }
        }
      }
    }

    def removeRuleData(datum: RuleData): Future[RuleData] = {
      if (datum.id.isEmpty || datum.id == "") {
        throw new IllegalArgumentException("No id provided for removing RuleData!")
      } else {
        findRuleDataIndex(datum.id).transform {
          case Success(value) => Success(ruleDataBuffer.remove(value))
          case Failure(cause) => Failure(new Exception("Error trying to find index!", cause))
        }
      }
    }

    def updateRuleData(datum: RuleData): Future[RuleData] = {
      findRuleDataIndex(datum.id).transform {
        case Success(value) => {
          ruleDataBuffer.update(value, datum)
          Success(datum)
        }
        case Failure(cause) => Failure(new Exception("Error trying to find index!", cause))
      }
    }

    ruleDataLock.writeLock().lock()
    val _ruleDataBuffer = ruleDataBuffer.clone()

    try {
      for {
        adds <- Future.sequence(diff.toAdd.map((datum) => addRuleData(datum)))
        updates <- Future.sequence(diff.toUpdate.map((datum) => updateRuleData(datum)))
        deletes <- Future.sequence(diff.toDelete.map((datum) => removeRuleData(datum)))
      } yield {
        adds
      }
    } catch {
      case e: Exception => {
        ruleDataBuffer = _ruleDataBuffer
        Future.failed(e)
      }
    } finally {
      ruleDataLock.writeLock().unlock()
    }
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
  override protected def writeAttributeData(diff: IODiff[AttributeData]): Future[Set[AttributeData]] = {
    def findAttributeDataIndex(id: Long): Future[Int] = {
      val data = attributeDataBuffer.zipWithIndex.filter((datum) => datum._1.id.equals(id)).map(datumWithIndex => datumWithIndex._2)
      if (data.isEmpty) {
        Future.failed(new NoSuchElementException("Could not find AttributeData with the requested id!"))
      } else if (data.size == 1) {
        Future.successful(data.head)
      } else {
        throw new IllegalStateException("Found more than one AttributeData with the requested id!")
      }
    }

    def addAttributeData(datum: AttributeData): Future[AttributeData] = {
      if (datum.id == 0 || datum.id.isNaN) {
        val fDatum = AttributeData(attributeDataBuffer.map((datum) => datum.id).max + 1, datum.instanceId, datum.key, datum.value, datum.isFinal)
        attributeDataBuffer += fDatum
        Future.successful(fDatum)
      } else {
        findAttributeDataIndex(datum.id).transform {
          case Success(value) => Failure(new IllegalArgumentException("Id already exists!"))
          case Failure(cause) => {
            val fDatum = datum
            attributeDataBuffer += fDatum
            Success(fDatum)
          }
        }
      }
    }

    def removeAttributeData(datum: AttributeData): Future[AttributeData] = {
      if (datum.id == 0 || datum.id.isNaN) {
        throw new IllegalArgumentException("No id provided for removing AttributeData!")
      } else {
        findAttributeDataIndex(datum.id).transform {
          case Success(value) => Success(attributeDataBuffer.remove(value))
          case Failure(cause) => Failure(new Exception("Error trying to find index!", cause))
        }
      }
    }

    def updateAttributeData(datum: AttributeData): Future[AttributeData] = {
      findAttributeDataIndex(datum.id).transform {
        case Success(value) => {
          attributeDataBuffer.update(value, datum)
          Success(datum)
        }
        case Failure(cause) => Failure(new Exception("Error trying to find index!", cause))
      }
    }

    attributeDataLock.writeLock().lock()
    val _attributeDataBuffer = attributeDataBuffer.clone()

    try {
      for {
        adds <- Future.sequence(diff.toAdd.map((datum) => addAttributeData(datum)))
        updates <- Future.sequence(diff.toUpdate.map((datum) => updateAttributeData(datum)))
        deletes <- Future.sequence(diff.toDelete.map((datum) => removeAttributeData(datum)))
      } yield {
        adds
      }
    } catch {
      case e: Exception => {
        attributeDataBuffer = _attributeDataBuffer
        Future.failed(e)
      }
    } finally
    {
      attributeDataLock.writeLock().unlock()
    }
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
  override protected def writeParentRelationData(diff: IODiff[ParentRelationData]): Future[Set[ParentRelationData]] = {
    def findParentRelationDataIndex(id: Long): Future[Int] = {
      val data = parentRelationDataBuffer.zipWithIndex.filter((datum) => datum._1.id.equals(id)).map(datumWithIndex => datumWithIndex._2)
      if (data.isEmpty) {
        Future.failed(new NoSuchElementException("Could not find AttributeData with the requested id!"))
      } else if (data.size == 1) {
        Future.successful(data.head)
      } else {
        throw new IllegalStateException("Found more than one AttributeData with the requested id!")
      }
    }

    def addParentRelationData(datum: ParentRelationData): Future[ParentRelationData] = {
      if (datum.id == 0 || datum.id.isNaN) {
        val fDatum = ParentRelationData(parentRelationDataBuffer.map((datum) => datum.id).max + 1,datum.instanceId, datum.parentInstanceId)
        parentRelationDataBuffer += fDatum
        Future.successful(fDatum)
      } else {
        findParentRelationDataIndex(datum.id).transform {
          case Success(value) => Failure(new IllegalArgumentException("Id already exists!"))
          case Failure(cause) => {
            val fDatum = datum
            parentRelationDataBuffer += fDatum
            Success(fDatum)
          }
        }
      }
    }

    def removeParentRelationData(datum: ParentRelationData): Future[ParentRelationData] = {
      if (datum.id == 0 || datum.id.isNaN) {
        throw new IllegalArgumentException("No id provided for removing AttributeData!")
      } else {
        findParentRelationDataIndex(datum.id).transform {
          case Success(value) => Success(parentRelationDataBuffer.remove(value))
          case Failure(cause) => Failure(new Exception("Error trying to find index!", cause))
        }
      }
    }

    def updateParentRelationData(datum: ParentRelationData): Future[ParentRelationData] = {
      findParentRelationDataIndex(datum.id).transform {
        case Success(value) => {
         parentRelationDataBuffer.update(value, datum)
          Success(datum)
        }
        case Failure(cause) => Failure(new Exception("Error trying to find index!", cause))
      }
    }

    parentRelationDataLock.writeLock().lock()
    val _parentRelationDataBuffer = parentRelationDataBuffer.clone()

    try {
      for {
        adds <- Future.sequence(diff.toAdd.map((datum) => addParentRelationData(datum)))
        updates <- Future.sequence(diff.toUpdate.map((datum) => updateParentRelationData(datum)))
        deletes <- Future.sequence(diff.toDelete.map((datum) => removeParentRelationData(datum)))
      } yield {
        adds
      }
    } catch {
      case e: Exception => {
        parentRelationDataBuffer = _parentRelationDataBuffer
        Future.failed(e)
      }
    } finally {
      parentRelationDataLock.writeLock().unlock()
    }
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
  override protected def writeAssociationData(diff: IODiff[AssociationData]): Future[Set[AssociationData]] = {
    def findAssociationDataIndex(id: Long): Future[Int] = {
      val data = associationDataBuffer.zipWithIndex.filter((datum) => datum._1.id.equals(id)).map(datumWithIndex => datumWithIndex._2)
      if (data.isEmpty) {
        Future.failed(new NoSuchElementException("Could not find AttributeData with the requested id!"))
      } else if (data.size == 1) {
        Future.successful(data.head)
      } else {
        throw new IllegalStateException("Found more than one AttributeData with the requested id!")
      }
    }

    def addAssociationData(datum: AssociationData): Future[AssociationData] = {
      if (datum.id == 0 || datum.id.isNaN) {
        val fDatum = AssociationData(associationDataBuffer.map((datum) => datum.id).max + 1, datum.byRelation, datum.instanceId, datum.targetInstanceId, datum.isFinal)
        associationDataBuffer += fDatum
        Future.successful(fDatum)
      } else {
        findAssociationDataIndex(datum.id).transform {
          case Success(value) => Failure(new IllegalArgumentException("Id already exists!"))
          case Failure(cause) => {
            val fDatum = datum
            associationDataBuffer += fDatum
            Success(fDatum)
          }
        }
      }
    }

    def removeAssociationData(datum: AssociationData): Future[AssociationData] = {
      if (datum.id == 0 || datum.id.isNaN) {
        throw new IllegalArgumentException("No id provided for removing AttributeData!")
      } else {
        findAssociationDataIndex(datum.id).transform {
          case Success(value) => Success(associationDataBuffer.remove(value))
          case Failure(cause) => Failure(new Exception("Error trying to find index!", cause))
        }
      }
    }

    def updateAssociationData(datum: AssociationData): Future[AssociationData] = {
      findAssociationDataIndex(datum.id).transform {
        case Success(value) => {
          associationDataBuffer.update(value, datum)
          Success(datum)
        }
        case Failure(cause) => Failure(new Exception("Error trying to find index!", cause))
      }
    }

    associationDataLock.writeLock().lock()
    val _associationDataBuffer = associationDataBuffer.clone()

    try {
      for {
        adds <- Future.sequence(diff.toAdd.map((datum) => addAssociationData(datum)))
        updates <- Future.sequence(diff.toUpdate.map((datum) => updateAssociationData(datum)))
        deletes <- Future.sequence(diff.toDelete.map((datum) => removeAssociationData(datum)))
      } yield {
        adds
      }
    } catch {
      case e: Exception => {
        associationDataBuffer = _associationDataBuffer
        Future.failed(e)
      }
    } finally {
      associationDataLock.writeLock().unlock()
    }
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
  override protected def removeModelElementWithRules(modelElementName: String, identity: String): Future[Any] = {
    modelElementDataLock.writeLock().lock()
    ruleDataLock.writeLock().lock()

    val _modelElementDataBuffer = modelElementDataBuffer.clone()
    val _ruleDataBuffer = ruleDataBuffer.clone()

    try {
      modelElementDataBuffer.filterInPlace((datum) => !datum.name.equals(modelElementName) || !datum.identity.equals(identity))
      ruleDataBuffer.filterInPlace((datum) => !datum.modelElementName.equals(modelElementName) || !datum.identity.equals(identity))
      Future.successful()
    } catch {
      case e: Exception => {
        modelElementDataBuffer = _modelElementDataBuffer
        ruleDataBuffer = _ruleDataBuffer
        Future.failed(e)
      }
    } finally {
      ruleDataLock.writeLock().unlock()
      modelElementDataLock.writeLock().unlock()
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
  override protected def removeInstanceWithData(instanceId: String): Future[Any] = {
    instanceDataLock.writeLock().lock()
    attributeDataLock.writeLock().lock()
    parentRelationDataLock.writeLock().lock()
    associationDataLock.writeLock().lock()

    val _instanceDataBuffer = instanceDataBuffer.clone()
    val _attributeDataBuffer = attributeDataBuffer.clone()
    val _parentRelationDataBuffer = parentRelationDataBuffer.clone()
    val _associationDataBuffer = associationDataBuffer.clone()

    try {
      instanceDataBuffer.filterInPlace((datum) => !datum.instanceId.equals(instanceId))
      attributeDataBuffer.filterInPlace((datum) => !datum.instanceId.equals(instanceId))
      parentRelationDataBuffer.filterInPlace((datum) => !datum.instanceId.equals(instanceId))
      associationDataBuffer.filterInPlace((datum) => !datum.instanceId.equals(instanceId))
      Future.successful()
    } catch {
      case e: Exception => {
        instanceDataBuffer = _instanceDataBuffer
        attributeDataBuffer = _attributeDataBuffer
        parentRelationDataBuffer = _parentRelationDataBuffer
        associationDataBuffer = _associationDataBuffer
        Future.failed(e)
      }
    } finally {
      associationDataLock.writeLock().unlock()
      parentRelationDataLock.writeLock().unlock()
      attributeDataLock.writeLock().unlock()
      instanceDataLock.writeLock().unlock()
    }
  }

  override protected def queryInstanceDataByIdentityPrefixAndTypeName(identityPrefix: String, typeName: String): Future[Set[InstanceData]] = {
    instanceDataLock.readLock().lock()

    try {
      val data = instanceDataBuffer.filter((datum) => datum.identity.startsWith(identityPrefix) && datum.instanceOf.equals(typeName))
      Future.successful(data.toSet)
    } catch {
      case e: Exception => Future.failed(e)
    } finally {
      instanceDataLock.readLock().unlock()
    }
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
  override protected def queryTypes(query: String): Future[Set[ModelElementData]] = {
    modelElementDataLock.readLock()

    try {
      var data = modelElementDataBuffer.clone()

      // Handle easy case first
      if (query == "") {
        Future.successful(data)
      }

      // Handle harder case second
      val termStrings = query.split(" & ")
      val termTuples = termStrings.map((term) => term.split("="))

      for (termTuple <- termTuples) {
        if (termTuple(0) == "identity") {
          data.filterInPlace((datum) => datum.identity.equals(termTuple(1)))
        } else if (termTuple(0) == "name") {
          data.filterInPlace((datum) => datum.name.equals(termTuple(1)))
        } else {
          throw new IllegalArgumentException("Query did not satisfy syntax!")
        }
      }
      Future.successful(data.toSet)
    } catch {
      case e: Exception => Future.failed(e)
    } finally {
      modelElementDataLock.readLock().unlock()
    }
  }

  /**
   * Query all variants which are used by a known instance
   *
   * @return Future sequence of variant tuples in the format (variantTime, variantId)
   */
override protected def queryVariantsOfInstances(): Future[Seq[(Long, String)]] = {
  modelElementDataLock.readLock().lock()
  instanceDataLock.readLock().lock()

  try {
    val options = instanceDataBuffer.map((datum) => modelElementDataBuffer.find((element) => element.name.equals(datum.instanceOf) && element.identity.equals(datum.identity)))
    val data = options.filter((option) => option.isEmpty).map((option) => option.get).map((datum) => (datum.variantTime, datum.variantId))
    Future.successful(data.toSeq)
  } catch {
    case e: Exception => Future.failed(e)
  } finally {
    modelElementDataLock.readLock().unlock()
  }
}

  /**
   * Query all variants that are known. This includes all variants that are known by instances and the variant used by
   * the reference model which does not need to be instantiated.
   *
   * @return Future sequence of variant tuples in the format (variantTime, variantId)
   */
override protected def queryVariantsOfTypes(): Future[Seq[(Long, String)]] = {
  modelElementDataLock.readLock().lock()

  try {
    val data = modelElementDataBuffer.map((datum) => (datum.variantTime, datum.variantId)).toSet
    Future.successful(data.toSeq)
  } catch {
    case e: Exception => Future.failed(e)
  } finally {
    modelElementDataLock.readLock().unlock()
  }
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
override protected def queryVariantOccurrencesAndCount(): Future[Map[(Long, String), Int]] = {
  modelElementDataLock.readLock().lock()

  try {
    Future.successful(modelElementDataBuffer.groupMapReduce((datum) => (datum.variantTime, datum.variantId))(_ => 1)(_ + _))
  } catch {
    case e: Exception => Future.failed(e)
  } finally {
    modelElementDataLock.readLock().unlock()
  }
}
}
