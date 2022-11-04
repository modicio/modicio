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
import modicio.core.ModelElement
import modicio.nativelang.input.{NativeDSL, NativeDSLParser, NativeDSLTransformer}

import scala.io.Source


class DeepInstanceIntegrationSpec extends AbstractIntegrationSpec {

  "DeepInstance.assignValue" must "change the value of the correspondent key" in {

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
      todoInstance <- instanceFactory.newInstance("Todo")
    } yield {
      todoInstance.assignValue("Content", "abc")
      todoInstance.value("Content").get should be("abc")
    }
  }

  "DeepInstance.assignDeepValue" must "change the value of the correspondent key of the parent" in {

    val source = Source.fromResource("model_deepInstance_01.json")
    val fileContents = source.getLines.mkString
    println(fileContents)
    source.close()

    val initialInput: NativeDSL = NativeDSLParser.parse(fileContents)
    val transformer = new NativeDSLTransformer(registry, definitionVerifier, modelVerifier)

    for {
      root <- typeFactory.newType(ModelElement.ROOT_NAME, ModelElement.REFERENCE_IDENTITY, isTemplate = true, Some(TIME_IDENTITY))
      _ <- registry.setType(root)
      _ <- transformer.extend(initialInput)
      todoInstance <- instanceFactory.newInstance("Todo")
      _ <- todoInstance.unfold()
    } yield {
      todoInstance.assignDeepValue("Name", "abc")
      todoInstance.deepValue("Name").get should be("abc")
    }
  }

  "A new DeepInstance" must "increment the running time of the model" in {

    val source = Source.fromResource("model_deepInstance_01.json")
    val fileContents = source.getLines.mkString
    println(fileContents)
    source.close()

    val initialInput: NativeDSL = NativeDSLParser.parse(fileContents)
    val transformer = new NativeDSLTransformer(registry, definitionVerifier, modelVerifier)

    for {
      root <- typeFactory.newType(ModelElement.ROOT_NAME, ModelElement.REFERENCE_IDENTITY, isTemplate = true, Some(TIME_IDENTITY))
      _ <- registry.setType(root)
      _ <- transformer.extend(initialInput)
      _ <- instanceFactory.newInstance("Todo")
      pre_time <- registry.getReferenceTimeIdentity
      _ <- instanceFactory.newInstance("Todo")
      post_time <- registry.getReferenceTimeIdentity
    } yield {
      pre_time.runningTime should be < post_time.runningTime
    }
  }

  "A new DeepInstance" must "not increment the running time of an older deepInstance" in {

    val source = Source.fromResource("model_deepInstance_01.json")
    val fileContents = source.getLines.mkString
    println(fileContents)
    source.close()

    val initialInput: NativeDSL = NativeDSLParser.parse(fileContents)
    val transformer = new NativeDSLTransformer(registry, definitionVerifier, modelVerifier)

    for {
      root <- typeFactory.newType(ModelElement.ROOT_NAME, ModelElement.REFERENCE_IDENTITY, isTemplate = true, Some(TIME_IDENTITY))
      _ <- registry.setType(root)
      _ <- transformer.extend(initialInput)
      todoInstance1 <- instanceFactory.newInstance("Todo")
      todoInstance2 <- instanceFactory.newInstance("Todo")
    } yield {
      todoInstance1.typeHandle.getTimeIdentity.runningTime should be < todoInstance2.typeHandle.getTimeIdentity.runningTime
    }
  }

  "A DeepInstance edit" must "not change timeIdentity" in {

    val source = Source.fromResource("model_deepInstance_01.json")
    val fileContents = source.getLines.mkString
    println(fileContents)
    source.close()

    val initialInput: NativeDSL = NativeDSLParser.parse(fileContents)
    val transformer = new NativeDSLTransformer(registry, definitionVerifier, modelVerifier)

    for {
      root <- typeFactory.newType(ModelElement.ROOT_NAME, ModelElement.REFERENCE_IDENTITY, isTemplate = true, Some(TIME_IDENTITY))
      _ <- registry.setType(root)
      _ <- transformer.extend(initialInput)
      todoInstance <- instanceFactory.newInstance("Todo")
      _ <- todoInstance.unfold()
    } yield {
      val pre = todoInstance.typeHandle.getTimeIdentity
      todoInstance.assignDeepValue("Name", "abc")
      val post = todoInstance.typeHandle.getTimeIdentity
      pre should be(post)
    }
  }

  "Incrementing variant" must "not change timeIdentity of existing DeepInstances" in {

    val source = Source.fromResource("model_deepInstance_01.json")
    val fileContents = source.getLines.mkString
    println(fileContents)
    source.close()

    val initialInput: NativeDSL = NativeDSLParser.parse(fileContents)
    val transformer = new NativeDSLTransformer(registry, definitionVerifier, modelVerifier)

    for {
      root <- typeFactory.newType(ModelElement.ROOT_NAME, ModelElement.REFERENCE_IDENTITY, isTemplate = true, Some(TIME_IDENTITY))
      _ <- registry.setType(root)
      _ <- transformer.extend(initialInput)
      todoInstance <- instanceFactory.newInstance("Todo")
      _ <- registry.incrementVariant
      post <- registry.getReferenceTimeIdentity
    } yield {
      todoInstance.typeHandle.getTimeIdentity.variantTime should be < post.variantTime
    }
  }
}