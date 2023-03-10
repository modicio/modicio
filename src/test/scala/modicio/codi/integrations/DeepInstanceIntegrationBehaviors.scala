package modicio.codi.integrations

import modicio.{Spec, RegistryFixture}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
trait DeepInstanceIntegrationBehaviors { this: Spec =>

  def deepInstance (newFixture: => RegistryFixture): Unit = {
    it must "change the value of the correspondent key for assignValue" in {
      val fixture = newFixture
      fixture.importProjectSetupFromFile("model_01.json") flatMap (_ =>
        for {
          todoInstance <- fixture.instanceFactory.newInstance("Todo")
        } yield {
          todoInstance.assignValue("Content", "abc")
          todoInstance.value("Content").get should be("abc")
        }
        )
    }

    it must "change the value of the correspondent key for ssignDeepValue" in {
      val fixture = newFixture
      fixture.importProjectSetupFromFile("model_deepInstance_01.json") flatMap (_ =>
        for {
          todoInstance <- fixture.instanceFactory.newInstance("Todo")
          _ <- todoInstance.unfold()
        } yield {
          todoInstance.assignDeepValue("Content", "abc")
          todoInstance.value("Content").get should be("abc")
        }
        )
    }

    it must "change the value of the correspondent key of the parent for assignDeepValue" in {
      val fixture = newFixture
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

    it must "change if applied sequentially with commit for assignDeepValue" in {
      val fixture = newFixture
      fixture.importProjectSetupFromFile("model_deepInstance_01.json") flatMap (_ =>
        for {
          todoInstance <- fixture.instanceFactory.newInstance("Todo")
          todoOption <- fixture.registry.get(todoInstance.instanceId)
          _ <- todoOption.get.unfold()
          _ <- Future(todoOption.get.assignDeepValue("Content", "abc"))
          _ <- Future(todoOption.get.assignDeepValue("Name", "abc"))
          _ <- todoOption.get.commit
          td2 <- fixture.registry.get(todoInstance.instanceId)
          _ <- td2.get.unfold()
        } yield {
          val m = td2.get.getDeepAttributes
          td2.get.deepValue("Content").get should be("abc")
          td2.get.deepValue("Name").get should be("abc")
        })
    }

    it must "not increment the running time of the model upon creating a new DeepInstance" in {
      val fixture = newFixture
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

    it must "not increment the running time of an older deepInstance upon creating a new DeepInstance" in {
      val fixture = newFixture
      fixture.importProjectSetupFromFile("model_deepInstance_01.json") flatMap (_ =>
        for {
          todoInstance1 <- fixture.instanceFactory.newInstance("Todo")
          todoInstance2 <- fixture.instanceFactory.newInstance("Todo")
        } yield {
          todoInstance1.typeHandle.getTimeIdentity.runningTime should be(todoInstance2.typeHandle.getTimeIdentity.runningTime)
        }
        )
    }

    it must "not change timeIdentity upon a DeepInstance edit" in {
      val fixture = newFixture
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

    it must "not change timeIdentity of existing DeepInstances upon incrementing variant" in {
      val fixture = newFixture
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

    it must "delete the complete ESI and not fail upon deleting a DeepInstance " in {
      val fixture = newFixture
      fixture.importProjectSetupFromFile("model_01.json") flatMap (_ =>
        for {
          todoInstance <- fixture.instanceFactory.newInstance("Todo")
          _ <- fixture.registry.autoRemove(todoInstance.instanceId)
          todoOption <- fixture.registry.get(todoInstance.instanceId)
        } yield {
          todoOption.isDefined should be(false)
        })
    }

    it must "not cause an error upon changing the value of an Attribute" in {
      val fixture = newFixture
      fixture.importProjectSetupFromFile("model_01.json") flatMap (_ =>
        for {
          todoInstance <- fixture.instanceFactory.newInstance("Todo")
          todoOption <- fixture.registry.get(todoInstance.instanceId)
          _ <- todoOption.get.unfold()
          _ <- Future(todoOption.get.assignDeepValue("Content", "foo"))
          _ <- todoOption.get.commit
          todoOption <- fixture.registry.get(todoInstance.instanceId)
          _ <- todoOption.get.unfold()
          _ <- Future(todoOption.get.assignDeepValue("Content", "bar"))
          _ <- Future.successful()
          _ <- todoOption.get.commit
          todoOption <- fixture.registry.get(todoInstance.instanceId)
          _ <- todoOption.get.unfold()
        } yield {
          todoOption.get.deepValue("Content").get should be("bar")
        })
    }
  }

}
