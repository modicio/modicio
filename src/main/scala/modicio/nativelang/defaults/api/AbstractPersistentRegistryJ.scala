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
package modicio.nativelang.defaults.api

import modicio.api.JavaAPIConversions._
import modicio.codi.Registry
import modicio.codi.api.{InstanceFactoryJ, RegistryJ, TypeFactoryJ}
import modicio.codi.datamappings._
import modicio.codi.datamappings.api._
import modicio.nativelang.defaults.AbstractPersistentRegistry

import java.util.Optional
import java.util.concurrent.CompletableFuture
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

abstract class AbstractPersistentRegistryJ(typeFactory: TypeFactoryJ, instanceFactoryJ: InstanceFactoryJ)
  extends AbstractPersistentRegistry(typeFactory, instanceFactoryJ) with RegistryJ {

  override def getRegistry: Registry = this

  protected case class IODiffJ[T](toDelete: java.util.Set[T], toAdd: java.util.Set[T], toUpdate: java.util.Set[T])

  implicit def convertDiff[A, T](value: IODiff[A])(implicit f: A => T): IODiffJ[T] = IODiffJ[T](value.toDelete.map(f), value.toAdd.map(f), value.toUpdate.map(f))

  override protected def fetchFragmentData(name: String, identity: String): Future[Option[FragmentData]] = futureConvertOption(fetchFragmentDataJ(name, identity))

  override protected def fetchFragmentData(identity: String): Future[Set[FragmentData]] = fetchFragmentDataJ(identity)

  override protected def fetchInstanceDataOfType(typeName: String): Future[Set[InstanceData]] = fetchInstanceDataOfTypeJ(typeName)

  override protected def fetchInstanceData(instanceId: String): Future[Option[InstanceData]] = futureConvertOption(fetchInstanceDataJ(instanceId))

  override protected def fetchRuleData(fragmentName: String, identity: String): Future[Set[RuleData]] = fetchRuleDataJ(fragmentName, identity)

  override protected def fetchAttributeData(instanceId: String): Future[Set[AttributeData]] =
    fetchAttributeDataJ(instanceId)

  override protected def fetchExtensionData(instanceId: String): Future[Set[ExtensionData]] =
    fetchExtensionDataJ(instanceId)

  override protected def fetchAssociationData(instanceId: String): Future[Set[AssociationData]] =
    fetchAssociationDataJ(instanceId)

  override protected def writeFragmentData(fragmentData: FragmentData): Future[FragmentData] =
    writeFragmentDataJ(fragmentData) map convert

  override protected def writeInstanceData(instanceData: InstanceData): Future[InstanceData] =
    convert(writeInstanceDataJ(instanceData) map convert)

  override protected def writeRuleData(diff: IODiff[RuleData]): Future[Set[RuleData]] =
    writeRuleDataJ(convertDiff(diff))

  override protected def writeAttributeData(diff: IODiff[AttributeData]): Future[Set[AttributeData]] =
    writeAttributeDataJ(convertDiff(diff))

  override protected def writeExtensionData(diff: IODiff[ExtensionData]): Future[Set[ExtensionData]] =
    writeExtensionDataJ(convertDiff(diff))

  override protected def writeAssociationData(diff: IODiff[AssociationData]): Future[Set[AssociationData]] =
    writeAssociationDataJ(convertDiff(diff))

  override protected def removeFragmentWithRules(fragmentName: String, identity: String): Future[Any] = removeFragmentWithRulesJ(fragmentName, identity)

  override protected def removeInstanceWithData(instanceId: String): Future[Any] = removeInstanceWithDataJ(instanceId)


  protected def fetchFragmentDataJ(name: java.lang.String, identity: java.lang.String): CompletableFuture[Optional[FragmentDataJ]]
  protected def fetchFragmentDataJ(identity: java.lang.String): CompletableFuture[java.util.Set[FragmentDataJ]]
  protected def fetchInstanceDataOfTypeJ(typeName: java.lang.String): CompletableFuture[java.util.Set[InstanceDataJ]]
  protected def fetchInstanceDataJ(instanceId: java.lang.String): CompletableFuture[Optional[InstanceDataJ]]
  protected def fetchRuleDataJ(fragmentName: java.lang.String, identity: java.lang.String): CompletableFuture[java.util.Set[RuleDataJ]]
  protected def fetchAttributeDataJ(instanceId: java.lang.String): CompletableFuture[java.util.Set[AttributeDataJ]]
  protected def fetchExtensionDataJ(instanceId: java.lang.String): CompletableFuture[java.util.Set[ExtensionDataJ]]
  protected def fetchAssociationDataJ(instanceId: java.lang.String): CompletableFuture[java.util.Set[AssociationDataJ]]
  protected def writeFragmentDataJ(fragmentData: FragmentDataJ): CompletableFuture[FragmentDataJ]
  protected def writeInstanceDataJ(instanceData: InstanceDataJ): CompletableFuture[InstanceDataJ]
  protected def writeRuleDataJ(diff: IODiffJ[RuleDataJ]): CompletableFuture[java.util.Set[RuleDataJ]]
  protected def writeAttributeDataJ(diff: IODiffJ[AttributeDataJ]): CompletableFuture[java.util.Set[AttributeDataJ]]
  protected def writeExtensionDataJ(diff: IODiffJ[ExtensionDataJ]): CompletableFuture[java.util.Set[ExtensionDataJ]]
  protected def writeAssociationDataJ(diff: IODiffJ[AssociationDataJ]): CompletableFuture[java.util.Set[AssociationDataJ]]
  protected def removeFragmentWithRulesJ(fragmentName: java.lang.String, identity: java.lang.String): CompletableFuture[Any]
  protected def removeInstanceWithDataJ(instanceId: java.lang.String): CompletableFuture[Any]

}
