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

import modicio.core.rules.{AssociationRule, AttributeRule, ParentRelationRule}
import modicio.core.values.ConcreteValue

/**
 * <p> The Base trait defines any class producing or forwarding a [[Definition Definition]] compatible specification
 * of [[Rule Rules]].
 * <p> [[values.ConcreteValue ConcreteValues]] are handled the same way as Rules internally.
 *
 * <p> As of right now, [[rules.ConstraintRule]] and [[rules.BehaviourRule]] are not supported, i.e.
 * throw an [[UnsupportedOperationException]].
 */
trait Base {

  /**
   * <p>Get all [[AttributeRule AttributeRules]] of the target.
   *
   * @return Set[AttributeRule]
   */
  def getAttributeRules: Set[AttributeRule]

  /**
   * <p>Get all [[AssociationRule AssociationRules]] of the target.
   *
   * @return Set[AssociationRule]
   */
  def getAssociationRules: Set[AssociationRule]

  /**
   * <p>Get all [[ParentRelationRule ParentRelationRules]] of the target.
   *
   * @return Set[ParentRelationRule]
   */
  def getParentRelationRules: Set[ParentRelationRule]

  /**
   * <p> Get all [[ConcreteValue ConcreteValues]] of the target
   *
   * @return Set[ConcreteValue]
   */
  def getConcreteValues: Set[ConcreteValue]

}
