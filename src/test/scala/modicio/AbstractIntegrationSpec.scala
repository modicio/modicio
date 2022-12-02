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

package modicio

import modicio.core.rules.{AssociationRule, AttributeRule, ConnectionInterface, ParentRelationRule}
import modicio.core.{InstanceFactory, ModelElement, TimeIdentity, TypeFactory}
import modicio.nativelang.defaults.{SimpleDefinitionVerifier, SimpleMapRegistry, SimpleModelVerifier}
import modicio.nativelang.input.{NativeDSL, NativeDSLParser, NativeDSLTransformer}
import org.scalatest.FutureOutcome
import org.scalatest.flatspec.FixtureAsyncFlatSpec
import org.scalatest.matchers.should

import scala.concurrent.Future
import scala.io.Source

class AbstractIntegrationSpec extends FixtureAsyncFlatSpec with should.Matchers{
  type FixtureParam = Fixture

  override def withFixture(test: OneArgAsyncTest): FutureOutcome = {
    val theFixture = new Fixture()

    complete {
      super.withFixture(test.toNoArgAsyncTest(theFixture))
    } lastly {

    }
  }
}

class Fixture {
  val modelVerifier = new SimpleModelVerifier()
  val definitionVerifier = new SimpleDefinitionVerifier()

  val typeFactory: TypeFactory = new TypeFactory (definitionVerifier, modelVerifier)
  val instanceFactory: InstanceFactory = new InstanceFactory (definitionVerifier, modelVerifier)

  val registry: SimpleMapRegistry = new SimpleMapRegistry (typeFactory, instanceFactory)
  typeFactory.setRegistry (registry)
  instanceFactory.setRegistry (registry)

  val TODO: String = "Todo"
  val SPECIAL_TODO: String = "SpecialTodo"
  val PROJECT: String = "Project"
  val SPECIAL_PROJECT: String = "SpecialProject"
  val DEADLINE: String = "Deadline"
  val TITLE: String = "Title"

  val PROJECT_CONTAINS_TODO: String = "contains"
  val PROJECT_HAS_PART: String = "hasPart"
  val PROJECT_DUE_BY_DEADLINE: String = "dueBy"
  val IS_PART_OF: String = "partOf"

  val SINGLE: String = "1"
  val MULTIPLE: String = "*"
  val STRING: String = "String"
  val NONEMPTY: Boolean = true

  val TIME_IDENTITY: TimeIdentity = TimeIdentity.create

  def initProjectSetup (): Future[Any] = {
    implicit val ec: scala.concurrent.ExecutionContext = scala.concurrent.ExecutionContext.global

    for {
      root <- typeFactory.newType (ModelElement.ROOT_NAME, ModelElement.REFERENCE_IDENTITY, isTemplate = true, Some (TIME_IDENTITY) )
      project <- typeFactory.newType (PROJECT, ModelElement.REFERENCE_IDENTITY, isTemplate = false, Some (TIME_IDENTITY) )
      todo <- typeFactory.newType (TODO, ModelElement.REFERENCE_IDENTITY, isTemplate = false, Some (TIME_IDENTITY) )
      specialProject <- typeFactory.newType(DEADLINE, ModelElement.REFERENCE_IDENTITY, isTemplate = false, Some(TIME_IDENTITY))
      specialTodo <- typeFactory.newType(SPECIAL_TODO, ModelElement.REFERENCE_IDENTITY, isTemplate = false, Some(TIME_IDENTITY))
      _ <- registry.setType (root)
      _ <- registry.setType (project)
      _ <- registry.setType (todo)
      _ <- registry.setType(specialProject)
      _ <- registry.setType(specialTodo)
      _ <- Future({
        project.applyRule(AssociationRule.create(PROJECT_CONTAINS_TODO, TODO, MULTIPLE, ConnectionInterface.parseInterface(TIME_IDENTITY.variantTime.toString, TODO)))
        project.applyRule(AttributeRule.create(TITLE, STRING, NONEMPTY))
        specialProject.applyRule(ParentRelationRule.create(project.getTypeName, project.getTypeIdentity))

      })
      _ <- project.commit()
      _ <- specialProject.commit()
    } yield {}
  }

  def importProjectSetupFromFile(file: String): Future[Any] = {
    implicit val ec: scala.concurrent.ExecutionContext = scala.concurrent.ExecutionContext.global

    val source = Source.fromResource(file)
    val fileContents = source.getLines.mkString
    println(fileContents)
    source.close()
    val initialInput: NativeDSL = NativeDSLParser.parse(fileContents)
    val transformer = new NativeDSLTransformer(registry, definitionVerifier, modelVerifier)

    for {
      root <- typeFactory.newType(ModelElement.ROOT_NAME, ModelElement.REFERENCE_IDENTITY, isTemplate = true, Some(TIME_IDENTITY))
      _ <- registry.setType(root)
      _ <- transformer.extend(initialInput)
    } yield {

    }
  }
}