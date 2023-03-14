package modicio.codi.integrations

import modicio.{AsyncSpec, RegistryFixture, Spec}
import org.scalatest.AppendedClues.convertToClueful

trait NativeInputTransformerBehaviors { this: AsyncSpec =>

  def nativeInputTransformer(newFixture: => RegistryFixture) {
    it must "load the input model and extend the registry" in {
      val fixture = newFixture
      fixture.importProjectSetupFromFile("model_01.json") flatMap (_ =>
        for {
          model <- fixture.registry.getReferences
        } yield {
          var names: String = "Elements in the model: "
          model.foreach(typeHandle => names = names + typeHandle.getTypeName + ", ")
          model.size should be(5) withClue names
        }
        )
    }

    it must "not change the version time of types inside the model" in {
      val fixture = newFixture
      fixture.importProjectSetupFromFile("model_types_01.json") flatMap (_ =>
        for {
          todoType <- fixture.registry.getType("Todo", "#")
        } yield {
          todoType.get.getTimeIdentity.versionTime should be(1669650193366L)
        }
        )
    }

    it must "not change the variant time of types inside the model" in {
      val fixture = newFixture
      fixture.importProjectSetupFromFile("model_types_01.json") flatMap (_ =>
        for {
          todoType <- fixture.registry.getType("Todo", "#")
        } yield {
          todoType.get.getTimeIdentity.variantTime should be(1669650193368L)
        }
        )
    }
  }
}
