package modicio.codi.api

import modicio.codi
import modicio.codi.{Fragment, Rule}

import modicio.api.JavaAPIConversions._

class TypeHandleJ(fragment: Fragment, static: Boolean) extends modicio.codi.TypeHandle(fragment, static) {
  override def getTypeName: String = super.getTypeName

  def getTypeIdentityJ: java.lang.String = super.getTypeIdentity

  def hasSingletonJ: java.util.concurrent.CompletableFuture[Boolean] = super.hasSingleton

  def hasSingletonRootJ: java.util.concurrent.CompletableFuture[Boolean] = super.hasSingletonRoot

  def updateSingletonRootJ(): java.util.concurrent.CompletableFuture[Any] = super.updateSingletonRoot()

  def unfoldJ(): java.util.concurrent.CompletableFuture[codi.TypeHandle] = super.unfold()

  def commitJ(): java.util.concurrent.CompletableFuture[Unit] = super.commit()

  def applyRuleJ(ruleJ: RuleJ): Unit = {
    //FIXME convert to default Rule
    super.applyRule(ruleJ.getRule)
  }

  def removeRuleJ(ruleJ: RuleJ): Unit = {
    //FIXME convert to default Rule
    super.removeRule(ruleJ.getRule)
  }

  def getAssociatedJ: java.util.Set[codi.TypeHandle] = super.getAssociated
}
