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

import modicio.codi.{Fragment, TypeIterator}
import modicio.api.JavaAPIConversions._

import java.util.Optional

class TypeIteratorJ(initialFragment: Fragment) extends TypeIterator(initialFragment) {

  def nameJ: java.lang.String = super.name

  def getJ: Optional[BaseJ] = convert(super.get)

  def getNameJ: Optional[java.lang.String] = super.getName

  def asDefinitionJ: Optional[DefinitionJ] = convert(super.asDefinition)

  def splitJ: Optional[TypeIteratorJ] = convert(super.split)

  def splitHandleJ: Optional[TypeHandleJ] = convert(super.splitHandle)

  def getAssociatedJ: java.util.Set[TypeHandleJ] = convert(super.getAssociated)


}
