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
package modicio.core.datamappings

import modicio.core.{DeepInstance, Shape}
import modicio.core.rules.AssociationRule

/**
 * <p>Tuple to represent the instantiated data following an [[AssociationRule AssociationRule]] as part of a
 * concrete [[Shape Shape]].
 *
 * @param id               unique technical id of this data tuple, initialised with 0
 * @param byRelation       name of the relation given by the corresponding [[AssociationRule AssociationRule]]
 * @param instanceId       id of the [[DeepInstance DeepInstance]] this association is starting
 * @param targetInstanceId id of the [[DeepInstance DeepInstance]] this association is pointing at
 * @param isFinal          flag is the association is final and can not be edited by a client
 */
case class AssociationData(id: Long, byRelation: String, instanceId: String, targetInstanceId: String, isFinal: Boolean)