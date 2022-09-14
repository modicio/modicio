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
package modicio.nativelang.input

import modicio.codi.datamappings.{AssociationData, AttributeData, ExtensionData, InstanceData}
import modicio.codi.rules.{AssociationRule, AttributeRule, ExtensionRule}
import modicio.codi.{Registry, Shape, Transformer}
import modicio.codi.values.ConcreteValue
import modicio.verification.{DefinitionVerifier, ModelVerifier}

import scala.collection.mutable
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

/**
 * @param registry
 * @param definitionVerifier
 * @param modelVerifier
 */
class NativeDSLTransformer(registry: Registry,
                           definitionVerifier: DefinitionVerifier,
                           modelVerifier: ModelVerifier) extends
  Transformer[NativeDSL, ExtendedNativeDSL](registry, definitionVerifier, modelVerifier) {

  override def extend(input: NativeDSL): Future[Unit] = {
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

  override def decompose(input: Option[String]): Future[ExtendedNativeDSL] = {
    if(input.isDefined){
      val statements = mutable.Set[Statement]()
      val configuration = mutable.Set[(InstanceData, Set[ExtensionData], Set[AttributeData], Set[AssociationData])]()

      registry.get(input.get) flatMap (flatInstance => {
        if(flatInstance.isDefined){
          flatInstance.get.unfold() map (deepInstance => {
            deepInstance.getExtensionClosure.foreach(i => {
              val data = i.toData
              configuration.add(data)
              val frag = i.getTypeHandle.getFragment
              val childOf = frag.definition.getExtensionRules.map(_.serialise()).toSeq
              val associations = frag.definition.getAssociationRules.map(_.serialise()).toSeq
              val attributes = frag.definition.getAttributeRules.map(_.serialise()).toSeq
              val values = frag.definition.getConcreteValues.map(_.serialise()).toSeq
              val s = Statement(frag.name+":"+frag.identity, frag.isTemplate, childOf, associations, attributes, values)
              statements.add(s)
            })
            ExtendedNativeDSL(NativeDSL(statements.toSeq), configuration.toSeq)
          })
        }else{
          Future.failed(new IllegalArgumentException("Invalid instanceId in decompose()"))
        }
      })

    }else{
      Future.failed(new IllegalArgumentException("decompose() currently supports only DeepInstances"))
    }
  }

}
