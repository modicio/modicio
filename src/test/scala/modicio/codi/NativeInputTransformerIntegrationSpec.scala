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
}
