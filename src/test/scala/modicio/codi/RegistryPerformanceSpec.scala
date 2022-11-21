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
import modicio.core.{InstanceFactory, ModelElement, TimeIdentity, TypeFactory}
import modicio.nativelang.defaults.{SimpleDefinitionVerifier, SimpleModelVerifier}
import org.scalatest.AppendedClues.convertToClueful

import scala.concurrent.Future


class RegistryPerformanceSpec extends AbstractIntegrationSpec {

  "Number of database operations" must "be lower than x" in { fixture => {
      fixture.importProjectSetupFromFile("model_02.json") flatMap (_ =>
        for {
          todoInstance1 <- fixture.instanceFactory.newInstance("Todo")
          todoInstance2 <- fixture.instanceFactory.newInstance("Todo")
          projectInstance1 <- fixture.instanceFactory.newInstance("Project")
        } yield {
          projectInstance1.associate(todoInstance1, fixture.TODO, fixture.PROJECT_HAS_PART)
          projectInstance1.associate(todoInstance2, fixture.TODO, fixture.PROJECT_HAS_PART)
          todoInstance1.associate(projectInstance1, fixture.PROJECT, fixture.IS_PART_OF)
          todoInstance2.associate(projectInstance1, fixture.PROJECT, fixture.IS_PART_OF)

          todoInstance1.assignValue("Content", "abc")
          todoInstance1.unfold()
          todoInstance1.assignDeepValue("Title", "abc")

          todoInstance2.assignValue("Content", "Todo1")
          todoInstance2.unfold()
          todoInstance2.assignDeepValue("Title", "Todo2")
          // TODO: Count number of operations
          1 should be(1)
        }
      )
    }
  }

/*  "BenchmarkMapRegistry" should "correctly record the number of function calls" in { fixture => {
      val modelVerifier = new SimpleModelVerifier()
      val definitionVerifier = new SimpleDefinitionVerifier()

      val typeFactory: TypeFactory = new TypeFactory(definitionVerifier, modelVerifier)
      val instanceFactory: InstanceFactory = new InstanceFactory(definitionVerifier, modelVerifier)

      val registry: BenchmarkMapRegistry = new BenchmarkMapRegistry(typeFactory, instanceFactory)
      typeFactory.setRegistry(registry)
      instanceFactory.setRegistry(registry)

      val TIME_IDENTITY: TimeIdentity = TimeIdentity.create

      val count = 11

      def addType(number: Number): Future[Any] = {
        for {
          newType <- typeFactory.newType(number.toString, ModelElement.REFERENCE_IDENTITY, isTemplate = false, Some(TIME_IDENTITY))
          _ <- registry.setType(newType)
        } yield {
        }
      }

      for {
        root <- typeFactory.newType (ModelElement.ROOT_NAME, ModelElement.REFERENCE_IDENTITY, isTemplate = true, Some(TIME_IDENTITY) )
        _ <- registry.setType (root)
        _ <- Future.sequence(Range(0,count-1).map((number) => addType(number)))
        model <- registry.getReferences
      } yield {
        var hint: String = "Elements in the model: "
        model.foreach(typeHandle => hint = hint + typeHandle.getTypeName + ", ")
        hint = hint + "\n" + "Attempted calls to setType: " + count + "\n Registered calls to setType: " + registry.benchmarkCounter.getOrElse("setType", 0)
        model.size should be(11) withClue hint
        registry.benchmarkCounter.getOrElse("setType", 0) should be(count)
      }
    }
  }*/
}
