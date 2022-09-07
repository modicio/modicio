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

import modicio.codi.{DeepInstance, Shape}
import modicio.codi.rules.AttributeRule

/**
 * <p>Tuple to represent the instantiated data following an [[AttributeRule AttributeRule]] as part of a
 * concrete [[Shape Shape]].
 *
 * @param id         unique technical id of this data tuple, initialised with 0
 * @param instanceId id of the [[DeepInstance DeepInstance]] this attribute is part of
 * @param key        name of the attribute specified by the corresponding [[AttributeRule AttributeRule]]
 * @param value      value of the attribute serialised as String. This value is mutable explicitly!
 * @param isFinal    if the attribute is part of the model specification and is conceptual immutable
 */
case class AttributeData(id: Long, instanceId: String, key: String, var value: String, isFinal: Boolean)
