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
package modicio.codi.api

import modicio.codi.datamappings.{AssociationData, AttributeData, ExtensionData, InstanceData}
import modicio.codi.rules.AttributeRule
import modicio.codi.{DeepInstance, Shape, TypeHandle}

import scala.collection.mutable
import scala.concurrent.Future

class DeepInstanceJ(instanceId: java.lang.String, identity: java.lang.String, shape: Shape, typeHandleJ: TypeHandleJ, registryJ: RegistryJ)
  extends DeepInstance(instanceId, identity, shape, typeHandleJ, registryJ.getRegistry) {

  def getTypeHandle: TypeHandle = super.getTypeHandle

  def getInstanceId: String = super.getInstanceId

  def unfold(): Future[DeepInstance] = super.unfold()

  def commit: Future[Any] = super.commit

  def getPolymorphSubtype(typeName: String): Option[DeepInstance] = super.getPolymorphSubtype(typeName)

  def toData: (InstanceData, Set[ExtensionData], Set[AttributeData], Set[AssociationData]) = super.toData

  def attributeMap(): Map[AttributeData, AttributeRule] = super.attributeMap()

  def deepAttributeMap(): Map[AttributeData, AttributeRule] = super.deepAttributeMap()

  def associationTypes: Set[String] = super.associationTypes

  def associationRuleMap: Map[String, mutable.Set[String]] = super.associationRuleMap

  def deepAssociationRuleMap: Map[String, mutable.Set[String]] = super.deepAssociationRuleMap

  def assignValue(key: String, value: String): Boolean = super.assignValue(key, value)

  def assignDeepValue(key: String, value: String): Boolean = super.assignDeepValue(key, value)

  def value(key: String): Option[String] = super.value(key)

  def deepValue(key: String): Option[String] = super.deepValue(key)

  def getAssociations: Set[AssociationData] = super.getAssociations

  def getDeepAssociations: Set[AssociationData] = super.getDeepAssociations

  def getTypeClosure: Set[String] = super.getTypeClosure

  def getExtensionClosure: Set[DeepInstance] = super.getExtensionClosure

  def associate(deepInstance: DeepInstance, associateAs: String, byRelation: String): Boolean = super.associate(deepInstance, associateAs, byRelation)
}
