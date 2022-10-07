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
package modicio.core.rules

import modicio.core.{ModelElement, Rule}
import modicio.core.datamappings.RuleData

/**
 * <p> A concrete [[Rule Rule]] implementation to represent parentRelations in the native unlinked model.
 * This Rule does not logically distinguish between reference parentRelations and instantiated parentRelations.
 * <br />
 * <br />
 * <p> <strong>String format: "ID:PARENT_IDENTITY:PARENT_NAME"</strong>
 * <P> where ID is the unique technical identifier of the [[Rule Rule]]
 * <p> where PARENT_IDENTITY is the identity value of the parent [[ModelElement ModelElement]]
 * <p> where TARGET_NAME is the name of the parent [[ModelElement ModelElement]]
 *
 * @see [[Rule]]<p>[[RuleData]]
 * @param nativeValue the string representation in the native-language format
 */
class ParentRelationRule(nativeValue: String) extends Rule(nativeValue) {

  val parentName: String = parseParentName(nativeValue)
  val parentIdentity: String = parseParentIdentity(nativeValue)

  /**
   * <p>Helper to retrieve the parent name from the serialised value
   *
   * @param nativeValue serialised rule representation
   * @return String of parent name
   */
  private def parseParentName(nativeValue: String): String = nativeValue.split(":")(2)

  /**
   * <p>Helper to retrieve the parent identity from the serialised value
   *
   * @param nativeValue serialised rule representation
   * @return String of parent identity
   */
  private def parseParentIdentity(nativeValue: String): String = nativeValue.split(":")(1)

  /**
   * <p>Implementation of [[Rule#serialise Rule.serialise()]]
   *
   * @return String of serialised rule
   */
  override def serialise(): String = {
    id + ":" + parentIdentity + ":" + parentName
  }

  /**
   * <p>Implementation of [[Rule#serialiseSimple Rule.serialiseSimple()]].
   * <p>This method must only be used for human-readable logs and outputs and not for technical purposes!
   *
   * @return String of simplified serialisation
   */
  override def serialiseSimple(): String = {
    "..." + id.takeRight(5) + ":" + parentIdentity + ":" + parentName
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
   * <p> In this case of an ParentRelationRule, especially the parent identity (which is in the reference case set to the reference identity)
   * is replaced by the provided new identity of the instantiation.
   *
   * @param identity the identity of an instantiated [[ModelElement ModelElement]]
   * @return [[Rule Rule]] - copy of this Rule with changed identity value and new ID
   */
  override def fork(identity: String): Rule = ParentRelationRule.create(parentName, identity, Some(Rule.UNKNOWN_ID))

  override def getDataType: Int = RuleDataType.EXTENSION

}

/**
 * <p> ParentRelationRule companion object for the static factory creator.
 *
 * @see [[ParentRelationRule ParentRelationRule]]
 */
object ParentRelationRule {

  /**
   * <p> Create a new [[ParentRelationRule ParentRelationRule]] from raw data.
   * <p> This serves as a factory method for the ParentRelationRule.
   * <p> If an empty idOption is provided, the id is set to [[Rule#UNKNOWN_ID UNKNOWN_ID]] and must be
   * changed manually.
   *
   * @param parentName     name of the parent [[ModelElement ModelElement]]
   * @param parentIdentity identity of the parent [[ModelElement ModelElement]]
   * @param idOption       id value if known, set to default otherwise
   * @return ParentRelationRule created from provided values
   */
  def create(parentName: String, parentIdentity: String, idOption: Option[String] = None): ParentRelationRule = {
    var id = Rule.UNKNOWN_ID
    if (idOption.isDefined) id = idOption.get
    new ParentRelationRule(id + ":" + parentIdentity + ":" + parentName)
  }
}