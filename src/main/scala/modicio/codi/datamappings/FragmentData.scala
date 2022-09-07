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

import modicio.codi.{BaseModel, Fragment, Node}

/**
 * <p>Tuple to represent a [[Fragment Fragment]] for serialisation.
 * <p>This class is produced by the Fragment itself.
 * <p>This class represents only [[Node Nodes]] in
 * the reference case, but Nodes and concrete [[BaseModel BaseModels]] in the instantiated case. This is because
 * BaseModels follow their static implementation in code in the reference case (immutable). Nodes are in this case dynamic
 * which is indicated by the reference identity.
 * After forking a Fragment, BaseModels and Nodes are represented in data equally and immutable by this tuple.
 *
 * <p> Name and identity form a unique combination which serves as ID.
 *
 * @param name       the name of the type-fragment
 * @param identity   identity value
 * @param isTemplate flag if the Fragment describes a template-type or instantiable-type
 */
case class FragmentData(name: String, identity: String, isTemplate: Boolean, isNode: Boolean)
