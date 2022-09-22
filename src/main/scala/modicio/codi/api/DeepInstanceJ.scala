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

import modicio.api.JavaAPIConversions._
import modicio.codi.datamappings.api.{AssociationDataJ, AttributeDataJ}
import modicio.codi.rules.api.AttributeRuleJ
import modicio.codi.{DeepInstance, Shape}

import java.util.Optional
import scala.concurrent.ExecutionContext.Implicits.global
import scala.language.implicitConversions

class DeepInstanceJ(instanceId: java.lang.String, identity: java.lang.String, shape: ShapeJ, typeHandleJ: TypeHandleJ, registryJ: RegistryJ)
  extends DeepInstance(instanceId, identity, shape, typeHandleJ, registryJ.getRegistry) {

  def getTypeHandleJ: TypeHandleJ = super.getTypeHandle

  def getInstanceIdJ: java.lang.String = super.getInstanceId

  def unfoldJ(): java.util.concurrent.CompletableFuture[DeepInstanceJ] = super.unfold() map ( d => convert(d))

  def commitJ: java.util.concurrent.CompletableFuture[Any] = super.commit

  def getPolymorphSubtypeJ(typeName: java.lang.String): Optional[DeepInstanceJ] =
    convert(super.getPolymorphSubtype(typeName))

  def toDataJ: ImmutableShapeJ = super.toData

  def attributeMapJ(): java.util.Map[AttributeDataJ, AttributeRuleJ] = super.attributeMap().map(x => (convert(x._1), convert(x._2)))

  def deepAttributeMapJ(): java.util.Map[AttributeDataJ, AttributeRuleJ] = super.deepAttributeMap().map(x => (convert(x._1), convert(x._2)))

  def associationTypesJ: java.util.Set[java.lang.String] = super.associationTypes

  def associationRuleMapJ: java.util.Map[java.lang.String, java.util.Set[java.lang.String]] =
    super.associationRuleMap.map(x => (x._1, convert(x._2.toSet)))

  def deepAssociationRuleMapJ: java.util.Map[java.lang.String, java.util.Set[java.lang.String]] =
    super.deepAssociationRuleMap.map(x => (x._1, convert(x._2.toSet)))

  def assignValueJ(key: java.lang.String, value: java.lang.String): Boolean = super.assignValue(key, value)

  def assignDeepValueJ(key: java.lang.String, value: java.lang.String): Boolean = super.assignDeepValue(key, value)

  def valueJ(key: java.lang.String): Optional[java.lang.String] = super.value(key)

  def deepValueJ(key: java.lang.String): Optional[java.lang.String] = super.deepValue(key)

  def getAssociationsJ: java.util.Set[AssociationDataJ] = convert(super.getAssociations)

  def getDeepAssociationsJ: java.util.Set[AssociationDataJ] = convert(super.getDeepAssociations)

  def getTypeClosureJ: java.util.Set[java.lang.String] = super.getTypeClosure

  def getExtensionClosureJ: java.util.Set[DeepInstanceJ] = convert(super.getExtensionClosure)

  def associateJ(deepInstance: DeepInstanceJ, associateAs: java.lang.String, byRelation: java.lang.String): Boolean =
    super.associate(deepInstance, associateAs, byRelation)
}
