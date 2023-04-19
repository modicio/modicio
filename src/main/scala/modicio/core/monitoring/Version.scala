/**
 * Copyright 2023 Minji Kim
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

package modicio.core.monitoring

import io.circe.generic.JsonCodec

import scala.collection.mutable

/*
	A Version includes an unique versionId, versionTime, a number of Instances and a list of  associations with another classes
 */
@JsonCodec
case class Version(var versionId: String,
									 var versionTime: Long,
									 var instances: mutable.Set[Set[String]] = mutable.Set[Set[String]](),
									 var associations: mutable.Set[Set[String]] = mutable.Set[Set[String]](),
									 var parentRelations: mutable.Set[Set[String]] = mutable.Set[Set[String]](),
									) {
	def increase(instanceId: String, instanceTime: Long): Unit = {
		val newInstance = Set(instanceId, instanceTime.toString)
		if (!this.instances.exists(i => i.contains(instanceId))) {
			instances.addOne(newInstance)
		}
	}
	
	def decrease(instance: Set[String]): Unit = {
		this.instances = instances.filter(i => !i.equals(instance))
	}
	
	def addParentRelations(parentName: String, parentIdentity: String): Unit = {
		val newParentRelation = Set(parentName, parentIdentity)
		if (!parentRelations.contains(newParentRelation)) {
			parentRelations.addOne(newParentRelation)
		}
	}
	
	def addAssociated(typeName: String, variantId: String, versionId: String): Unit = {
		val newAssociation = Set(typeName, variantId, versionId)
		if (!associations.contains(newAssociation)) {
			associations.addOne(newAssociation)
		}
	}
}
