package modicio.codi

import modicio.AbstractIntegrationSpec
import modicio.core.ModelElement
import modicio.nativelang.input.{NativeDSL, NativeDSLParser, NativeDSLTransformer}
import org.scalatest.AppendedClues.convertToClueful

import scala.io.Source

class NativeInputTransformerIntegrationSpec extends AbstractIntegrationSpec {

  "The NativeInputTransformer" must "load the input model and extend the registry" in {  fixture => {
      fixture.importProjectSetupFromFile("model_01.json") flatMap (_ =>
        for {
          model <- fixture.registry.getReferences
        } yield {
          var names: String = "Elements in the model: "
          model.foreach(typeHandle => names = names + typeHandle.getTypeName + ", ")
          model.size should be (5) withClue names
        }
      )
    }
  }
  it must "not change the version time of types inside the model" in { fixture => {
    fixture.importProjectSetupFromFile("model_types_02.json") flatMap (_ =>
      for {
        todoType <- fixture.registry.getType("Todo", "#")
      } yield {
        todoType.get.getTimeIdentity.versionTime should be(1669650193366L)
      }
      )
  }
  }
  it must "not change the variant time of types inside the model" in { fixture => {
    fixture.importProjectSetupFromFile("model_types_02.json") flatMap (_ =>
      for {
        todoType <- fixture.registry.getType("Todo", "#")
      } yield {
        todoType.get.getTimeIdentity.variantTime should be(1669650193368L)
      }
      )
  }
  }
}
