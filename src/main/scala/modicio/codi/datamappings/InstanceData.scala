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
package modicio.codi.datamappings

import modicio.codi.{DeepInstance, ModelElement, Shape}

/**
 * <p> Tuple to represent a [[DeepInstance DeepInstance]] for serialisation.
 * <p> All objectified members of the DeepInstance i.e. its [[Shape Shape]] is serialised independently.
 * Such serialised members refer to this class via a given instanceId.
 *
 * @param instanceId unique technical identifier of a [[DeepInstance DeepInstance]]
 * @param instanceOf name of the [[ModelElement ModelElement]] this instance is type of
 * @param identity   identity of the instantiated instance-type clabject
 */
case class InstanceData(instanceId: String, instanceOf: String, identity: String)
