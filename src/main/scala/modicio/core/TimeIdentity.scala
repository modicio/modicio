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

package modicio.core

import modicio.core.util.IdentityProvider

case class TimeIdentity(localId: Long, variantTime: Long, runningTime: Long, versionTime: Long, variantId: String, runningId: String, versionId: String)

object TimeIdentity {

  def create: TimeIdentity = {
    val randomPart = IdentityProvider.newRandomId()
    val timePart = IdentityProvider.newTimestampId()
    TimeIdentity(localId = 0, timePart, timePart, timePart, randomPart, randomPart, randomPart)
  }

  def createFrom(reference: TimeIdentity): TimeIdentity = {
    TimeIdentity(
      localId = 0,
      reference.variantTime,
      reference.runningTime,
      IdentityProvider.newTimestampId(),
      reference.variantId,
      reference.runningId,
      IdentityProvider.newRandomId()
    )
  }

  def incrementVersion(reference: TimeIdentity, runningTime: Long, runningId: String): TimeIdentity = {
    TimeIdentity(
      reference.localId,
      reference.variantTime,
      runningTime,
      IdentityProvider.newTimestampId(),
      reference.variantId,
      runningId,
      IdentityProvider.newRandomId()
    )
  }

  def incrementRunning(reference: TimeIdentity, runningTime: Long, runningId: String): TimeIdentity = {
    TimeIdentity(
      reference.localId,
      reference.variantTime,
      runningTime,
      reference.versionTime,
      reference.variantId,
      runningId,
      reference.versionId
    )
  }

  def incrementVariant(reference: TimeIdentity, variantTime: Long, variantId: String): TimeIdentity = {
    TimeIdentity(
      reference.localId,
      variantTime,
      reference.runningTime,
      reference.versionTime,
      variantId,
      reference.runningId,
      reference.versionId
    )
  }

}