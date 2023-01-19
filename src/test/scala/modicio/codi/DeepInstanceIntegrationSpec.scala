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

import scala.concurrent.Future


class DeepInstanceIntegrationSpec extends AbstractIntegrationSpec {

  "DeepInstance.assignValue" must "change the value of the correspondent key" in { fixture => {
      fixture.importProjectSetupFromFile("model_01.json") flatMap (_ =>
        for {
          todoInstance <- fixture.instanceFactory.newInstance("Todo")
        } yield {
          todoInstance.assignValue("Content", "abc")
          todoInstance.value("Content").get should be("abc")
        }
      )
    }
  }

  "DeepInstance.assignDeepValue" must "change the value of the correspondent key" in { fixture => {
    fixture.importProjectSetupFromFile("model_deepInstance_01.json") flatMap (_ =>
      for {
        todoInstance <- fixture.instanceFactory.newInstance("Todo")
        _ <- todoInstance.unfold()
      } yield {
        todoInstance.assignDeepValue("Content", "abc")
        todoInstance.value("Content").get should be("abc")
      }
      )
  }}

  "DeepInstance.assignDeepValue" must "change the value of the correspondent key of the parent" in { fixture => {
      fixture.importProjectSetupFromFile("model_deepInstance_01.json") flatMap (_ =>
        for {
          todoInstance <- fixture.instanceFactory.newInstance("Todo")
          _ <- todoInstance.unfold()
        } yield {
          todoInstance.assignDeepValue("Name", "abc")
          todoInstance.deepValue("Name").get should be("abc")
        }
      )
    }
  }

  "DeepInstance.assignDeepValue" must "change if applied sequentially with commit" in { fixture => {
    fixture.importProjectSetupFromFile("model_deepInstance_01.json") flatMap (_ =>
      for {
        todoInstance <- fixture.instanceFactory.newInstance("Todo")
        _ <- todoInstance.unfold()
        _ <- Future(todoInstance.assignDeepValue("Content", "abc"))
        _ <- Future(todoInstance.assignDeepValue("Name", "abc"))
        _ <- todoInstance.commit
        td2 <- fixture.registry.get(todoInstance.instanceId)
        _ <- td2.get.unfold()
      } yield {
        val m = td2.get.getDeepAttributes
        td2.get.deepValue("Content").get should be("abc")
        td2.get.deepValue("Name").get should be("abc")
      })
  }
  }

  "A new DeepInstance" must "not increment the running time of the model" in { fixture => {
      fixture.importProjectSetupFromFile("model_deepInstance_01.json") flatMap (_ =>
        for {
          _ <- fixture.instanceFactory.newInstance("Todo")
          pre_time <- fixture.registry.getReferenceTimeIdentity
          _ <- fixture.instanceFactory.newInstance("Todo")
          post_time <- fixture.registry.getReferenceTimeIdentity
        } yield {
          pre_time.runningTime should be(post_time.runningTime)
        }
      )
    }
  }

  "A new DeepInstance" must "not increment the running time of an older deepInstance" in { fixture => {
      fixture.importProjectSetupFromFile("model_deepInstance_01.json") flatMap (_ =>
        for {
          todoInstance1 <- fixture.instanceFactory.newInstance("Todo")
          todoInstance2 <- fixture.instanceFactory.newInstance("Todo")
        } yield {
          todoInstance1.typeHandle.getTimeIdentity.runningTime should be (todoInstance2.typeHandle.getTimeIdentity.runningTime)
        }
      )
    }
  }

  "A DeepInstance edit" must "not change timeIdentity" in { fixture => {
      fixture.importProjectSetupFromFile("model_deepInstance_01.json") flatMap (_ =>
        for {
          todoInstance <- fixture.instanceFactory.newInstance("Todo")
          _ <- todoInstance.unfold()
        } yield {
          val pre = todoInstance.typeHandle.getTimeIdentity
          todoInstance.assignDeepValue("Name", "abc")
          val post = todoInstance.typeHandle.getTimeIdentity
          pre should be(post)
        }
      )
    }
  }

  "Incrementing variant" must "not change timeIdentity of existing DeepInstances" in { fixture => {
      fixture.importProjectSetupFromFile("model_deepInstance_01.json") flatMap (_ =>
        for {
          todoInstance <- fixture.instanceFactory.newInstance("Todo")
          _ <- fixture.registry.incrementVariant
          post <- fixture.registry.getReferenceTimeIdentity
        } yield {
          todoInstance.typeHandle.getTimeIdentity.variantTime should be < post.variantTime
        }
      )
    }
  }

  "Deleting a DeepInstance" must "delete the complete ESI and not fail" in { fixture => {
    fixture.importProjectSetupFromFile("model_01.json") flatMap (_ =>
      for {
        todoInstance <- fixture.instanceFactory.newInstance("Todo")
        _ <- fixture.registry.autoRemove(todoInstance.instanceId)
        todoOption <- fixture.registry.get(todoInstance.instanceId)
      } yield {
       todoOption.isDefined should be(false)
      })
  }}

}
