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
package modicio.codi.rules

import modicio.codi.{ModelElement, Rule}
import modicio.codi.datamappings.RuleData

/**
 * <p> A concrete [[Rule Rule]] implementation to represent attributes in the native unlinked model.
 * <br />
 * <br />
 * <p> <strong>String format: "ID:NAME:DATATYPE:NON_EMPTY"</strong>
 * <P> where ID is the unique technical identifier of the [[Rule Rule]]
 * <p> where NAME is the arbitrary name of the attribute
 * <p> where DATATYPE is the type-value of the attribute ["STRING", "NUMBER", "DATETIME"]
 * <P> where NON_EMPTY describes if a concrete attribute must always have a non-empty value
 *
 * @see [[Rule]]<p>[[RuleData]]
 * @param nativeValue the string representation in the native-language format
 */
class AttributeRule(nativeValue: String) extends Rule(nativeValue) {

  val name: String = parseName(nativeValue)
  val datatype: String = parseDatatype(nativeValue)
  val nonEmpty: Boolean = parseNonEmpty(nativeValue)

  /**
   * <p>Helper to retrieve the attribute name from the serialised value
   *
   * @param nativeValue serialised rule representation
   * @return String of attribute name
   */
  private def parseName(nativeValue: String): String = nativeValue.split(":")(1)

  /**
   * <p>Helper to retrieve the datatype from the serialised value
   * TODO data-types should be represented by an enum value
   *
   * @param nativeValue serialised rule representation
   * @return String of datatype value
   */
  private def parseDatatype(nativeValue: String): String = nativeValue.split(":")(2)

  /**
   * <p>Helper to retrieve the nonEmpty value from the serialised value
   *
   * @param nativeValue serialised rule representation
   * @return Boolean if nonEmpty
   */
  private def parseNonEmpty(nativeValue: String): Boolean = nativeValue.split(":")(3).toBoolean

  /**
   * <p>Implementation of [[Rule#serialise Rule.serialise()]]
   *
   * @return String of serialised rule
   */
  override def serialise(): String = {
    id + ":" + name + ":" + datatype + ":" + nonEmpty.toString
  }

  /**
   * <p>Implementation of [[Rule#serialiseSimple Rule.serialiseSimple()]].
   * <p>This method must only be used for human-readable logs and outputs and not for technical purposes!
   *
   * @return String of simplified serialisation
   */
  override def serialiseSimple(): String = {
    "..." + id.takeRight(5) + ":" + name + ":" + datatype + ":" + nonEmpty.toString
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
  override def fork(identity: String): Rule = AttributeRule.create(name, datatype, nonEmpty, Some(Rule.UNKNOWN_ID))

  /**
   *
   * @param rule
   * @return
   */
  override def isPolymorphEqual(rule: Rule): Boolean = {
    rule match {
      case rule: AttributeRule => {
        //TODO some sophisticated reasoning and verification must be made here!
        rule.name == name && rule.datatype == datatype
      }
      case _ => false
    }
  }

  override def getDataType: Int = RuleDataType.ATTRIBUTE

}

/**
 * <p> AssociationRule companion object for the static factory creator.
 */
object AttributeRule {

  /**
   * <p> Create a new [[AttributeRule AttributeRule]] from raw data.
   * <p> This serves as a factory method for the AttributeRule.
   * <p> If an empty idOption is provided, the id is set to [[Rule#UNKNOWN_ID UNKNOWN_ID]] and must be
   * changed manually.
   *
   * @param name     name of the attribute
   * @param datatype datatype of the attribute, see [[AttributeRule AttributeRule]]
   * @param nonEmpty see [[AttributeRule AttributeRule]]
   * @param idOption id value if known, set to default otherwise
   * @return AttributeRules created from provided values
   */
  def create(name: String, datatype: String, nonEmpty: Boolean, idOption: Option[String] = None): AttributeRule = {
    var id = Rule.UNKNOWN_ID
    if (idOption.isDefined) id = idOption.get
    new AttributeRule(id + ":" + name + ":" + datatype + ":" + nonEmpty)
  }
}
