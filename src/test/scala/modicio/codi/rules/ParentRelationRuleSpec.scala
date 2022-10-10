package modicio.codi.rules

import modicio.core.Rule
import modicio.core.rules.ParentRelationRule
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should

class ParentRelationRuleSpec extends AnyFlatSpec with should.Matchers {

  val nativeStringNoId = ":#:Project"

  "An ExtensionRule" must "be correctly constructed from a (native value) DSL string with empty id" in {
    val extensionRule = new ParentRelationRule(nativeStringNoId)
    extensionRule.parentName should equal ("Project")
    extensionRule.parentIdentity should equal ("#")
    extensionRule.id should be (Rule.UNKNOWN_ID)
  }

}
