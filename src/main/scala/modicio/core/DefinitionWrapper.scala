package modicio.core

import modicio.core.datamappings.{PluginData, RuleData}
import modicio.core.rules.{AssociationRule, AttributeRule, ParentRelationRule}
import modicio.core.util.IODiff
import modicio.core.values.ConcreteValue
import modicio.verification.DefinitionVerifier

import scala.collection.mutable

class DefinitionWrapper
(
  definitionVerifier: DefinitionVerifier,
  private val inRules: Set[Rule] = Set(),
  private val inPlugins: Set[Plugin] = Set()
)
  extends Definition(definitionVerifier) {

  private val hotDefinition = new Definition(definitionVerifier, inRules, inPlugins)
  private val coldDefinition = new Definition(definitionVerifier, inRules, inPlugins)

  override def isVolatile: Boolean = hotDefinition.isVolatile

  override def cleanVolatile(): Unit = hotDefinition.cleanVolatile()

  override def setVolatile(): Unit = hotDefinition.setVolatile()

  override def getRules: Set[Rule] = hotDefinition.getRules

  override def toData(name: String, identity: String): Set[RuleData] = hotDefinition.toData(name, identity)

  override def fork(identity: String): DefinitionWrapper = {
    val newDefinition = new DefinitionWrapper(definitionVerifier)
    getRules.map(_.fork(identity)).foreach(newDefinition.applyRule)
    newDefinition
  }

  override private[modicio] def applyRule(rule: Rule): Unit = hotDefinition.applyRule(rule)

  override private[modicio] def removeRule(rule: Rule): Unit = hotDefinition.removeRule(rule)

  override private[modicio] def removeRuleByID(ruleID: String): Unit = hotDefinition.removeRuleByID(ruleID)

  override def applyPlugin(plugin: Plugin): Unit = hotDefinition.applyPlugin(plugin)

  override def removePlugin(plugin: Plugin): Unit = hotDefinition.removePlugin(plugin)

  override def getPlugins: Set[Plugin] = hotDefinition.getPlugins

  override def getAttributeRules: Set[AttributeRule] = hotDefinition.getAttributeRules

  override def getAssociationRules: Set[AssociationRule] = hotDefinition.getAssociationRules

  override def getParentRelationRules: Set[ParentRelationRule] = hotDefinition.getParentRelationRules
  override def getConcreteValues: Set[ConcreteValue] = hotDefinition.getConcreteValues

  def getRuleDiff(name: String, identity: String): IODiff[RuleData] = {
    val hotRules = hotDefinition.toData(name, identity)
    val coldRules = coldDefinition.toData(name, identity)
    compare(hotRules, coldRules)
  }

  def getPluginDiff(parent: ModelElement): IODiff[PluginData] = {
    val hotPlugins = hotDefinition.getPlugins.map(_.toData(parent))
    val coldPlugins = coldDefinition.getPlugins.map(_.toData(parent))
    compare(hotPlugins, coldPlugins)
  }

  def compare[T](hot: Set[T], cold: Set[T]): IODiff[T] = {
    val toUpdate = hot.intersect(cold)
    val toAdd = hot.diff(toUpdate)
    val toDelete = cold.diff(toUpdate)
    IODiff(toDelete, toAdd, toUpdate)
  }
}
