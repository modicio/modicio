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

import modicio.codi.datamappings.RuleData
import modicio.util.Identity

/**
 * <p>Overall abstract class to represent any rule held by a [[Definition Definition]].
 * <p>Instances of this class and their concrete implementations are stored as [[RuleData]] by
 * their serialised value.
 * <p>The nativeValue of a Rule must contain all information regarding the Rule and the concrete Rule-type. The nativeValue
 * must follow a definition of the native-language.
 * <br />
 * <br />
 * <p> The abstract Rule class handles the id value, which must always be the first value of a nativeValue of any concrete
 * Rule, separated by a ":" from the rest. If no id is known, the nativeValue must start with the ":" directly.
 *
 * @see [[Definition]]<p>[[RuleData]]
 * @param nativeValue he string representation in the native-language format
 */
abstract class Rule(private[modicio] val nativeValue: String) {

  /**
   * <p>The id serves as a technical identifier of the Rule. For in-memory usecases it is optional but must be set for
   * persistence purposes.
   */
  val id: String = parseId(nativeValue)

  /**
   * <p>Abstract method to serialise a concrete Rule.
   * <p>The serialisation must produce a native-language representation which can vice-versa be used as constructor
   * parameter for this Rule.
   *
   * @return native-language specification of this Rule
   */
  def serialise(): String

  /**
   * <p>Abstract method to serialise a concrete Rule to a human-readable format.
   * <p><strong>This method must only be used for human-readable logs and outputs and not for technical purposes!</strong>
   *
   * @return human-readable serialisation of this Rule
   */
  def serialiseSimple(): String

  /**
   * <p>Abstract method to verify a concrete Rule against their structural native-language representation.
   * <p>This method does not check, if the Rule makes any sense as part of the runtime-model. It is only checked that the
   * concrete Rule is well defined in a way that it can be parsed and serialised without issues.
   *
   * @return Boolean - if the Rule is well-defined following the native-language specification
   */
  def verify(): Boolean

  /**
   * <p>Abstract method to create a deep copy of a concrete Rule.
   * <p>If the Rule contains any identity values that should be replaced by an other identity in the copy, a parameter
   * is provided.
   *
   * @param identity identity of the context the Rule is used in
   * @return Rule - new deep-copy
   */
  def fork(identity: String): Rule

  /**
   * <p>Helper to parse the id of the Rule from its serialised representation
   * <p>If no id is specified, i.e. the serialisation starts with ":". the [[Rule#UNKNOWN_ID UNKNOWN_ID]]
   * is returned instead.
   *
   * @param nativeValue serialised rule representation
   * @return String of id value
   */
  protected def parseId(nativeValue: String): String = {
    val givenId = nativeValue.split(":").headOption
    if (givenId.isEmpty || givenId.get.nonEmpty) {
      givenId.get
    } else {
      Rule.UNKNOWN_ID
    }
  }

  def isPolymorphEqual(rule: Rule): Boolean = ???

  def getDataType: Int

}

/**
 * Rule companion object containing static constants and setting for Rule classes in a specific runtime context.
 *
 * @see [[Rule]]
 */
object Rule {

  //flag to set if unique ids should be assigned automatically, if no explicit id is specified by the client.
  private var isAutoID = false

  /**
   * <p> Generates the UNKNOWN_ID value which is used, if no explicit Rule id is provided.
   * <p> if AutoID is enabled, this method will retrieve a new unique random identifier form the [[Identity Identity]]
   * object.
   *
   * @return String - unique random id if isAutoID, "0" otherwise
   */
  def UNKNOWN_ID: String = {
    if (isAutoID) {
      Identity.create()
    } else {
      "0"
    }
  }

  /**
   * <p> Method to enable the autoID functionality for all Rules
   */
  def enableAutoID(): Unit = isAutoID = true

}