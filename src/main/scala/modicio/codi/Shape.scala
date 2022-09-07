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
package modicio.codi

import modicio.codi.datamappings.{AssociationData, AttributeData, ExtensionData}

import scala.collection.mutable

/**
 * @param attributes
 * @param associations
 * @param extensions
 */
class Shape(attributes: Set[AttributeData], associations: mutable.Set[AssociationData], extensions: Set[ExtensionData]){

  def getAttribute(key: String): Option[AttributeData] = attributes.find(_.key == key)

  def getAttributes: Set[AttributeData] = attributes

  def getAssociations: Set[AssociationData] = associations.toSet

  def removeAssociation(associationId: Long): Unit = {
    val associationOption = associations.find(_.id == associationId)
    if(associationOption.isDefined){
      associations.remove(associationOption.get)
    }
  }

  def addAssociation(association: AssociationData): Unit = associations.add(association)

  def getExtensions: Set[ExtensionData] = extensions

}
