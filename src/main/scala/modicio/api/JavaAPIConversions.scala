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
package modicio.api

import modicio.codi._
import modicio.codi.api._
import modicio.codi.datamappings._
import modicio.codi.datamappings.api._
import modicio.codi.rules.api.{AssociationRuleJ, AttributeRuleJ, ExtensionRuleJ}
import modicio.codi.rules.{AssociationRule, AttributeRule, ExtensionRule}
import modicio.codi.values.api.{ConcreteAssociationJ, ConcreteAttributeJ, ConcreteValueJ, ValueDescriptorJ}
import modicio.codi.values.{ConcreteAssociation, ConcreteAttribute, ConcreteValue, ValueDescriptor}
import modicio.nativelang.defaults.SimpleMapRegistry
import modicio.nativelang.defaults.api.SimpleMapRegistryJ
import modicio.verification.api.{DefinitionVerifierJ, ModelVerifierJ}
import modicio.verification.{DefinitionVerifier, ModelVerifier}

import java.util
import java.util.concurrent.CompletableFuture
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.jdk.CollectionConverters._
import scala.jdk.FutureConverters._
import scala.jdk.OptionConverters._
import scala.language.implicitConversions

object JavaAPIConversions {

  trait ContainerFunctor[Container[_]] {
    def map[A, B](container: Container[A], f: A => B): Container[B]
  }

  implicit object SetFunctor extends ContainerFunctor[Set] {
    override def map[A, B](container: Set[A], f: (A) => B): Set[B] = {
      Option(container).map(_.map(f)).getOrElse(Set.empty[B])
    }
  }

  implicit object SeqFunctor extends ContainerFunctor[Seq] {
    override def map[A, B](container: Seq[A], f: (A) => B): Seq[B] = {
      Option(container).map(_.map(f)).getOrElse(Seq.empty[B])
    }
  }

  implicit object OptionFunctor extends ContainerFunctor[Option] {
    override def map[A, B](container: Option[A], f: (A) => B): Option[B] = {
      Option(container).map(_.map(f)).getOrElse(Option.empty[B])
    }
  }

  implicit def functorConvert[F[_], A, B](x: F[A])(implicit f: A => B, functor: ContainerFunctor[F]): F[B] = functor.map(x, f)


  implicit def futureConvert[T, S <: Iterable[T], B](x: Future[S])(implicit f: T => B): CompletableFuture[Iterable[B]] = {
    x.map(t => Option(t).map(_.map(f)).getOrElse(Option.empty[B]).iterator.to(Iterable)).toCompletableFuture
  }

  implicit def futureConvert[T, S <: java.util.Collection[T], B](x: CompletableFuture[S])(implicit f: T => B): Future[Iterable[B]] = {
    x.asScala.map(t => Option(t).map(_.asScala.map(f)).getOrElse(Option.empty[B]).iterator.to(Iterable))
  }

  implicit def futureConvertSet[T, B](x: CompletableFuture[java.util.Set[T]])(implicit f: T => B): Future[Set[B]] = {
   futureConvert[T, java.util.Set[T], B](x).map(_.toSet)
  }

  implicit def futureConvertOption[T, B](x: CompletableFuture[java.util.Optional[T]])(implicit f: T => B): Future[Option[B]] = {
    x.asScala.map(t => convert(t).map(f))
  }

  implicit def convert[T](value: java.util.concurrent.CompletableFuture[T]): Future[T] = value.asScala

  implicit def convert[T](value: Future[T]): java.util.concurrent.CompletableFuture[T] = value.asJava.toCompletableFuture

  implicit def convert[T](value: Option[T]): java.util.Optional[T] = value.toJava

  implicit def convert[T](value: java.util.Optional[T]): Option[T] = value.toScala

  implicit def convert[T](value: java.util.Set[T]): Set[T] = value.asScala.toSet

  implicit def convert[T](value: Set[T]): java.util.Set[T] = value.asJava

  implicit def convert[T](value: java.util.List[T]): Seq[T] = value.asScala.toSeq

  implicit def convert[T](value: Seq[T]): java.util.List[T] = value.asJava

  implicit def convert[T, K](value: Map[T, K]): java.util.Map[T, K] = value.asJava

  implicit def convert[T, K](value: java.util.Map[T, K]): Map[T, K] = value.asScala.toMap

  implicit def convert(value: TypeHandle): TypeHandleJ = new TypeHandleJ(value.getFragment, value.getIsStatic)

  implicit def convert(value: DeepInstance): DeepInstanceJ = new DeepInstanceJ(value.instanceId, value.identity, value.shape, value.typeHandle, value.registry)

  implicit def convert(value: RegistryJ): Registry = value.getRegistry

  implicit def convert(value: Registry): RegistryJ = value match {
    case value: SimpleMapRegistry => value
    case _ => throw new IllegalArgumentException()
  }

  implicit def convert(simpleMapRegistry: SimpleMapRegistry): SimpleMapRegistryJ = {
    val res = new SimpleMapRegistryJ(simpleMapRegistry.typeFactory, simpleMapRegistry.instanceFactory)
    res.load(simpleMapRegistry)
    res
  }

  implicit def convert(instanceFactory: modicio.codi.InstanceFactory): InstanceFactoryJ = {
    new InstanceFactoryJ(instanceFactory.definitionVerifier, instanceFactory.modelVerifier)
  }

  implicit def convert(typeFactory: TypeFactory): TypeFactoryJ = {
    new TypeFactoryJ(typeFactory.definitionVerifier, typeFactory.modelVerifier)
  }

  implicit def convert(value: Definition): DefinitionJ = new DefinitionJ(value)

  implicit def convert(value: RuleJ): Rule = value.getRule

  implicit def convert(value: Rule): RuleJ = value match {
    case rule: AssociationRule => rule
    case rule: AttributeRule => rule
    case value: ConcreteValue => value
    case rule: ExtensionRule => rule
    case _ => throw new IllegalArgumentException()
  }

  implicit def convertBase(value: Base): BaseJ = value match {
    case j: BaseJ => j
    case model: BaseModel => throw new IllegalArgumentException()
    case definition: Definition => convert(definition)
    case _ => throw new IllegalArgumentException()
  }

  implicit def convert(value: ValueDescriptorJ): ValueDescriptor = value.getValueDescriptor

  implicit def convert(value: RuleData): RuleDataJ =
    RuleDataJ tupled RuleData.unapply(value).get

  implicit def convert(value: RuleDataJ): RuleData =
    RuleData tupled RuleDataJ.unapply(value).get

  implicit def convert(value: AttributeRule): AttributeRuleJ = new AttributeRuleJ(value.nativeValue)

  implicit def convert(value: AssociationRule): AssociationRuleJ = new AssociationRuleJ(value.nativeValue)

  implicit def convert(value: ExtensionRule): ExtensionRuleJ = new ExtensionRuleJ(value.nativeValue)

  implicit def convert(value: ConcreteValue): ConcreteValueJ = new ConcreteValueJ(value.nativeValue)

  implicit def convert(value: ConcreteAssociation): ConcreteAssociationJ = new ConcreteAssociationJ(value.nativeValue)

  implicit def convert(value: ConcreteAttribute): ConcreteAttributeJ = new ConcreteAttributeJ(value.nativeValue)

  implicit def convert(value: FragmentData): FragmentDataJ = FragmentDataJ tupled FragmentData.unapply(value).get

  implicit def convert(value: FragmentDataJ): FragmentData = FragmentData tupled FragmentDataJ.unapply(value).get

  implicit def convert(value: AssociationData): AssociationDataJ =
    AssociationDataJ tupled AssociationData.unapply(value).get

  implicit def convert(value: AssociationDataJ): AssociationData =
    AssociationData tupled AssociationDataJ.unapply(value).get

  implicit def convert(value: AttributeData): AttributeDataJ = AttributeDataJ tupled AttributeData.unapply(value).get

  implicit def convert(value: AttributeDataJ): AttributeData = AttributeData tupled AttributeDataJ.unapply(value).get

  implicit def convert(value: ExtensionData): ExtensionDataJ = ExtensionDataJ tupled ExtensionData.unapply(value).get

  implicit def convert(value: ExtensionDataJ): ExtensionData = ExtensionData tupled ExtensionDataJ.unapply(value).get

  implicit def convert(value: InstanceData): InstanceDataJ = InstanceDataJ tupled InstanceData.unapply(value).get

  implicit def convert(value: InstanceDataJ): InstanceData = InstanceData tupled InstanceDataJ.unapply(value).get

  implicit def convert(value: ImmutableShape): ImmutableShapeJ =
    ImmutableShapeJ(value.instanceData, convert(value.attributes), convert(value.associations), convert(value.extensions))

  implicit def convert(value: DefinitionVerifier): DefinitionVerifierJ = {
    new DefinitionVerifierJ {
      override def verifyJ(rules: util.Set[RuleJ]): Boolean = value.verify(rules map convert)
    }
  }

  implicit def convert(value: ModelVerifier): ModelVerifierJ = {
    new ModelVerifierJ {
      override def verifyJ(typeHandle: TypeHandleJ): Boolean = value.verify(typeHandle)
    }
  }

  implicit def convert(value: Shape): ShapeJ =
    new ShapeJ(convert(value.getAttributes), convert(value.getAssociations), convert(value.getExtensions))


  implicit def convert(value: TypeIterator): TypeIteratorJ = new TypeIteratorJ(value.initialFragment)
}
