package modicio.codi

import modicio.AbstractIntegrationSpec
import modicio.core.ModelElement
import modicio.core.rules.{AssociationRule, AttributeRule, ParentRelationRule}
import org.scalatest.AppendedClues.convertToClueful

class ModelModificationSpec extends AbstractIntegrationSpec {

  protected val DEADLINE: String = "Deadline"

  protected val PROJECT_DUE_BY_DEADLINE: String = "dueBy"
  protected val MULTIPLICITY: String = "1"

  protected val SPECIAL_PROJECT: String = "SpecialProject"

  protected val TITLE: String = "Title"
  protected val STRING: String = "String"
  protected val NONEMPTY: String = "true"

  //TODO: Test for adding a Type

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

  //TODO: Test for removing a Type

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

  //TODO: Test for adding an AssociationRule

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

  //TODO: Test for removing an AssociationRule

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

  //TODO: Test for adding a ParentRelationRule

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

  //TODO: Test for removing a ParentRelationRule

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

  //TODO: Test for adding an AttributeRule

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

  //TODO: Test for removing an AttributeRule

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
}