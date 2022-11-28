/**
 * Copyright 2022 Karl Kegel
 * Johannes Gröschel
 * Tom Felber
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
import modicio.core.rules.{AssociationRule, AttributeRule, ParentRelationRule}
import org.scalatest.AppendedClues.convertToClueful

import scala.concurrent.Future

class ModelModificationSpec extends AbstractIntegrationSpec {

  protected val DEADLINE: String = "Deadline"

  protected val PROJECT_DUE_BY_DEADLINE: String = "dueBy"
  protected val MULTIPLICITY: String = "1"

  protected val SPECIAL_PROJECT: String = "SpecialProject"

  protected val TITLE: String = "Title"
  protected val STRING: String = "String"
  protected val NONEMPTY: String = "true"

  "A new type" should "be correctly added to the model" in { fixture => {
      fixture.initProjectSetup() flatMap (_ =>
        for {
          deadline <- fixture.typeFactory.newType(DEADLINE, ModelElement.REFERENCE_IDENTITY, isTemplate = false, Some(fixture.TIME_IDENTITY))
          _ <- fixture.registry.setType(deadline)
          model <- fixture.registry.getReferences

        } yield {
          var names: String = "Elements in the model: "
          model.foreach(typeHandle => names = names + typeHandle.getTypeName + ", ")
          model.size should be(4) withClue names
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
          model.size should be(2) withClue names
        }
      )
    }
  }

  "An AssociationRule" should "be correctly added to a model" in { fixture => {
      val newRule = new AssociationRule(":"+PROJECT_DUE_BY_DEADLINE+":"+DEADLINE+":1:"+fixture.TIME_IDENTITY.variantTime.toString)
      fixture.initProjectSetup() flatMap (_ =>
        for {
          deadline <- fixture.typeFactory.newType(DEADLINE, ModelElement.REFERENCE_IDENTITY, isTemplate = false, Some(fixture.TIME_IDENTITY))
          _ <- fixture.registry.setType(deadline)
          typeOption <- fixture.registry.getType(fixture.PROJECT, ModelElement.REFERENCE_IDENTITY)
          project <- typeOption.get.unfold()
        } yield {
          // typeHandle.getAssociated.find(handle => handle.getTypeName == DEADLINE) should be(None)
          project.applyRule(newRule)
          project.commit() //optional in SimpleRegistry mode
          var rules: String = "AssociationRules for Project: "
          project.getModelElement.definition.getAssociationRules.foreach(rule => rules = rules + rule.associationName + ", ")
          project.getModelElement.definition.getAssociationRules.size should be (2) withClue rules
        }
      )
    }
  }

  "An AssociationRule" should "be correctly removed from a model" in { fixture => {
      fixture.initProjectSetup() flatMap (_ =>
        for {
          typeOption <- fixture.registry.getType(fixture.PROJECT, ModelElement.REFERENCE_IDENTITY)
          project <- typeOption.get.unfold()
        } yield {
          val rule: AssociationRule = project.getModelElement.definition.getAssociationRules.find(rule => rule.associationName == "contains").orNull
          project.removeRule(rule)
          project.commit()
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
          typeOption <- fixture.registry.getType(fixture.PROJECT, ModelElement.REFERENCE_IDENTITY)
          project <- typeOption.get.unfold()
          specialProject <- fixture.typeFactory.newType(SPECIAL_PROJECT, ModelElement.REFERENCE_IDENTITY, isTemplate = false, Some(fixture.TIME_IDENTITY))
        } yield {
          val newRule = new ParentRelationRule(":"+project.getTypeIdentity+":"+project.getTypeName)
          specialProject.applyRule(newRule)
          specialProject.commit()
          var rules: String = "ParentRelationRules for SpecialProject: "
          specialProject.getModelElement.definition.getParentRelationRules.foreach(rule => rules = rules + rule.parentName + ", ")
          specialProject.getModelElement.definition.getParentRelationRules.size should be (1) withClue rules
        }
      )
    }
  }

  "A ParentRelationRule" should "be correctly removed from a model" in { fixture => {
    fixture.initProjectSetup() flatMap (_ =>
        for {
          typeOption <- fixture.registry.getType(fixture.PROJECT, ModelElement.REFERENCE_IDENTITY)
          project <- typeOption.get.unfold()
          specialProject <- fixture.typeFactory.newType(SPECIAL_PROJECT, ModelElement.REFERENCE_IDENTITY, isTemplate = false, Some(fixture.TIME_IDENTITY))
        } yield {
          val newRule = new ParentRelationRule(":" + project.getTypeIdentity + ":" + project.getTypeName)
          specialProject.applyRule(newRule)
          specialProject.commit()
          var rules: String = "ParentRelationRules for SpecialProject: "
          specialProject.getModelElement.definition.getParentRelationRules.foreach(rule => rules = rules + rule.parentName + ", ")
          specialProject.getModelElement.definition.getParentRelationRules.size should be(1) withClue rules
          specialProject.removeRule(newRule)
          specialProject.commit()
          rules = "ParentRelationRules for SpecialProject: "
          specialProject.getModelElement.definition.getParentRelationRules.foreach(rule => rules = rules + rule.parentName + ", ")
          specialProject.getModelElement.definition.getParentRelationRules.size should be(0) withClue rules
        }
      )
    }
  }

  "An AttributeRule" should "be correctly added to a model" in { fixture => {
      val newRule = new AttributeRule(":" + TITLE + ":" + STRING + ":" + NONEMPTY)
      fixture.initProjectSetup() flatMap (_ =>
        for {
          typeOption <- fixture.registry.getType(fixture.PROJECT, ModelElement.REFERENCE_IDENTITY)
          project <- typeOption.get.unfold()
        } yield {
          project.applyRule(newRule)
          project.commit()
          var rules: String = "AttributeRules for Project: "
          project.getModelElement.definition.getAttributeRules.foreach(rule => rules = rules + rule.name + ", ")
          project.getModelElement.definition.getAttributeRules.size should be(1) withClue rules
        }
      )
    }
  }

  "An AttributeRule" should "be correctly removed from a model" in { fixture => {
      val newRule = new AttributeRule(":" + TITLE + ":" + STRING + ":" + NONEMPTY)
      fixture.initProjectSetup() flatMap (_ =>
        for {
          typeOption <- fixture.registry.getType(fixture.PROJECT, ModelElement.REFERENCE_IDENTITY)
          project <- typeOption.get.unfold()
        } yield {
          project.applyRule(newRule)
          project.commit()
          var rules: String = "AttributeRules for Project: "
          project.getModelElement.definition.getAttributeRules.foreach(rule => rules = rules + rule.name + ", ")
          project.getModelElement.definition.getAttributeRules.size should be(1) withClue rules
          project.removeRule(newRule)
          project.commit()
          rules = "AttributeRules for Project: "
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
          deadline <- fixture.typeFactory.newType(DEADLINE, ModelElement.REFERENCE_IDENTITY, isTemplate = false, Some(fixture.TIME_IDENTITY))
          _ <- fixture.registry.setType(deadline)
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

  "A ModelElement edit" should "raise its version time" in { fixture => {
    fixture.importProjectSetupFromFile("model_types_01.json") flatMap (_ =>
      for {
        todoType <- fixture.registry.getType("Todo", "#")
        oldTime <- Future.successful(todoType.get.getTimeIdentity.versionTime)
        _ <- Future.successful(todoType.get.getModelElement.definition.setVolatile())
        _ <- todoType.get.commit()
      } yield {
        oldTime should be < todoType.get.getTimeIdentity.versionTime
      }
      )
  }
  }

  "Incrementing Version of the child type" should "not change the version of the parent type" in { fixture => {
    fixture.importProjectSetupFromFile("model_types_01.json") flatMap (_ =>
      for {
        todoType <- fixture.registry.getType("Todo", "#")
        projectItemType <- fixture.registry.getType("ProjectItem", "#")
        oldTime <- Future.successful(projectItemType.get.getTimeIdentity.versionTime)
        _ <- Future.successful(todoType.get.getModelElement.definition.setVolatile())
        _ <- todoType.get.commit()
      } yield {
        oldTime should be (projectItemType.get.getTimeIdentity.versionTime)
      }
      )
  }
  }
}