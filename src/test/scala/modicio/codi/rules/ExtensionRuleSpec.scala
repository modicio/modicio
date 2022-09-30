package modicio.codi.rules

import modicio.codi.Rule
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should

class ExtensionRuleSpec extends AnyFlatSpec with should.Matchers {

  val nativeStringNoId = ":#:Project"

  "An ExtensionRule" must "be correctly constructed from a (native value) DSL string with empty id" in {
    val extensionRule = new ExtensionRule(nativeStringNoId)
    extensionRule.parentName should equal ("Project")
    extensionRule.parentIdentity should equal ("#")
    extensionRule.id should be (Rule.UNKNOWN_ID)
  }

}
