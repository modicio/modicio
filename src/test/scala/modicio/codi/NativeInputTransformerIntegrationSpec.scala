package modicio.codi

import modicio.AbstractIntegrationSpec
import modicio.core.ModelElement
import modicio.nativelang.input.{NativeDSL, NativeDSLParser, NativeDSLTransformer}

import scala.io.Source

class NativeInputTransformerIntegrationSpec extends AbstractIntegrationSpec {

  "The NativeInputTransformer" must "load the input model and extend the registry" in {

    val source = Source.fromResource("model_01.json")
    val fileContents = source.getLines.mkString
    println(fileContents)
    source.close()

    val initialInput: NativeDSL = NativeDSLParser.parse(fileContents)
    val transformer = new NativeDSLTransformer(registry, definitionVerifier, modelVerifier)

    for {
      root <- typeFactory.newType(ModelElement.ROOT_NAME, ModelElement.REFERENCE_IDENTITY, isTemplate = true, Some(TIME_IDENTITY))
      _ <- registry.setType(root)
      _ <- transformer.extend(initialInput)
      model <- registry.getReferences
    } yield {
      model.size should be (5)
    }
  }

}
