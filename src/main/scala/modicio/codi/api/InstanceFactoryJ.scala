package modicio.codi.api

import modicio.codi.datamappings.InstanceData
import modicio.codi.{DeepInstance, Shape, TypeHandle}
import modicio.verification.{DefinitionVerifier, ModelVerifier}

import modicio.api.JavaAPIConversions._

class InstanceFactoryJ(definitionVerifier: DefinitionVerifier,
                       modelVerifier: ModelVerifier) extends modicio.codi.InstanceFactory(definitionVerifier, modelVerifier){

  def newInstanceJ(typeName: java.lang.String): java.util.concurrent.CompletableFuture[DeepInstance] = super.newInstance(typeName)

  def newInstanceJ(typeName: java.lang.String, newIdentity: java.lang.String): java.util.concurrent.CompletableFuture[DeepInstance] = super.newInstance(typeName, newIdentity)

  def loadInstanceJ(instanceData: InstanceData, shape: Shape, typeHandle: TypeHandle): java.util.Optional[DeepInstance] = super.loadInstance(instanceData, shape, typeHandle)
}
