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

import modicio.core.rules.{AssociationRule, AttributeRule, ParentRelationRule}
import modicio.core.values.ConcreteValue
import modicio.core.{ImmutableShape, ModelElement, Registry, TimeIdentity, Transformer, TypeHandle}
import modicio.verification.{DefinitionVerifier, ModelVerifier}

import scala.collection.mutable
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
 * @param registry
 * @param definitionVerifier
 * @param modelVerifier
 */
class NativeDSLTransformer(registry: Registry,
                           definitionVerifier: DefinitionVerifier,
                           modelVerifier: ModelVerifier) extends
  Transformer[NativeDSL, NativeCompartment](registry, definitionVerifier, modelVerifier) {

  override def extend(input: NativeDSL): Future[Any] = {
    val defaultTimeIdentity = TimeIdentity.create
    Future.sequence(input.model.map(statement => evaluateModelElement(statement, defaultTimeIdentity)))
  }

  def evaluateModelElement(statement: NativeModelElement, defaultTimeIdentity: TimeIdentity): Future[TypeHandle] = {
    val name = NativeModelElement.parseName(statement)
    val identity = NativeModelElement.parseIdentity(statement)

    println("EVALUATE MODEL ELEMENT")
    println(statement)
    println(defaultTimeIdentity)

    val timeIdentity = {
      if (statement.timeDescriptor.isDefined) {
        val desc = statement.timeDescriptor.get
        TimeIdentity(desc.variantTime, desc.runningTime, desc.versionTime, desc.variantId, desc.runningId, desc.versionId)
      } else {
        defaultTimeIdentity
      }
    }

    for {
      typeHandle <- typeFactory.newType(name, identity, statement.template, Some(timeIdentity))
      _ <- registry.setType(typeHandle)
    } yield {
      statement.childOf.foreach(parentRelationRule => typeHandle.applyRule(new ParentRelationRule(parentRelationRule)))
      statement.attributes.foreach(propertyRule => typeHandle.applyRule(new AttributeRule(propertyRule)))
      statement.associations.foreach(associationRule => typeHandle.applyRule(new AssociationRule(associationRule)))
      statement.values.foreach(concreteValue => typeHandle.applyRule(new ConcreteValue(concreteValue)))
      typeHandle
    }

  }

  /**
   * Decomposes a runtime model into its serialised NativeModel
   * @param deepInstanceId
   * @return
   */
  override def decomposeInstance(deepInstanceId: String): Future[NativeCompartment] = {
      val statements = mutable.Set[NativeModelElement]()
      val configuration = mutable.Set[ImmutableShape]()
      registry.get(deepInstanceId) flatMap (flatInstance => {
        if (flatInstance.isDefined) {
          flatInstance.get.unfold() map (deepInstance => {
            deepInstance.getParentRelationClosure.foreach(i => {
              val data = i.toData
              configuration.add(data)
              val frag = i.getTypeHandle.getModelElement
              statements.add(buildStatement(frag))
            })
            NativeCompartment(NativeDSL(statements.toSeq), configuration.toSeq.map(s =>
              (s.instanceData, s.parentRelations, s.attributes, s.associations)))
          })
        } else {
          Future.failed(new IllegalArgumentException("Invalid instanceId in decompose()"))
        }
      })
  }

  override def decomposeModel(): Future[NativeDSL] = {
    registry.getReferences map( references => {
      val statements = references.map(_.getModelElement).map(buildStatement)
      NativeDSL(statements.toSeq)
    })
  }

  private def buildStatement(frag: ModelElement): NativeModelElement = {
    val childOf = frag.definition.getParentRelationRules.map(_.serialise()).toSeq
    val associations = frag.definition.getAssociationRules.map(_.serialise()).toSeq
    val attributes = frag.definition.getAttributeRules.map(_.serialise()).toSeq
    val values = frag.definition.getConcreteValues.map(_.serialise()).toSeq
    val ti = frag.getTimeIdentity
    val timeIdentity = NativeTimeIdentity(ti.variantTime, ti.runningTime, ti.versionTime, ti.variantId, ti.runningId, ti.versionId)
    NativeModelElement(frag.name + ":" + frag.identity, frag.isTemplate, Some(timeIdentity), childOf, associations, attributes, values)
  }
}
