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
package modicio.core

import modicio.core.datamappings.RuleData
import modicio.core.rules.{AssociationRule, AttributeRule, ExtensionRule}
import modicio.core.util.Observable
import modicio.core.values.ConcreteValue
import modicio.verification.DefinitionVerifier
import scala.collection.mutable

/**
 * <p> The Definition class is a concrete [[Base Base]] implementation representing the set of
 * [[Rule Rules]] a [[ModelElement ModelElement]] possesses.
 * <p> The Definition encapsulates the Rules in individual sets and provides an api to edit the rule-set. Note that the
 * Definition class itself does not ensure validity of the resulting model, especially in an extension-hierarchy. If a new Rule is
 * added or an old Rule is removed, the provided [[DefinitionVerifier DefinitionVerifier]] is called with
 * the temporary altered rule-set. If the DefinitionVerifier accepts accepts the rule-set as valid, the changes are applied.
 *
 * <p> The [[Observable Observable]] trait is implemented in this class to enable clients to observe rule-set changes
 * if Rules are added and removed.
 *
 * @param definitionVerifier the [[DefinitionVerifier DefinitionVerifier]] to allow or reject modifications of the
 *                           rule-set.
 */
class Definition
(
  val definitionVerifier: DefinitionVerifier
) extends Observable with Base {

  private val attributes: mutable.Set[AttributeRule] = mutable.Set()
  private val extensions: mutable.Set[ExtensionRule] = mutable.Set()
  private val associations: mutable.Set[AssociationRule] = mutable.Set()

  private val values: mutable.Set[ConcreteValue] = mutable.Set()

  /**
   * <p> Get all [[Rule Rules]] part of this Definition.
   *
   * @return Set[codi.Rule] - all Rules part of the Definition
   */
  def getRules: Set[Rule] = Set.from(attributes ++ extensions ++ associations ++ values)

  /**
   * <p> Generates the set of [[RuleData RuleData]] which contains all [[Rule Rules]] of
   * this Definition in their serialised form.
   * <p> The name and identity parameters are required because the Definition does not know by which [[ModelElement ModelElement]]
   * it is used. However the RuleData serialisation requires this information.
   *
   * @param name     name of the [[ModelElement ModelElement]] possessing this Definition
   * @param identity identity of the [[ModelElement ModelElement]] possessing this Definition
   * @return Set[RuleData] - all Rules part of the Definition in their serialised form
   */
  def toData(name: String, identity: String): Set[RuleData] = {
    getRules.map(rule => RuleData(rule.id, name, identity, rule.serialise(), rule.getDataType))
  }

  /**
   * <p> Fork this Definition according to the overall fork specification. This operation produces a deep copy
   * of the Definition and all its [[Rule Rules]].
   * <p> This operation takes the identity parameter because [[ExtensionRule ExtensionRules]] target a
   * specified identity which must be exchanged during the fork.
   * <p> This operation calls [[Rule#fork Rule.fork(identity)]] on each rule.
   *
   * @param identity the identifier of the forked [[ModelElement ModelElement]] following the default usage.
   * @return Definition - the forked deep-copy of this Definition
   */
  def fork(identity: String): Definition = {
    val newDefinition = new Definition(definitionVerifier)
    getRules.map(_.fork(identity)).foreach(newDefinition.applyRule)
    newDefinition
  }

  /**
   * <p>Add a [[Rule Rule]] to the rule-set represented by this Definition.
   * <p> This operation creates a temporary copy of the rule-set and the provided [[DefinitionVerifier DefinitionVerifier]]
   * decides if the new rule is allowed. If true, thr rule is added to the rule-set and all observers are notified.
   * <p> <strong>Note as of right now, only [[AttributeRule AttributeRules]], [[AssociationRule AssociationRules]]
   * and [[ExtensionRule ExtensionRules]] are supported.</strong>
   *
   * TODO provide feedback by returning a success criteria
   *
   * @param rule the new [[Rule Rule]] to add
   */
  private[modicio] def applyRule(rule: Rule): Unit = {
    val isValid = definitionVerifier.verify(getRules + rule)
    if (isValid) {
      rule match {
        case rule: AttributeRule => attributes.add(rule)
        case rule: ExtensionRule => extensions.add(rule)
        case rule: AssociationRule => associations.add(rule)
        case rule: ConcreteValue => values.add(rule)
      }
      notifyObservers()
    }
  }

  /**
   * <p> Remove a [[Rule Rule]] from the rule-set represented by this Definition.
   * <p> This operation creates a temporary copy of the rule-set and the provided [[DefinitionVerifier DefinitionVerifier]]
   * decides if the deletion is allowed. If true, thr rule is removed from the rule-set and all observers are notified.
   * <p> <strong>Note as of right now, only [[AttributeRule AttributeRules]], [[AssociationRule AssociationRules]]
   * and [[ExtensionRule ExtensionRules]] are supported.</strong>
   *
   * TODO provide feedback by returning a success criteria
   *
   * @param rule the [[Rule Rule]] to remove
   */
  private[modicio] def removeRule(rule: Rule): Unit = {
    val rules = getRules
    val newRuleset = rules.filter(_.id != rule.id)
    if (definitionVerifier.verify(newRuleset)) {
      rule match {
        case rule: AttributeRule => attributes.remove(rule)
        case rule: ExtensionRule => extensions.remove(rule)
        case rule: AssociationRule => associations.remove(rule)
        case rule: ConcreteValue => values.remove(rule)
      }
      notifyObservers()
    }
  }

  /**
   * <p> Remove a [[Rule Rule]] from the rule-set represented by this Definition.
   * <p> <strong>See [[Definition#removeRule Definition.removeRule()]] for more information!</strong>
   * <p> Note that in certain usecases, unique ids are not yet provided by the individual Rules and this operation
   * may lead to unexpected results. See [[Rule Rule (autoId)]] for more information.
   *
   * @param ruleID id of the [[Rule Rule]] to remove
   */
  private[modicio] def removeRuleByID(ruleID: String): Unit = {
    val rule = getRules.find(_.id == ruleID)
    if (rule.isDefined) removeRule(rule.get)
  }

  /**
   * <p> Get all [[AttributeRule AttributeRules]] part of this Definition.
   * <p> The provided result is deep-immutable and safe.
   *
   * @return Set[AttributeRule] - immutable set of all [[AttributeRule AttributeRules]]
   */
  override def getAttributeRules: Set[AttributeRule] = Set.from(attributes)

  /**
   * <p> Get all [[AssociationRule AssociationRules]] part of this Definition.
   * <p> The provided result is deep-immutable and safe.
   *
   * @return Set[AssociationRule] - immutable set of all [[AssociationRule AssociationRules]]
   */
  override def getAssociationRules: Set[AssociationRule] = Set.from(associations)

  /**
   * <p> Get all [[ExtensionRule ExtensionRules]] part of this Definition.
   * <p> The provided result is deep-immutable and safe.
   *
   * @return Set[ExtensionRule] - immutable set of all [[ExtensionRule ExtensionRules]]
   */
  override def getExtensionRules: Set[ExtensionRule] = Set.from(extensions)

  /**
   * <p> Get all [[ConcreteValue ConcreteValues]] part of this Definition.
   * <p> The provided result is deep-immutable and safe.
   *
   * @return Set[ConcreteValue] - immutable set of all [[ConcreteValue ConcreteValues]]
   */
  override def getConcreteValues: Set[ConcreteValue] = Set.from(values)

}