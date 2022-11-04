package modicio.codi

import modicio.AbstractIntegrationSpec
import modicio.core.ModelElement
import modicio.nativelang.input.{NativeDSL, NativeDSLParser, NativeDSLTransformer}
import org.scalatest.AppendedClues.convertToClueful

import scala.io.Source

class NativeInputTransformerIntegrationSpec extends AbstractIntegrationSpec {

  "The NativeInputTransformer" must "load the input model and extend the registry" in {  fixture => {

    val source = Source.fromResource("model_01.json")
    val fileContents = source.getLines.mkString
    println(fileContents)
    source.close()

    val initialInput: NativeDSL = NativeDSLParser.parse(fileContents)
    val transformer = new NativeDSLTransformer(fixture.registry, fixture.definitionVerifier, fixture.modelVerifier)

    for {
      root <- fixture.typeFactory.newType(ModelElement.ROOT_NAME, ModelElement.REFERENCE_IDENTITY, isTemplate = true, Some(fixture.TIME_IDENTITY))
      _ <- fixture.registry.setType(root)
      _ <- transformer.extend(initialInput)
      model <- fixture.registry.getReferences
    } yield {
      var names: String = "Elements in the model: "
      model.foreach(typeHandle => names = names + typeHandle.getTypeName + ", ")
      model.size should be (5) withClue names
    }
  }}

}
