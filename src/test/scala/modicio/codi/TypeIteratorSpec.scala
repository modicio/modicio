/**
 * Copyright 2022 Karl Kegel
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
import modicio.core.TypeIterator

import scala.concurrent.Future


class TypeIteratorSpec extends AbstractIntegrationSpec {
  "TypeIterator.up" must "set parent as current element" in { fixture => {
    fixture.importProjectSetupFromFile("model_01.json") flatMap (_ =>
      for {
        todoInstance <- fixture.registry.getType("Todo", "#")
        modelElement <- Future.successful(todoInstance.get.getModelElement)
        _ <- modelElement.unfold()
      } yield {
        val typeIterator = new TypeIterator(modelElement)
        typeIterator.up
        typeIterator.up
        typeIterator.name should be("NamedElement")
      }
      )
    }
  }

  "TypeIterator.next" must "clear internal buffer if there is no next element" in { fixture => {
    fixture.importProjectSetupFromFile("model_01.json") flatMap (_ =>
      for {
        todoInstance <- fixture.registry.getType("Todo", "#")
        modelElement <- Future.successful(todoInstance.get.getModelElement)
        _ <- modelElement.unfold()
      } yield {
        val typeIterator = new TypeIterator(modelElement)
        typeIterator.next
        typeIterator.getName should be(None)
      }
      )
    }
  }
}
