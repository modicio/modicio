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
package modicio.core.values.api

import modicio.core.values.{ConcreteAssociation, ValueDescriptor}

class ConcreteAssociationJ(nativeValue: java.lang.String) extends ConcreteAssociation(nativeValue) with ValueDescriptorJ {

  def targetIdentityJ: java.lang.String = super.targetIdentity

  def targetModelElementJ: java.lang.String = super.targetModelElement

  def getValueDescriptor: ValueDescriptor = this
}
