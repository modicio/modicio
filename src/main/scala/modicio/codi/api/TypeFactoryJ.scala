package modicio.codi.api

import modicio.api.JavaAPIConversions._
import modicio.codi.datamappings.api.{FragmentDataJ, RuleDataJ}
import modicio.codi.{Rule, TypeFactory}
import modicio.verification.{DefinitionVerifier, ModelVerifier}

class TypeFactoryJ(definitionVerifier: DefinitionVerifier, modelVerifier: ModelVerifier)
  extends TypeFactory(definitionVerifier, modelVerifier) {

  def setRegistryJ(registry: RegistryJ): Unit = super.setRegistry(registry)

  def newTypeJ(name: java.lang.String, identity: java.lang.String, isTemplate: Boolean): TypeHandleJ = super.newType(name, identity, isTemplate)

  def loadTypeJ(fragmentData: FragmentDataJ, ruleData: java.util.Set[RuleDataJ]): TypeHandleJ = super.loadType(fragmentData, ruleData.map(convert))

  def loadRuleJ(ruleData: RuleDataJ): Rule = super.loadRule(ruleData)
}
