package modicio.codi

import modicio.nativelang.util.AccessCounting
import modicio.{AsyncSpec, RegistryFixture}

trait RegistryPerformanceBehaviors { this: AsyncSpec =>

  def performance(newFixture: => RegistryFixture with AccessCounting, fileNameModifier: String) {
    it must "perform a number of database operations" in {
      val fixture = newFixture
      fixture.importProjectSetupFromFile("model_02.json") flatMap (_ =>
        for {
          (todoInstance1, todoInstance2) <- for {
            todoInstance1 <- fixture.instanceFactory.newInstance("Todo")
            todoInstance2 <- fixture.instanceFactory.newInstance("Todo")
            projectInstance1 <- fixture.instanceFactory.newInstance("Project")
            _ <- todoInstance1.unfold()
            _ <- todoInstance2.unfold()
            _ <- projectInstance1.unfold()
          } yield {
            projectInstance1.associate(todoInstance1, fixture.TODO, fixture.PROJECT_HAS_PART)
            projectInstance1.commit
            projectInstance1.associate(todoInstance2, fixture.TODO, fixture.PROJECT_HAS_PART)
            projectInstance1.commit

            todoInstance1.associate(projectInstance1, fixture.PROJECT, fixture.IS_PART_OF)
            todoInstance1.commit
            todoInstance2.associate(projectInstance1, fixture.PROJECT, fixture.IS_PART_OF)
            todoInstance2.commit

            todoInstance1.assignValue("Content", "abc")
            todoInstance1.commit
            todoInstance1.assignDeepValue("Title", "abc")
            todoInstance1.commit

            todoInstance2.assignValue("Content", "Todo1")
            todoInstance2.commit
            todoInstance2.assignDeepValue("Title", "Todo2")
            todoInstance2.commit

            (todoInstance1, todoInstance2)
          }
          _ <- for {
            todoOption1 <- fixture.registry.get(todoInstance1.instanceId)
            _ <- todoOption1.get.unfold()
            todoOption2 <- fixture.registry.get(todoInstance2.instanceId)
            _ <- todoOption2.get.unfold()
          } yield {}
        } yield {
          fixture.writeAccessCounts("registry_performance_" + fileNameModifier, ".")
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
