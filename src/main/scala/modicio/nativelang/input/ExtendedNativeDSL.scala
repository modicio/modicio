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
package modicio.nativelang.input

import io.circe.syntax._
import io.circe.generic.JsonCodec
import modicio.codi.datamappings.{AssociationData, AttributeData, ExtensionData, InstanceData}

case class ExtendedNativeDSL(definition: NativeDSL, configuration: Seq[(InstanceData, Set[ExtensionData], Set[AttributeData], Set[AssociationData])])