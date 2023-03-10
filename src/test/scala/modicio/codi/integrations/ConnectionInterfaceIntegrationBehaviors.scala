package modicio.codi.integrations

import modicio.{Spec, RegistryFixture}

import scala.concurrent.ExecutionContext.Implicits.global

trait ConnectionInterfaceIntegrationBehaviors { this: Spec =>

  def connectionInterface(newFixture: => RegistryFixture): Unit = {
    it must "create correct AssociationData if a matching Slot is found upon DeepInstance.associate()" in {
      val fixture = newFixture
      fixture.initProjectSetup() flatMap (_ =>
        for {
          todoInstance <- fixture.instanceFactory.newInstance(fixture.TODO)
          projectInstance <- fixture.instanceFactory.newInstance(fixture.PROJECT)
        } yield {
          val res = projectInstance.associate(todoInstance, fixture.TODO, fixture.PROJECT_CONTAINS_TODO)
          res should be(true)
        }
      )
    }

    it must "fail if no matching Slot is found by targetName upon DeepInstance.associate()" in {
      val fixture = newFixture
      fixture.initProjectSetup() flatMap (_ =>
        for {
          todoInstance <- fixture.instanceFactory.newInstance(fixture.TODO)
          projectInstance <- fixture.instanceFactory.newInstance(fixture.PROJECT)
        } yield {
          val thrown = intercept[Exception] {
            projectInstance.associate(todoInstance, fixture.PROJECT, fixture.PROJECT_CONTAINS_TODO)
          }
          assert(thrown.getMessage === "The proposed relation is not defined on top of a type which is in the instance type hierarchy")
        }
      )
    }
  }
}
