package modicio.codi

import modicio.AbstractIntegrationSpec
import modicio.core.ModelElement
import modicio.core.rules.AssociationRule
import org.scalatest.AppendedClues.convertToClueful

class ModelModificationSpec extends AbstractIntegrationSpec {

  protected val DEADLINE: String = "Deadline"

  protected val PROJECT_DUE_BY_DEADLINE: String = "dueBy"
  protected val MULTIPLICITY: String = "1"

  //TODO: Test for adding a Type

  "A new type" should "be correctly added to the model" in {
    initProjectSetup() flatMap (_ => {
      for {
        deadline <- typeFactory.newType(DEADLINE, ModelElement.REFERENCE_IDENTITY, isTemplate = false, Some(TIME_IDENTITY))
        _ <- registry.setType(deadline)
        model <- registry.getReferences

      } yield {
        var names: String = "Elements in the model: "
        model.foreach(typeHandle => names = names + typeHandle.getTypeName + ", ")
        model.size should be(4) withClue names
      }
    })
  }

  //TODO: Test for removing a Type

  "A type" should "be correctly removed from the model" in {
    initProjectSetup() flatMap (_ => {
      for {
        _ <- registry.autoRemove(TODO, ModelElement.REFERENCE_IDENTITY)
        model <- registry.getReferences
      } yield {
        var names: String = "Elements in the model: "
        model.foreach(typeHandle => names = names + typeHandle.getTypeName + ", ")
        model.size should be(3) withClue names
      }
    })
  }

  //TODO: Test for adding an AssociationRule

  "An AssociationRule" should "be correctly added to a model" in {
    initProjectSetup() flatMap(_ => {
      val newRule = new AssociationRule(":"+PROJECT_DUE_BY_DEADLINE+":"+DEADLINE+":1:"+TIME_IDENTITY.variantTime.toString)

      for {
        deadline <- typeFactory.newType(DEADLINE, ModelElement.REFERENCE_IDENTITY, isTemplate = false, Some(TIME_IDENTITY))
        _ <- registry.setType(deadline)
        typeOption <- registry.getType(PROJECT, ModelElement.REFERENCE_IDENTITY)
        project <- typeOption.get.unfold()
      } yield {
        // typeHandle.getAssociated.find(handle => handle.getTypeName == DEADLINE) should be(None)
        project.applyRule(newRule)
        project.commit() //optional in SimpleRegistry mode
        var rules: String = "AssociationRules for Project: "
        project.getModelElement.definition.getAssociationRules.foreach(rule => rules = rules + rule.associationName + ", ")
        project.getModelElement.definition.getAssociationRules.size should be (2) withClue rules
      }
    })
  }

  //TODO: Test for removing an AssociationRule

  "An AssociationRule" should "be correctly removed from a model" in {
    initProjectSetup() flatMap(_ => {
      for {
        typeOption <- registry.getType(PROJECT, ModelElement.REFERENCE_IDENTITY)
        project <- typeOption.get.unfold()
      } yield {
        val rule: AssociationRule = project.getModelElement.definition.getAssociationRules.find(rule => rule.associationName == "contains").orNull
        project.removeRule(rule)
        project.commit()
        var rules: String = "AssociationRules for Project: "
        project.getModelElement.definition.getAssociationRules.foreach(rule => rules = rules + rule.associationName + ", ")
        project.getModelElement.definition.getAssociationRules.size should be(0) withClue rules
      }
    })
  }

  //TODO: Test for adding a ParentRelationRule

  //TODO: Test for removing a ParentRelationRule

  //TODO: Test for adding an AttributeRule

  //TODO: Test for removing an AttributeRule

}