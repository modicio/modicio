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

import modicio.codi.datamappings.{AssociationData, AttributeData, ExtensionData, FragmentData, InstanceData, RuleData}
import modicio.codi.{DeepInstance, Fragment, ImmutableShape, InstanceFactory, Registry, Shape, TypeFactory, TypeHandle}

import scala.collection.mutable
import scala.concurrent.{ExecutionContext, Future}

abstract class AbstractPersistentRegistry(typeFactory: TypeFactory, instanceFactory: InstanceFactory)
                                         (implicit executionContext: ExecutionContext)
  extends Registry(typeFactory, instanceFactory) {

  protected case class IODiff[T](toDelete: Set[T], toAdd: Set[T], toUpdate: Set[T])


  protected def fetchFragmentData(name: String, identity: String): Future[Option[FragmentData]]

  protected def fetchFragmentData(identity: String): Future[Set[FragmentData]]

  protected def fetchInstanceDataOfType(typeName: String): Future[Set[InstanceData]]

  protected def fetchInstanceData(instanceId: String): Future[Option[InstanceData]]

  protected def fetchRuleData(fragmentName: String, identity: String): Future[Set[RuleData]]

  protected def fetchAttributeData(instanceId: String): Future[Set[AttributeData]]

  protected def fetchExtensionData(instanceId: String): Future[Set[ExtensionData]]

  protected def fetchAssociationData(instanceId: String): Future[Set[AssociationData]]


  protected def writeFragmentData(fragmentData: FragmentData): Future[FragmentData]

  protected def writeInstanceData(instanceData: InstanceData): Future[InstanceData]

  protected def writeRuleData(diff: IODiff[RuleData]): Future[Set[RuleData]]

  protected def writeAttributeData(diff: IODiff[AttributeData]): Future[Set[AttributeData]]

  protected def writeExtensionData(diff: IODiff[ExtensionData]): Future[Set[ExtensionData]]

  protected def writeAssociationData(diff: IODiff[AssociationData]): Future[Set[AssociationData]]


  protected def removeFragmentWithRules(fragmentName: String, identity: String): Future[Any]

  protected def removeInstanceWithData(instanceId: String): Future[Any]


  /*
   * ***********************************************************
   * Implementation of abstract members
   * ***********************************************************
   */

  override def getType(name: String, identity: String): Future[Option[TypeHandle]] = {
    for {
      fragmentDataOption <- fetchFragmentData(name, identity)
      ruleData <- fetchRuleData(name, identity)
    } yield {
      if(fragmentDataOption.isDefined){
        val fragmentData = fragmentDataOption.get;
        Some(typeFactory.loadType(fragmentData, ruleData))
      }else {
        None
      }
    }
  }

  override def getReferences: Future[Set[TypeHandle]] = {
    for {
      fragmentDataSet <- fetchFragmentData(Fragment.REFERENCE_IDENTITY)
      ruleDataSet <- Future.sequence(fragmentDataSet.map(f => fetchRuleData(f.name, f.identity)))
    } yield {
      if(fragmentDataSet.size != ruleDataSet.size){
        Future.failed(new Exception("Not matching fragment and rule-set relations"))
      }
      fragmentDataSet.map(fragmentData => (fragmentData, {
        ruleDataSet.find(rules => rules.exists(ruleData =>
          ruleData.fragmentName == fragmentData.name && ruleData.identity == fragmentData.identity))
      })).map(modelTuple => {
        val (fragmentData, ruleDataOption) = modelTuple
        val ruleData: Set[RuleData] = ruleDataOption.getOrElse(Set())
        typeFactory.loadType(fragmentData, ruleData)
      }) //++ baseModels.values.map(_.createHandle).toSet
    }
  }

  override protected def setNode(typeHandle: TypeHandle): Future[Unit] = {
    fetchRuleData(typeHandle.getFragment.name, typeHandle.getTypeIdentity) flatMap (oldRuleData => {
      val (fragmentData, ruleData) = typeHandle.getFragment.toData
      for {
        _ <- writeRuleData(applyRules(oldRuleData, ruleData))
        _ <- writeFragmentData(fragmentData)
      } yield {}
    })
  }

  override def get(instanceId: String): Future[Option[DeepInstance]] = {
    fetchInstanceData(instanceId) flatMap (instanceDataOption => {
      if(instanceDataOption.isDefined){
        val instanceData = instanceDataOption.get
        for {
          attributeData <- fetchAttributeData(instanceId)
          extensionData <- fetchExtensionData(instanceId)
          associationData <- fetchAssociationData(instanceId)
          typeOption <- getType(instanceData.instanceOf, instanceData.identity)
        } yield {
          if(typeOption.isDefined){
            val associations = mutable.Set[AssociationData]()
            associations.addAll(associationData)
            val shape = new Shape(attributeData,  associations, extensionData)
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

  override def getAll(typeName: String): Future[Set[DeepInstance]] = {
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
  override def setInstance(deepInstance: DeepInstance): Future[Unit] = {
    val data = deepInstance.toData
    val (instanceData, attributeData, associationData, extensionData) = (ImmutableShape unapply data).get
    get(deepInstance.getInstanceId) flatMap (oldInstanceOption => {
      val (_, oldExtensionData: Set[ExtensionData], oldAttributeData: Set[AttributeData], oldAssociationData: Set[AssociationData]) = {
        if(oldInstanceOption.isDefined){
          oldInstanceOption.get.toData
        }else{
          (null, Set[ExtensionData](), Set[AttributeData](), Set[AssociationData]())
        }
      }
      for {
        _ <- writeInstanceData(instanceData)
        _ <- writeExtensionData(applyUpdate[ExtensionData](oldExtensionData, extensionData, _.id == 0))
        _ <- writeAssociationData(applyUpdate[AssociationData](oldAssociationData, associationData, _.id == 0))
        _ <- writeAttributeData(applyUpdate[AttributeData](oldAttributeData, attributeData, _.id == 0))
      } yield {}
    })
  }

  /**
   * Remove parts of the model in a way producing a minimal number of overall deletions while trying to retain integrity
   * <p> <strong>Experimental Feature</strong>
   * <p> In case of a reference-identity Fragment, the Fragment is deleted only. In consequence, children pointing to that Fragment
   * and other Fragments associating this Fragment become invalid and must be repaired manually.
   * <p> In case of a singleton-identity Fragment, the whole singleton-fork of the Fragment tree and the corresponding
   * [[DeepInstance DeepInstance]] tree are removed.
   * <p> In case of a user-space identity, nothing happens yet => TODO
   *
   * @param name     of the [[Fragment Fragment]] trying to remove
   * @param identity of the [[Fragment Fragment]] trying to remove
   * @return
   */
  override def autoRemove(name: String, identity: String): Future[Any] = {

    if (identity == Fragment.REFERENCE_IDENTITY) {
      //In case of reference identity, remove model-element locally. FIXME The model may become invalid
      removeFragmentWithRules(name, identity)

    } else if (identity == Fragment.SINGLETON_IDENTITY) {
      //In case of a singleton identity fragment

      val singletonInstanceId = DeepInstance.deriveSingletonInstanceId(identity, name)

      //get the associated singleton deep instance
      get(singletonInstanceId) flatMap  (deepInstanceOption => {
        if (deepInstanceOption.isDefined) {
          //unfold the singleton deep-instance

          deepInstanceOption.get.unfold() flatMap (unfoldedInstance => {
            val extensions = unfoldedInstance.getTypeHandle.getFragment.getParents

            //delete all parent model-elements of the singleton deep-instance
            //delete the actual deep-instance and trigger deletion of its parents
            for {
              _ <- removeInstanceWithData(singletonInstanceId)
              _ <- Future.sequence(extensions.map(extension => autoRemove(extension.name, Fragment.SINGLETON_IDENTITY)))
              _ <- removeFragmentWithRules(name, identity)
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


  private def applyRules(old: Set[RuleData], in: Set[RuleData]): IODiff[RuleData] = {
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
  private def applyUpdate[T](old: Set[T], in: Set[T], isNew: T => Boolean): IODiff[T] = {
    val toAdd = in.filter(isNew)
    val toChange = in.diff(toAdd)
    val toDelete = old.diff(toChange)
    val toUpdate = toChange.diff(toDelete)
    IODiff(toDelete, toAdd, toUpdate)
  }

}
