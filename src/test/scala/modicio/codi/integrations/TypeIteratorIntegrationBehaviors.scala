package modicio.codi.integrations

import modicio.core.TypeIterator
import modicio.{AsyncSpec, RegistryFixture, Spec}

import scala.concurrent.Future

trait TypeIteratorIntegrationBehaviors { this: AsyncSpec =>

  def typeIterator(newFixture: => RegistryFixture) {
    it must "create new TypeIterator of the current element for split()" in {
      val fixture = newFixture
      fixture.importProjectSetupFromFile("model_01.json") flatMap (_ =>
        for {
          todoInstance <- fixture.registry.getType("Todo", "#")
          modelElement <- Future.successful(todoInstance.get.getModelElement)
          _ <- modelElement.unfold()
        } yield {
          val typeIterator = new TypeIterator(modelElement)
          val newTypeIterator = typeIterator.split.get
          newTypeIterator.name should be("Todo")
        }
      )
    }

    it must "return TypeHandle for the current element for splitHandle()" in {
      val fixture = newFixture
      fixture.importProjectSetupFromFile("model_01.json") flatMap (_ =>
        for {
          todoInstance <- fixture.registry.getType("Todo", "#")
          modelElement <- Future.successful(todoInstance.get.getModelElement)
          _ <- modelElement.unfold()
        } yield {
          val typeIterator = new TypeIterator(modelElement)
          val typeHandle = typeIterator.splitHandle.get
          typeHandle.getTypeName should be("Todo")
        }
      )
    }

    it must "return a TypeHandle for each parent for getAssociated()" in {
      val fixture = newFixture
      fixture.importProjectSetupFromFile("model_01.json") flatMap (_ =>
        for {
          todoInstance <- fixture.registry.getType("Todo", "#")
          modelElement <- Future.successful(todoInstance.get.getModelElement)
          _ <- modelElement.unfold()
        } yield {
          val typeIterator = new TypeIterator(modelElement)
          val parentTypeHandleSet = typeIterator.getAssociated
          parentTypeHandleSet.head.getTypeName should be("ProjectItem")
          parentTypeHandleSet.size should be(1)
        }
      )
    }

    it must "set parent as current elementfor up()" in {
      val fixture = newFixture
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

    it must "clear internal buffer if there is no next element for next()" in {
      val fixture = newFixture
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

    it must "return None when no element is inside buffer for get()" in {
      val fixture = newFixture
      fixture.importProjectSetupFromFile("model_01.json") flatMap (_ =>
        for {
          todoInstance <- fixture.registry.getType("Todo", "#")
          modelElement <- Future.successful(todoInstance.get.getModelElement)
          _ <- modelElement.unfold()
        } yield {
          val typeIterator = new TypeIterator(modelElement)
          typeIterator.next
          val mustBeNone = typeIterator.get
          mustBeNone should be(None)
        }
      )
    }

    it must "return None when no element is inside buffer for asDefinition()" in {
      val fixture = newFixture
      fixture.importProjectSetupFromFile("model_01.json") flatMap (_ =>
        for {
          todoInstance <- fixture.registry.getType("Todo", "#")
          modelElement <- Future.successful(todoInstance.get.getModelElement)
          _ <- modelElement.unfold()
        } yield {
          val typeIterator = new TypeIterator(modelElement)
          typeIterator.next
          val mustBeNone = typeIterator.asDefinition
          mustBeNone should be(None)
        }
      )
    }

    it must "return the definition of the element inside buffer" in {
      val fixture = newFixture
      fixture.importProjectSetupFromFile("model_01.json") flatMap (_ =>
        for {
          todoInstance <- fixture.registry.getType("Todo", "#")
          modelElement <- Future.successful(todoInstance.get.getModelElement)
          _ <- modelElement.unfold()
        } yield {
          val typeIterator = new TypeIterator(modelElement)
          val definition = typeIterator.asDefinition.get
          definition should be(modelElement.definition)
        }
      )
    }
  }

}
