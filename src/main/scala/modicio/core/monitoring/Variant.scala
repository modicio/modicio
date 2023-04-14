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

import scala.collection.mutable.ListBuffer

/*
	A variant contains an unique variantId, variantTime and a list of versions.
 */
@JsonCodec
case class Variant(var variantTime: Long, var variantId: String, var versions: ListBuffer[Version] = new ListBuffer[Version]) {
	
	def addVersion(versionId: String, versionTime: Long): Version = {
		val newVersion = Version(versionId, versionTime)
		versions :+= newVersion
		newVersion
	}
	
	def deleteVersion(version: Version): Unit = {
		versions = versions.filter(_ != version)
	}
	
	def getVersion(versionId: String): Option[Version] = versions.find(vi => vi.versionId == versionId)
	
}
