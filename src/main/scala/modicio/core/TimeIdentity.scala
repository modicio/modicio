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

import modicio.core.datamappings.ModelElementData
import modicio.core.util.IdentityProvider

import java.time.Instant

case class TimeIdentity(variantTime: Long, runningTime: Long, versionTime: Long, variantId: String, runningId: String, versionId: String)

object TimeIdentity {

  def prettyPrint(time: Long): String = Instant.ofEpochMilli(time).toString

  def prettyPrintExtended(time: Long): String = time + " (" + prettyPrint(time) + ")"

  def fork(reference: TimeIdentity): TimeIdentity = TimeIdentity(
    reference.variantTime,
    reference.runningTime,
    reference.versionTime,
    reference.variantId,
    reference.runningId,
    reference.versionId
  )

  def create: TimeIdentity = {
    val randomPart = IdentityProvider.newRandomId()
    val timePart = IdentityProvider.newTimestampId()
    TimeIdentity(timePart, timePart, timePart, randomPart, randomPart, randomPart)
  }

  def fromModelElementData(med: ModelElementData): TimeIdentity =
    TimeIdentity(med.variantTime, med.runningTime, med.versionTime, med.variantId, med.runningId, med.versionId)


  def createFrom(reference: TimeIdentity): TimeIdentity = {
    TimeIdentity(
      reference.variantTime,
      reference.runningTime,
      reference.versionTime,
      reference.variantId,
      reference.runningId,
      IdentityProvider.newRandomId()
    )
  }

  def incrementVersion(reference: TimeIdentity): TimeIdentity = {
    TimeIdentity(
      reference.variantTime,
      reference.runningTime,
      IdentityProvider.newTimestampId(),
      reference.variantId,
      reference.runningId,
      IdentityProvider.newRandomId()
    )
  }

  def incrementRunning(reference: TimeIdentity, runningTime: Long, runningId: String): TimeIdentity = {
    TimeIdentity(
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
      variantTime,
      reference.runningTime,
      reference.versionTime,
      variantId,
      reference.runningId,
      reference.versionId
    )
  }

}