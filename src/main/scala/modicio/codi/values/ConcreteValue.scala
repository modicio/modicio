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
package modicio.codi.values

import modicio.codi.rules.{AssociationRule, AttributeRule, RuleDataType}
import modicio.codi.{ModelElement, Rule}

/**
 * @param nativeValue he string representation in the native-language format
 */
class ConcreteValue(nativeValue: String) extends Rule(nativeValue) {

  val valueType: String = parseType
  val valueName: String = parseName

  val valueDescriptor: ValueDescriptor = parseDescriptor

  def getValueType: String = valueType

  override def getDataType: Int = RuleDataType.VALUE

  def isAttributeValue: Boolean = valueType == ConcreteValue.ATTRIBUTE_VALUE

  def isAssociationValue: Boolean = valueType == ConcreteValue.ASSOCIATION_VALUE

  private def parseType: String = nativeValue.split(":")(1)

  private def parseName: String = nativeValue.split(":")(2).split("\\(")(0)

  private def parseDescriptor: ValueDescriptor = {
    val rawDescriptor = "(" + nativeValue.split("\\(")(1)
    if(isAttributeValue){
      new ConcreteAttribute(rawDescriptor)
    }else{
      new ConcreteAssociation(rawDescriptor)
    }
  }

  def getAttributeDescriptor: ConcreteAttribute = {
    valueDescriptor match {
      case valueDescriptor: ConcreteAttribute => valueDescriptor
      case _ => throw new UnsupportedOperationException()
    }
  }

  def getAssociationDescriptor: ConcreteAssociation = {
    valueDescriptor match {
      case valueDescriptor: ConcreteAssociation => valueDescriptor
      case _ => throw new UnsupportedOperationException()
    }
  }

  /**
   * <p>Implementation of [[Rule#serialise Rule.serialise()]]
   *
   * @return String of serialised rule
   */
  override def serialise(): String = {
    id + ":" + valueType + ":" + valueDescriptor.serialize
  }

  /**
   * <p>Implementation of [[Rule#serialiseSimple Rule.serialiseSimple()]].
   * <p>This method must only be used for human-readable logs and outputs and not for technical purposes!
   *
   * @return String of simplified serialisation
   */
  override def serialiseSimple(): String = {
    valueType + " := " + valueDescriptor.serializeSimple
  }

  /**
   * <p>Implementation of [[Rule#verify Rule.verify()]]
   * <p>FIXME not implemented yet, returns always true
   *
   * @return Boolean - if the rule is valid in terms of producing a valid serialisation
   */
  override def verify(): Boolean = {
    true
  }

  /**
   * <p>Implementation of [[Rule#fork Rule.fork()]].
   *
   * @param identity the identity of an instantiated [[ModelElement ModelElement]]
   * @return [[Rule Rule]] - copy of this Rule with changed identity value and new ID
   */
  override def fork(identity: String): Rule =
    ConcreteValue.create(valueType, valueName, valueDescriptor.toSeq, Some(Rule.UNKNOWN_ID))


  /**
   * TODO doc
   * @param concreteValue
   * @return
   */
  def isPolymorphEqual(concreteValue: ConcreteValue): Boolean = {
    valueType == concreteValue.valueType && valueName == concreteValue.valueName
  }

  def concreteOf(rule: Rule): Boolean = {
    rule match {
      case rule: AttributeRule => matchesAttributeRule(rule)
      case rule: AssociationRule => matchesAssociationRule(rule)
      case _ => false
    }
  }

  private def matchesAttributeRule(rule: AttributeRule): Boolean = {
    rule.name == valueName
  }

  private def matchesAssociationRule(rule: AssociationRule): Boolean = {
    rule.associationName == valueName
  }

}

object ConcreteValue {

  val ATTRIBUTE_VALUE = "ATTRIBUTE"
  val ASSOCIATION_VALUE = "LINK"

  /**
   * TODO add documentation
   *
   * @param valueType
   * @param valueName
   * @param valueDescriptor
   * @param idOption
   * @return
   */
  def create(valueType: String, valueName: String, valueDescriptor: Seq[String],
             idOption: Option[String] = None): ConcreteValue = {
    var id = Rule.UNKNOWN_ID
    if (idOption.isDefined) id = idOption.get
    new ConcreteValue(id + ":" + valueType + ":" + valueName + ":" + ValueDescriptor.fromSeq(valueDescriptor))
  }

}