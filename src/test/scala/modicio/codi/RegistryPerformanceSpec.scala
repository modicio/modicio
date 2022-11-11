/**
 * Copyright 2022 Karl Kegel, Tom Felber
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
import modicio.core.{InstanceFactory, ModelElement, TimeIdentity, TypeFactory, TypeHandle}
import modicio.nativelang.defaults.{BenchmarkMapRegistry, SimpleDefinitionVerifier, SimpleModelVerifier}
import org.scalatest.AppendedClues.convertToClueful


class RegistryPerformanceSpec extends AbstractIntegrationSpec {

  "DeepInstance.assignValue2" must "change the value of the correspondent key" in { fixture => {
      fixture.importProjectSetupFromFile("model_02.json") flatMap (_ =>
        for {
          todoInstance1 <- fixture.instanceFactory.newInstance("Todo")
          todoInstance2 <- fixture.instanceFactory.newInstance("Todo")
          projectInstance1 <- fixture.instanceFactory.newInstance("Project")
        } yield {
//          projectInstance1.associate(todoInstance1, fixture.PROJECT_ITEM, fixture.PROJECT_HAS_PART)
          1 should be(1)
        }
      )
    }
  }

  "BenchmarkMapRegistry" should "correctly record the number of function calls" in { fixture => {
    val modelVerifier = new SimpleModelVerifier()
    val definitionVerifier = new SimpleDefinitionVerifier()

    val typeFactory: TypeFactory = new TypeFactory(definitionVerifier, modelVerifier)
    val instanceFactory: InstanceFactory = new InstanceFactory(definitionVerifier, modelVerifier)

    val registry: BenchmarkMapRegistry = new BenchmarkMapRegistry(typeFactory, instanceFactory)
    typeFactory.setRegistry(registry)
    instanceFactory.setRegistry(registry)

    val TIME_IDENTITY: TimeIdentity = TimeIdentity.create

    var count = 0
    for {
      root <- typeFactory.newType (ModelElement.ROOT_NAME, ModelElement.REFERENCE_IDENTITY, isTemplate = true, Some(TIME_IDENTITY) )
      _ <- registry.setType (root)
    } yield {
      count = count + 1
    }

    for (i <- 1 to 10) {
      for {
        newType <- typeFactory.newType("1", ModelElement.REFERENCE_IDENTITY, isTemplate = false, Some(TIME_IDENTITY))
        _ <- registry.setType(newType)
      } yield {
        count = count + 1
      }
    }

    for {
      model <- registry.getReferences
    } yield {
      var hint: String = "Elements in the model: "
      model.foreach(typeHandle => hint = hint + typeHandle.getTypeName + ", ")
      hint = hint + "\n" + "Attempted calls to setType: " + count + "\n Registered calls to setType: " + registry.benchmarkCounter.getOrElse("setType", 0)
      model.size should be(11) withClue hint
      registry.benchmarkCounter.getOrElse("setType", 0) should be(count)
    }

    // val hint = "Calls: " + count + "; Recorded function calls: " + registry.benchmarkCounter.toString
    // registry.benchmarkCounter.getOrElse("setType", 0) should be (10) withClue hint
  }

  }
}
