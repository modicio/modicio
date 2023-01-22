/**
 * Copyright 2022 Karl Kegel, Johannes Gröschel
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

import modicio.core.util.IdentityProvider
import modicio.core.datamappings.{AssociationData, AttributeData, InstanceData, ModelElementData, ParentRelationData, PluginData, RuleData}
import modicio.core.{DeepInstance, InstanceFactory, ModelElement, TypeFactory}

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Success}

/**
 * Non-persistent implementation of the [[AbstractPersistentRegistry]] simulating database behaviour via
 * Scala Buffer implementations.
 * <p> This class is used for testing and benchmarking mainly, but can also be used in prototypical scenarios where
 * the [[SimpleMapRegistry]] is not sufficient due to their superficial behaviour.
 */
class VolatilePersistentRegistry(typeFactory: TypeFactory, instanceFactory: InstanceFactory, core: VolatilePersistentRegistryCore = new VolatilePersistentRegistryCore())
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
   * Get the [[ModelElementData]] of a type matching the provided parameters.
   *
   * @param name     name of the [[ModelElement]]
   * @param identity identity of the [[ModelElement]]
   * @return Future option of [[ModelElementData]] or None if not found
   */
  override protected def fetchModelElementData(name: String, identity: String): Future[Option[ModelElementData]] = {
    Future({
      core.modelElementDataLock.readLock().lock()

      try {
        val data = core.modelElementDataBuffer.find((datum) => datum.name.equals(name) && datum.identity.equals(identity))
        data
      } catch {
        case e: Exception => throw e
      } finally {
        core.modelElementDataLock.readLock().unlock()
      }
    })
  }

  /**
   * Get the [[ModelElementData]] of a type matching the provided parameters.
   *
   * @param identity identity of the [[ModelElement]]
   * @return Future set of [[ModelElementData]] matching the given identity
   */
  override protected def fetchModelElementData(identity: String): Future[Set[ModelElementData]] = {
    Future({
      core.modelElementDataLock.readLock().lock()

      try {
        val data = core.modelElementDataBuffer.filter((datum) => datum.identity.equals(identity))
        data.toSet
      } catch {
        case e: Exception => throw e
      } finally {
        core.modelElementDataLock.readLock().unlock()
      }
    })
  }

  /**
   * Get the [[InstanceData]] elements instantiation a given type ([[ModelElement]]) specified by its name.
   * <p> [[InstanceData]] refers to its type by [[InstanceData.instanceOf]]
   *
   * @param typeName name of the [[ModelElement]] which instances must be returned
   * @return Future set of [[InstanceData]] matching the given type name
   */
  override protected def fetchInstanceDataOfType(typeName: String): Future[Set[InstanceData]] = {
  Future({
      core.instanceDataLock.readLock().lock()

      try {
        val data = core.instanceDataBuffer.filter((datum) => datum.instanceOf.equals(typeName))
       data.toSet
      } catch {
        case e: Exception => throw e
      } finally {
        core.instanceDataLock.readLock().unlock()
      }
    })
  }

  /**
   * Get the exact match of an [[InstanceData]] object by its instanceId.
   *
   * @param instanceId the [[InstanceData.instanceId]] of an instance
   * @return Future option of [[InstanceData]] or None if not found
   */
  override protected def fetchInstanceData(instanceId: String): Future[Option[InstanceData]] = {
    Future({
      core.instanceDataLock.readLock().lock()

      try {
        val data = core.instanceDataBuffer.find((datum) => datum.instanceId.equals(instanceId))
        data
      } catch {
        case e: Exception => throw e
      } finally {
        core.instanceDataLock.readLock().unlock()
      }
    })
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
    Future({
      core.ruleDataLock.readLock().lock()

      try {
        val data = core.ruleDataBuffer.filter((datum) => datum.modelElementName.equals(modelElementName) && datum.identity.equals(identity))
        data.toSet
      } catch {
        case e: Exception => throw e
      } finally {
        core.ruleDataLock.readLock().unlock()
      }
    })
  }

  /**
   * Get all [[AttributeData]] referenced by a given instanceId which is provided by [[AttributeData.instanceId]].
   *
   * @param instanceId instanceId of the parent [[DeepInstance]]
   * @return Future set of all matching [[AttributeData]]
   */
  override protected def fetchAttributeData(instanceId: String): Future[Set[AttributeData]] = {
    Future({
      core.attributeDataLock.readLock().lock()

      try {
        val data = core.attributeDataBuffer.filter((datum) => datum.instanceId.equals(instanceId))
        data.toSet
      } catch {
        case e: Exception => throw e
      } finally {
        core.attributeDataLock.readLock().unlock()
      }
    })
  }

  /**
   * Get all [[ParentRelationData]] referenced by a given instanceId which is provided by [[ParentRelationData.instanceId]].
   *
   * @param instanceId instanceId of the parent [[DeepInstance]]
   * @return Future set of all matching [[ParentRelationData]]
   */
  override protected def fetchParentRelationData(instanceId: String): Future[Set[ParentRelationData]] = {
    Future({
      core.parentRelationDataLock.readLock().lock()

      try {
        val data = core.parentRelationDataBuffer.filter((datum) => datum.instanceId.equals(instanceId))
        data.toSet
      } catch {
        case e: Exception => throw e
      } finally {
        core.parentRelationDataLock.readLock().unlock()
      }
    })
  }

  /**
   * Get all [[AssociationData]] referenced by a given instanceId which is provided by [[AssociationData.instanceId]].
   *
   * @param instanceId instanceId of the parent [[DeepInstance]]
   * @return Future set of all matching [[AssociationData]]
   */
  override protected def fetchAssociationData(instanceId: String): Future[Set[AssociationData]] = {
    Future({
      core.associationDataLock.readLock().lock()

      try {
        val data = core.associationDataBuffer.filter((datum) => datum.instanceId.equals(instanceId))
        data.toSet
      } catch {
        case e: Exception => throw e
      } finally {
        core.associationDataLock.readLock().unlock()
      }
    })
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
    Future({
      core.modelElementDataLock.writeLock().lock()
      val _modelElementDataBuffer = core.modelElementDataBuffer.clone()

      try {
        val datum = core.modelElementDataBuffer.zipWithIndex.find((datum) => datum._1.name.equals(modelElementData.name) && datum._1.identity.equals(modelElementData.identity))
        datum match {
          case Some((_, index)) => {
            core.modelElementDataBuffer.update(index, modelElementData)
            modelElementData
          }
          case None => {
            core.modelElementDataBuffer += modelElementData
            modelElementData
          }
        }
      } catch {
        case e: Exception => {
          core.modelElementDataBuffer = _modelElementDataBuffer
          throw e
        }
      } finally {
        core.modelElementDataLock.writeLock().unlock()
      }
    })
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
    Future({
      core.instanceDataLock.writeLock().lock()
      val _instanceDataBuffer = core.instanceDataBuffer.clone()

      try {
        val datum = core.instanceDataBuffer.zipWithIndex.find((datum) => datum._1.instanceId.equals(instanceData.instanceId))
        datum match {
          case Some((_, index)) => {
            core.instanceDataBuffer.update(index, instanceData)
            instanceData
          }
          case None => {
            core.instanceDataBuffer += instanceData
            instanceData
          }
        }
      } catch {
        case e: Exception => {
          core.instanceDataBuffer = _instanceDataBuffer
          throw e
        }
      } finally {
        core.instanceDataLock.writeLock().unlock()
      }
    })
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
    def findRuleDataIndex(id: String): Int = {
      val data = core.ruleDataBuffer.zipWithIndex.filter((datum) => datum._1.id.equals(id)).map(datumWithIndex => datumWithIndex._2)
      if (data.isEmpty) {
       -1
      } else if (data.size == 1) {
        data.head
      } else {
        throw new IllegalStateException("Found more than one RuleData with the requested id!")
      }
    }

    def addRuleData(datum: RuleData): RuleData = {
      if (datum.id.isEmpty || datum.id == "" || datum.id == "0") {
        val fDatum = RuleData(IdentityProvider.newRandomId(), datum.modelElementName, datum.identity, datum.nativeValue, datum.typeOf)
        this.core.ruleDataBuffer += fDatum
        fDatum
      } else {
        val _index = findRuleDataIndex(datum.id)
        if (_index < 0) {
          val fDatum = datum
          core.ruleDataBuffer += fDatum
          fDatum
        } else {
          throw new IllegalArgumentException("Id already exists!")
        }
      }
    }

    def removeRuleData(datum: RuleData): RuleData = {
      if (datum.id.isEmpty || datum.id == "" || datum.id == "0") {
        throw new IllegalArgumentException("No id provided for removing RuleData!")
      } else {
        val _index = findRuleDataIndex(datum.id)
        if (_index < 0) {
          throw new NoSuchElementException("Couldn't find element!")
        } else {
          core.ruleDataBuffer.remove(_index)
        }
      }
    }

    def updateRuleData(datum: RuleData): RuleData = {
      if (datum.id.isEmpty || datum.id == "" || datum.id == "0") {
        throw new IllegalArgumentException("No id provided for updating RuleData!")
      } else {
        val _index = findRuleDataIndex(datum.id)
        if (_index < 0) {
          throw new NoSuchElementException("Couldn't find element!")
        } else {
          core.ruleDataBuffer.update(_index, datum)
          datum
        }
      }
    }

    Future({
      core.ruleDataLock.writeLock().lock()
      val _ruleDataBuffer = core.ruleDataBuffer.clone()

      try {
        val adds = diff.toAdd.map((datum) => addRuleData((datum)))
        val updates = diff.toUpdate.map((datum) => updateRuleData(datum))
        val deletes = diff.toDelete.map((datum) => removeRuleData(datum))
        adds
      } catch {
        case e: Exception => {
          core.ruleDataBuffer = _ruleDataBuffer
          throw e
        }
      } finally {
        core.ruleDataLock.writeLock().unlock()
      }
    })
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
    def findAttributeDataIndex(id: Long): Int = {
      val data = core.attributeDataBuffer.zipWithIndex.filter((datum) => datum._1.id.equals(id)).map(datumWithIndex => datumWithIndex._2)
      if (data.isEmpty) {
        -1
      } else if (data.size == 1) {
        data.head
      } else {
        throw new IllegalStateException("Found more than one AttributeData with the requested id!")
      }
    }

    def addAttributeData(datum: AttributeData): AttributeData = {
      if (datum.id == 0 || datum.id.isNaN) {
        val fDatum = AttributeData(core.attributeDataBuffer.map((datum) => datum.id).addOne(0).max + 1, datum.instanceId, datum.key, datum.value, datum.isFinal)
        core.attributeDataBuffer += fDatum
        fDatum
      } else {
        val _index = findAttributeDataIndex(datum.id)
        if (_index < 0) {
          val fDatum = datum
          core.attributeDataBuffer += fDatum
          fDatum
        } else {
          throw new IllegalArgumentException("Id already exists!")
        }
      }
    }

    def removeAttributeData(datum: AttributeData): AttributeData = {
      if (datum.id == 0 || datum.id.isNaN) {
        throw new IllegalArgumentException("No id provided for removing AttributeData!")
      } else {
        val _index = findAttributeDataIndex(datum.id)
        if (_index < 0) {
          throw new NoSuchElementException("Couldn't find element!")
        } else {
          core.attributeDataBuffer.remove(_index)
        }
      }
    }

    def updateAttributeData(datum: AttributeData): AttributeData = {
      if (datum.id == 0 || datum.id.isNaN) {
        throw new IllegalArgumentException("No id provided for updating AttributeData!")
      } else {
        val _index = findAttributeDataIndex(datum.id)
        if (_index < 0) {
          throw new NoSuchElementException("Couldn't find element!")
        } else {
          core.attributeDataBuffer.update(_index, datum)
          datum
        }
      }
    }

    Future({
      core.attributeDataLock.writeLock().lock()
      val _attributeDataBuffer = core.attributeDataBuffer.clone()

      try {
        val adds = diff.toAdd.map((datum) => addAttributeData(datum))
        val updates = diff.toUpdate.map((datum) => updateAttributeData(datum))
        val deletes = diff.toDelete.map((datum) => removeAttributeData(datum))
        adds
      } catch {
        case e: Exception => {
          core.attributeDataBuffer = _attributeDataBuffer
          throw e
        }
      } finally {
        core.attributeDataLock.writeLock().unlock()
      }
    })
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
    def findParentRelationDataIndex(id: Long): Int = {
      val data = core.parentRelationDataBuffer.zipWithIndex.filter((datum) => datum._1.id.equals(id)).map(datumWithIndex => datumWithIndex._2)
      if (data.isEmpty) {
        -1
      } else if (data.size == 1) {
        data.head
      } else {
        throw new IllegalStateException("Found more than one ParentRelationData with the requested id!")
      }
    }

    def addParentRelationData(datum: ParentRelationData): ParentRelationData = {
      if (datum.id == 0 || datum.id.isNaN) {
        val fDatum = ParentRelationData(core.parentRelationDataBuffer.map((datum) => datum.id).addOne(0).max + 1,datum.instanceId, datum.parentInstanceId)
        core.parentRelationDataBuffer += fDatum
        fDatum
      } else {
        val _index = findParentRelationDataIndex(datum.id)
        if (_index < 0) {
          val fDatum = datum
          core.parentRelationDataBuffer += fDatum
          fDatum
        } else {
          throw new IllegalArgumentException("Id already exists!")
        }
      }
    }

    def removeParentRelationData(datum: ParentRelationData): ParentRelationData = {
      if (datum.id == 0 || datum.id.isNaN) {
        throw new IllegalArgumentException("No id provided for removing ParentRelationData!")
      } else {
        val _index = findParentRelationDataIndex(datum.id)
        if (_index < 0) {
          throw new NoSuchElementException("Couldn't find element!")
        } else {
          core.parentRelationDataBuffer.remove(_index)
        }
      }
    }

    def updateParentRelationData(datum: ParentRelationData): ParentRelationData = {
      if (datum.id == 0 || datum.id.isNaN) {
        throw new IllegalArgumentException("No id provided for removing ParentRelationData!")
      } else {
        val _index = findParentRelationDataIndex(datum.id)
        if (_index < 0) {
          throw new NoSuchElementException("Couldn't find element!")
        } else {
          core.parentRelationDataBuffer.update(_index, datum)
          datum
        }
      }
    }

    Future({
      core.parentRelationDataLock.writeLock().lock()
      val _parentRelationDataBuffer = core.parentRelationDataBuffer.clone()

      try {
        val adds = diff.toAdd.map((datum) => addParentRelationData(datum))
        val updates = diff.toUpdate.map((datum) => updateParentRelationData(datum))
        val deletes =diff.toDelete.map((datum) => removeParentRelationData(datum))
        adds
      } catch {
        case e: Exception => {
          core.parentRelationDataBuffer = _parentRelationDataBuffer
          throw e
        }
      } finally {
        core.parentRelationDataLock.writeLock().unlock()
      }
    })
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
    def findAssociationDataIndex(id: Long): Int = {
      val data = core.associationDataBuffer.zipWithIndex.filter((datum) => datum._1.id.equals(id)).map(datumWithIndex => datumWithIndex._2)
      if (data.isEmpty) {
        -1
      } else if (data.size == 1) {
        data.head
      } else {
        throw new IllegalStateException("Found more than one AttributeData with the requested id!")
      }
    }

    def addAssociationData(datum: AssociationData): AssociationData = {
      if (datum.id == 0 || datum.id.isNaN) {
        val fDatum = AssociationData(core.associationDataBuffer.map((datum) => datum.id).addOne(0).max + 1, datum.byRelation, datum.instanceId, datum.targetInstanceId, datum.isFinal)
        core.associationDataBuffer += fDatum
        fDatum
      } else {
        val _index = findAssociationDataIndex(datum.id)
        if (_index < 0) {
          val fDatum = datum
          core.associationDataBuffer += fDatum
          fDatum
        } else {
          throw new IllegalArgumentException("Id already exists!")
        }
      }
    }

    def removeAssociationData(datum: AssociationData): AssociationData = {
      if (datum.id == 0 || datum.id.isNaN) {
        throw new IllegalArgumentException("No id provided for removing AttributeData!")
      } else {
        val _index = findAssociationDataIndex(datum.id)
        if (_index < 0) {
          throw new NoSuchElementException("Couldn't find element!")
        } else {
          core.associationDataBuffer.remove(_index)
        }
      }
    }

    def updateAssociationData(datum: AssociationData): AssociationData = {
      if (datum.id == 0 || datum.id.isNaN) {
        throw new IllegalArgumentException("No id provided for updating AssociationData!")
      } else {
        val _index = findAssociationDataIndex(datum.id)
        if (_index < 0) {
          throw new NoSuchElementException("Couldn't find element!")
        } else {
          core.associationDataBuffer.update(_index, datum)
          datum
        }
      }
    }

    Future({
      core.associationDataLock.writeLock().lock()
      val _associationDataBuffer = core.associationDataBuffer.clone()

      try {
        val adds = diff.toAdd.map((datum) => addAssociationData(datum))
        val updates = diff.toUpdate.map((datum) => updateAssociationData(datum))
        val deletes = diff.toDelete.map((datum) => removeAssociationData(datum))
        adds
      } catch {
        case e: Exception => {
          core.associationDataBuffer = _associationDataBuffer
          throw e
        }
      } finally {
        core.associationDataLock.writeLock().unlock()
      }
    })
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
    Future({
      core.modelElementDataLock.writeLock().lock()
      core.ruleDataLock.writeLock().lock()

      val _modelElementDataBuffer = core.modelElementDataBuffer.clone()
      val _ruleDataBuffer = core.ruleDataBuffer.clone()

      try {
        core.modelElementDataBuffer.filterInPlace((datum) => !datum.name.equals(modelElementName) || !datum.identity.equals(identity))
        core.ruleDataBuffer.filterInPlace((datum) => !datum.modelElementName.equals(modelElementName) || !datum.identity.equals(identity))
        Success()
      } catch {
        case e: Exception => {
          core.modelElementDataBuffer = _modelElementDataBuffer
          core.ruleDataBuffer = _ruleDataBuffer
          throw e
        }
      } finally {
        core.ruleDataLock.writeLock().unlock()
        core.modelElementDataLock.writeLock().unlock()
      }
    })
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
    Future({
      core.instanceDataLock.writeLock().lock()
      core.attributeDataLock.writeLock().lock()
      core.parentRelationDataLock.writeLock().lock()
      core.associationDataLock.writeLock().lock()

      val _instanceDataBuffer = core.instanceDataBuffer.clone()
      val _attributeDataBuffer = core.attributeDataBuffer.clone()
      val _parentRelationDataBuffer = core.parentRelationDataBuffer.clone()
      val _associationDataBuffer = core.associationDataBuffer.clone()

      try {
        core.instanceDataBuffer.filterInPlace((datum) => !datum.instanceId.equals(instanceId))
        core.attributeDataBuffer.filterInPlace((datum) => !datum.instanceId.equals(instanceId))
        core.parentRelationDataBuffer.filterInPlace((datum) => !datum.instanceId.equals(instanceId))
        core.associationDataBuffer.filterInPlace((datum) => !datum.instanceId.equals(instanceId))
        Success()
      } catch {
        case e: Exception => {
          core.instanceDataBuffer = _instanceDataBuffer
          core.attributeDataBuffer = _attributeDataBuffer
          core.parentRelationDataBuffer = _parentRelationDataBuffer
          core.associationDataBuffer = _associationDataBuffer
          throw e
        }
      } finally {
        core.associationDataLock.writeLock().unlock()
        core.parentRelationDataLock.writeLock().unlock()
        core.attributeDataLock.writeLock().unlock()
        core.instanceDataLock.writeLock().unlock()
      }
    })
  }

  override protected def queryInstanceDataByIdentityPrefixAndTypeName(identityPrefix: String, typeName: String): Future[Set[InstanceData]] = {
    Future({
      core.instanceDataLock.readLock().lock()

      try {
        val data = core.instanceDataBuffer.filter((datum) => datum.identity.startsWith(identityPrefix) && datum.instanceOf.equals(typeName))
        data.toSet
      } catch {
        case e: Exception => throw e
      } finally {
        core.instanceDataLock.readLock().unlock()
      }
    })
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
    Future({
      core.modelElementDataLock.readLock()

      try {
        val data = core.modelElementDataBuffer.clone()

        // Handle easy case first
        if (query == "") {
          Future.successful(data)
        }

        // Handle harder case second
        val termStrings = query.split(" & ")
        val termTuples = termStrings.map((term) => term.split("="))

        for (termTuple <- termTuples) {
          if (termTuple(0).toLowerCase() == "identity") {
            data.filterInPlace((datum) => datum.identity.equals(termTuple(1)))
          } else if (termTuple(0).toLowerCase() == "name") {
            data.filterInPlace((datum) => datum.name.equals(termTuple(1)))
          } else {
            throw new IllegalArgumentException("Query did not satisfy syntax!")
          }
        }
        data.toSet
      } catch {
        case e: Exception => throw e
      } finally {
        core.modelElementDataLock.readLock().unlock()
      }
    })
  }

  /**
   * Query all variants which are used by a known instance
   *
   * @return Future sequence of variant tuples in the format (variantTime, variantId)
   */
override protected def queryVariantsOfInstances(): Future[Seq[(Long, String)]] = {
  Future({
    core.modelElementDataLock.readLock().lock()
    core.instanceDataLock.readLock().lock()

    try {
      val options = core.instanceDataBuffer.map((datum) => core.modelElementDataBuffer.find((element) => element.name.equals(datum.instanceOf) && element.identity.equals(datum.identity)))
      val data = options.filter((option) => option.isEmpty).map((option) => option.get).map((datum) => (datum.variantTime, datum.variantId))
      data.toSeq
    } catch {
      case e: Exception => throw e
    } finally {
      core.modelElementDataLock.readLock().unlock()
    }
  })
}

  /**
   * Query all variants that are known. This includes all variants that are known by instances and the variant used by
   * the reference model which does not need to be instantiated.
   *
   * @return Future sequence of variant tuples in the format (variantTime, variantId)
   */
override protected def queryVariantsOfTypes(): Future[Seq[(Long, String)]] = {
  Future({
    core.modelElementDataLock.readLock().lock()

    try {
      val data = core.modelElementDataBuffer.map((datum) => (datum.variantTime, datum.variantId)).toSet
      data.toSeq
    } catch {
      case e: Exception => throw e
    } finally {
      core.modelElementDataLock.readLock().unlock()
    }
  })
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
  Future({
    core.modelElementDataLock.readLock().lock()

    try {
      core.modelElementDataBuffer.groupMapReduce((datum) => (datum.variantTime, datum.variantId))(_ => 1)(_ + _)
    } catch {
      case e: Exception => throw e
    } finally {
      core.modelElementDataLock.readLock().unlock()
    }
  })
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
  override protected def fetchPluginData(modelElementName: String, identity: String): Future[Set[PluginData]] = ???

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
  override protected def writePluginData(diff: IODiff[PluginData]): Future[Set[PluginData]] = ???
}
