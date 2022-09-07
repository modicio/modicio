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
package modicio.native.input

import modicio.codi.rules.{AssociationRule, AttributeRule, ExtensionRule}
import modicio.codi.{Registry, Transformer}
import modicio.codi.values.ConcreteValue
import modicio.verification.{DefinitionVerifier, ModelVerifier}

import scala.concurrent.Future

/**
 * @param registry
 * @param definitionVerifier
 * @param modelVerifier
 */
class NativeInputTransformer(registry: Registry,
                             definitionVerifier: DefinitionVerifier,
                             modelVerifier: ModelVerifier) extends
  Transformer[NativeInput](registry, definitionVerifier, modelVerifier) {

  override def extendModel(input: NativeInput): Future[Unit] = {
    input.model.foreach(statement => evaluateStatement(statement))
    Future.successful()
  }

  def evaluateStatement(statement: Statement): Unit = {
    val name = Statement.parseName(statement)
    val identity = Statement.parseIdentity(statement)
    val typeHandle = typeFactory.newType(name, identity, statement.template)
    registry.setType(typeHandle)

    statement.childOf.foreach(extensionRule => typeHandle.applyRule(new ExtensionRule(extensionRule)))

    statement.attributes.foreach(propertyRule => typeHandle.applyRule(new AttributeRule(propertyRule)))

    statement.associations.foreach(associationRule => typeHandle.applyRule(new AssociationRule(associationRule)))

    statement.values.foreach(concreteValue => typeHandle.applyRule(new ConcreteValue(concreteValue)))
  }

}
