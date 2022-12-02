/**
 * Copyright 2022 Karl Kegel, Johannes Gröschel
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

import modicio.AbstractIntegrationSpec
import modicio.core.ModelElement
import modicio.core.rules.{AssociationRule, AttributeRule, ConnectionInterface, ParentRelationRule}
import org.scalatest.AppendedClues.convertToClueful

import scala.concurrent.Future

class ModelModificationSpec extends AbstractIntegrationSpec {

  "A new type" should "be correctly added to the model" in { fixture => {
      fixture.initProjectSetup() flatMap (_ =>
        for {
          newType <- fixture.typeFactory.newType("IntegrationTest", ModelElement.REFERENCE_IDENTITY, isTemplate = false, Some(fixture.TIME_IDENTITY))
          _ <- fixture.registry.setType(newType)
          model <- fixture.registry.getReferences
        } yield {
          var names: String = "Elements in the model: "
          model.foreach(typeHandle => names = names + typeHandle.getTypeName + ", ")
          model.size should be(5) withClue names
        }
      )
    }
  }

  "A type" should "be correctly removed from the model" in { fixture => {
      fixture.initProjectSetup() flatMap (_ =>
        for {
          _ <- fixture.registry.autoRemove(fixture.TODO, ModelElement.REFERENCE_IDENTITY)
          model <- fixture.registry.getReferences
        } yield {
          var names: String = "Elements in the model: "
          model.foreach(typeHandle => names = names + typeHandle.getTypeName + ", ")
          model.size should be(3) withClue names
        }
      )
    }
  }

  "An AssociationRule" should "be correctly added to a model" in { fixture => {
      val newRule = AssociationRule.create("dueBy", fixture.DEADLINE, fixture.SINGLE, ConnectionInterface.parseInterface(fixture.TIME_IDENTITY.variantTime.toString, fixture.DEADLINE))
      fixture.initProjectSetup() flatMap (_ =>
        for {
          typeOption <- fixture.registry.getType(fixture.TODO, ModelElement.REFERENCE_IDENTITY)
          todo <- typeOption.get.unfold()
          _ <- Future(todo.applyRule(newRule))
          _ <- todo.commit()
        } yield {
          var rules: String = "AssociationRules for Todo: "
          todo.getModelElement.definition.getAssociationRules.foreach(rule => rules = rules + rule.associationName + ", ")
          todo.getModelElement.definition.getAssociationRules.size should be (1) withClue rules
        }
      )
    }
  }

  "An AssociationRule" should "be correctly removed from a model" in { fixture => {
      fixture.initProjectSetup() flatMap (_ =>
        for {
          typeOption <- fixture.registry.getType(fixture.PROJECT, ModelElement.REFERENCE_IDENTITY)
          project <- typeOption.get.unfold()
          _ <- Future(project.removeRule(project.getModelElement.definition.getAssociationRules.find(rule => rule.associationName == "contains").orNull))
          _ <- project.commit()
        } yield {
          var rules: String = "AssociationRules for Project: "
          project.getModelElement.definition.getAssociationRules.foreach(rule => rules = rules + rule.associationName + ", ")
          project.getModelElement.definition.getAssociationRules.size should be(0) withClue rules
        }
      )
    }
  }

  "A ParentRelationRule" should "be correctly added to a model" in { fixture => {
      fixture.initProjectSetup() flatMap (_ =>
        for {
          typeOption <- fixture.registry.getType(fixture.SPECIAL_TODO, ModelElement.REFERENCE_IDENTITY)
          specialTodo <- typeOption.get.unfold()
          typeOption <- fixture.registry.getType(fixture.TODO, ModelElement.REFERENCE_IDENTITY)
          todo <- typeOption.get.unfold()
          _ <- Future(specialTodo.applyRule(ParentRelationRule.create(todo.getTypeName, todo.getTypeIdentity)))
          _ <- specialTodo.commit()
          typeOption <- fixture.registry.getType(fixture.SPECIAL_TODO, ModelElement.REFERENCE_IDENTITY)
          specialProject <- typeOption.get.unfold()
        } yield {
          var rules: String = "ParentRelationRules for SpecialTodo: "
          specialProject.getModelElement.definition.getParentRelationRules.foreach(rule => rules = rules + rule.parentName + ", ")
          specialProject.getModelElement.definition.getParentRelationRules.size should be (1) withClue rules
        }
      )
    }
  }

  "A ParentRelationRule" should "be correctly removed from a model" in { fixture => {
    fixture.initProjectSetup() flatMap (_ =>
        for {
          typeOption <- fixture.registry.getType(fixture.SPECIAL_PROJECT, ModelElement.REFERENCE_IDENTITY)
          specialProject <- typeOption.get.unfold()
          _ <- Future(specialProject.removeRule(specialProject.getModelElement.definition.getParentRelationRules.find(rule => rule.parentName == fixture.PROJECT).orNull))
          _ <- specialProject.commit()
        } yield {
          var rules = "ParentRelationRules for SpecialProject: "
          specialProject.getModelElement.definition.getParentRelationRules.foreach(rule => rules = rules + rule.parentName + ", ")
          specialProject.getModelElement.definition.getParentRelationRules.size should be(0) withClue rules
        }
      )
    }
  }

  "An AttributeRule" should "be correctly added to a model" in { fixture => {
      val newRule = AttributeRule.create("IntegrationTest", fixture.STRING, fixture.NONEMPTY)
      fixture.initProjectSetup() flatMap (_ =>
        for {
          typeOption <- fixture.registry.getType(fixture.PROJECT, ModelElement.REFERENCE_IDENTITY)
          project <- typeOption.get.unfold()
          _ <- Future(project.applyRule(newRule))
          _ <- project.commit()
        } yield {
          var rules: String = "AttributeRules for Project: "
          project.getModelElement.definition.getAttributeRules.foreach(rule => rules = rules + rule.name + ", ")
          project.getModelElement.definition.getAttributeRules.size should be(2) withClue rules
        }
      )
    }
  }

  "An AttributeRule" should "be correctly removed from a model" in { fixture => {
      fixture.initProjectSetup() flatMap (_ =>
        for {
          typeOption <- fixture.registry.getType(fixture.PROJECT, ModelElement.REFERENCE_IDENTITY)
          project <- typeOption.get.unfold()
          _ <- Future(project.removeRule(project.getModelElement.definition.getAttributeRules.find(rule => rule.name == fixture.TITLE).orNull))
          _ <- project.commit()
        } yield {
          var rules = "AttributeRules for Project: "
          project.getModelElement.definition.getAttributeRules.foreach(rule => rules = rules + rule.name + ", ")
          project.getModelElement.definition.getAttributeRules.size should be(0) withClue rules
        }
      )
    }
  }

  "Adding a new Type" should "change running id of the model" in { fixture => {
    fixture.initProjectSetup() flatMap (_ =>
        for {
          pre_time <- fixture.registry.getReferenceTimeIdentity
          newType <- fixture.typeFactory.newType("IntegrationTest", ModelElement.REFERENCE_IDENTITY, isTemplate = false, Some(fixture.TIME_IDENTITY))
          _ <- fixture.registry.setType(newType)
          post_time <- fixture.registry.getReferenceTimeIdentity
          model <- fixture.registry.getReferences

        } yield {
          val times: String = "Before: " + pre_time.toString + "; After: " + post_time.toString
          var names: String = "Elements in the model: "
          model.foreach(typeHandle => names = names + typeHandle.getTypeName + ", ")
          val hint: String = times + "\n" + names
          pre_time.runningId should not be post_time.runningId withClue hint
        }
      )
    }
  }

  "Removing a Type" should "change the id of the model" in { fixture => {
    fixture.initProjectSetup() flatMap (_ =>
        for {
          pre_time <- fixture.registry.getReferenceTimeIdentity
          _ <- fixture.registry.autoRemove(fixture.TODO, ModelElement.REFERENCE_IDENTITY)
          post_time <- fixture.registry.getReferenceTimeIdentity
          model <- fixture.registry.getReferences
        } yield {
          val times: String = "Before: " + pre_time.toString + "; After: " + post_time.toString
          var names: String = "Elements in the model: "
          model.foreach(typeHandle => names = names + typeHandle.getTypeName + ", ")
          val hint: String = times + "\n" + names
          pre_time.runningId should not be post_time.runningId withClue hint
        }
      )
    }
  }
}