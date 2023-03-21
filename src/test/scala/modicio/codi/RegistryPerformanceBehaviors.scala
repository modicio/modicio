package modicio.codi

import modicio.core.rules.{AssociationRule, AttributeRule, ConnectionInterface, ParentRelationRule}
import modicio.core.util.IdentityProvider.newRandomId
import modicio.core.{DeepInstance, TypeHandle}
import modicio.nativelang.util.AccessCounting
import modicio.{AsyncSpec, RegistryFixture}

import scala.concurrent.Future

trait RegistryPerformanceBehaviors { this: AsyncSpec =>

  def createAndModifyTodoInstance(fixture: RegistryFixture, projectInstance: DeepInstance): Future[DeepInstance] = {
    for {
      todoInstance <- fixture.instanceFactory.newInstance("Todo")
      _ <- todoInstance.unfold()

      _ <- Future.successful(projectInstance.associate(todoInstance, fixture.TODO, fixture.PROJECT_HAS_PART))
      _ <- projectInstance.commit
      projectInstanceOption <- fixture.registry.get(projectInstance.instanceId)
      projectInstance <- Future.successful(projectInstanceOption.get)
      _ <- projectInstance.unfold()

      _ <- Future.successful(todoInstance.associate(projectInstance, fixture.PROJECT, fixture.IS_PART_OF))
      _ <- todoInstance.commit
      todoInstanceOption <- fixture.registry.get(todoInstance.instanceId)
      todoInstance <- Future.successful(todoInstanceOption.get)
      _ <- todoInstance.unfold()

      _ <- Future.successful(todoInstance.assignValue("Content", newRandomId()))
      _ <- todoInstance.commit
      todoInstanceOption <- fixture.registry.get(todoInstance.instanceId)
      todoInstance <- Future.successful(todoInstanceOption.get)
      _ <- todoInstance.unfold()

      _ <- Future.successful(todoInstance.assignDeepValue("Title", newRandomId()))
      _ <- todoInstance.commit
      todoInstanceOption <- fixture.registry.get(todoInstance.instanceId)
      todoInstance <- Future.successful(todoInstanceOption.get)
      _ <- todoInstance.unfold()
    } yield {
      todoInstance
    }
  }

  def createInstance(fixture: RegistryFixture, typeName: String): Future[DeepInstance] = {
    for {
      instance <- fixture.instanceFactory.newInstance(typeName)
      _ <- instance.unfold()
      _ <- instance.commit
      instanceOption <- fixture.registry.get(instance.instanceId)
      instance <- Future.successful(instanceOption.get)
      _ <- instance.unfold()
    } yield {
      instance
    }
  }

  def createAndModifyType(fixture: RegistryFixture, projectType: TypeHandle): Future[TypeHandle] = {
    val typeName = newRandomId() + "Type"
    for {
      testType <- fixture.typeFactory.newType(typeName, "#", isTemplate = false, Some(fixture.TIME_IDENTITY))
      _ <- fixture.registry.setType(testType)
      _ <- Future.successful({
        testType.applyRule(AssociationRule.create("testRule", "Todo", "*", ConnectionInterface.parseInterface(fixture.TIME_IDENTITY.variantTime.toString, "Todo")))
        testType.applyRule(AttributeRule.create("ABC", "String", nonEmpty = true))
        testType.applyRule(ParentRelationRule.create(projectType.getTypeName, projectType.getTypeIdentity))
      })
      _ <- testType.commit()
      testTypeOption <- fixture.registry.getType(typeName, "#")
      testType <- Future.successful(testTypeOption.get)
    } yield {
      testType
    }
  }

  def createType(fixture: RegistryFixture): Future[TypeHandle] = {
    val typeName = newRandomId() + "Type"
    for {
      testType <- fixture.typeFactory.newType(typeName, "#", isTemplate = false, Some(fixture.TIME_IDENTITY))
      _ <- fixture.registry.setType(testType)
      _ <- testType.commit()
      testTypeOption <- fixture.registry.getType(typeName, "#")
      testType <- Future.successful(testTypeOption.get)
    } yield {
      testType
    }
  }

  def performance(newFixture: => RegistryFixture with AccessCounting, fileNameModifier: String): Unit = {
    it must "perform a set of best case database operations" in {
      val fixture = newFixture
      fixture.importProjectSetupFromFile("model_02.json") flatMap (_ =>
        for {
          projectInstance1 <- fixture.instanceFactory.newInstance("Project")
          _ <- projectInstance1.unfold()
          _ <- projectInstance1.commit
          _ <- Future.sequence(for (_ <- 1 to 200) yield createAndModifyTodoInstance(fixture, projectInstance1))
          projectType <- fixture.registry.getType("Project", "#")

          t <- createAndModifyType(fixture, projectType.get)
          _ <- Future.sequence(for (_ <- 1 to 20) yield createInstance(fixture, t.getTypeName))

          t <- createAndModifyType(fixture, projectType.get)
          _ <- Future.sequence(for (_ <- 1 to 20) yield createInstance(fixture, t.getTypeName))

          t <- createAndModifyType(fixture, projectType.get)
          _ <- Future.sequence(for (_ <- 1 to 20) yield createInstance(fixture, t.getTypeName))

          t <- createAndModifyType(fixture, projectType.get)
          _ <- Future.sequence(for (_ <- 1 to 20) yield createInstance(fixture, t.getTypeName))

          t <- createAndModifyType(fixture, projectType.get)
          _ <- Future.sequence(for (_ <- 1 to 20) yield createInstance(fixture, t.getTypeName))

        } yield {
          fixture.writeAccessCounts("registry_performance_best_case_" + fileNameModifier, ".")
          1 should be(1)
        }
        )
    }

    it must "perform a set of worst case database operations" in {
      val fixture = newFixture
      fixture.importProjectSetupFromFile("model_02.json") flatMap (_ =>
        for {
          projectInstance <- fixture.instanceFactory.newInstance("Project")
          _ <- projectInstance.unfold()
          _ <- projectInstance.commit
          projectInstanceOption <- fixture.registry.get(projectInstance.instanceId)
          projectInstance <- Future.successful(projectInstanceOption.get)
          _ <- projectInstance.unfold()

          todos <- Future.sequence(for (_ <- 1 to 200) yield createInstance(fixture, "Todo"))

          _ <- Future.successful(for (todo <- todos) {
            projectInstance.associate(todo, fixture.TODO, fixture.PROJECT_HAS_PART)
            todo.associate(projectInstance, fixture.PROJECT, fixture.IS_PART_OF)
          })
          _ <- Future.sequence(for (todo <- todos) yield todo.commit)
          _ <- projectInstance.commit
          todosOptions <- Future.sequence(for (todo <- todos) yield fixture.registry.get(todo.instanceId))
          todos <- Future.successful(for (todoOption <- todosOptions) yield todoOption.get)
          _ <- Future.sequence(for (todo <- todos) yield todo.unfold())

          _ <- Future.successful(for (todo <- todos) {
            todo.assignValue("Content", newRandomId())
          })
          _ <- Future.sequence(for (todo <- todos) yield todo.commit)
          todosOptions <- Future.sequence(for (todo <- todos) yield fixture.registry.get(todo.instanceId))
          todos <- Future.successful(for (todoOption <- todosOptions) yield todoOption.get)
          _ <- Future.sequence(for (todo <- todos) yield todo.unfold())

          _ <- Future.successful(for (todo <- todos) {
            todo.assignDeepValue("Title", newRandomId())
          })
          _ <- Future.sequence(for (todo <- todos) yield todo.commit)

          projectType <- fixture.registry.getType("Project", "#")

          types <- Future.sequence(for (_ <- 1 to 100) yield createAndModifyType(fixture, projectType.get))
          _ <- Future.sequence(for (t <- types) yield createInstance(fixture, t.getTypeName))

        } yield {
          fixture.writeAccessCounts("registry_performance_worst_case_" + fileNameModifier, ".")
          1 should be(1)
        }
        )
    }
  }
}
