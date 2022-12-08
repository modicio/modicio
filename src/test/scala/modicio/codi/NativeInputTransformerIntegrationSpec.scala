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
