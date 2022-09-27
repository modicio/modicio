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
package modicio.core.api

import modicio.api.JavaAPIConversions._
import modicio.core.Shape
import modicio.core.datamappings.api.{AssociationDataJ, AttributeDataJ, ExtensionDataJ}

import java.util.Optional
import scala.collection.mutable

class ShapeJ(attributes: java.util.Set[AttributeDataJ],
             associations: java.util.Set[AssociationDataJ],
             extensions: java.util.Set[ExtensionDataJ])

  extends Shape(attributes map convert, mutable.Set.from(convert(associations)), extensions map convert){
  def getAttributeJ(key: java.lang.String): Optional[AttributeDataJ] = convert(super.getAttribute(key))

  def getAttributesJ: java.util.Set[AttributeDataJ] = convert(super.getAttributes)

  def getAssociationsJ: java.util.Set[AssociationDataJ] = convert(super.getAssociations)

  def removeAssociationJ(associationId: Long): Unit = super.removeAssociation(associationId)

  def addAssociationJ(association: AssociationDataJ): Unit = super.addAssociation(association)

  def getExtensionsJ: java.util.Set[ExtensionDataJ] = convert(super.getExtensions)
}
