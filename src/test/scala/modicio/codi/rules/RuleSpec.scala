package modicio.codi.rules

import modicio.codi.Rule
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should

class RuleSpec extends AnyFlatSpec with should.Matchers {

  val nativeStringNoId = ":#:Project"
  val nativeStringId = "abc:#:Project"

  "Rule" must "be correctly constructed from a (native value) DSL string with empty id" in {
    val rule = new ExtensionRule(nativeStringNoId)
    rule.id should be (Rule.UNKNOWN_ID)
  }

  it must "be correctly evaluate a predefined id" in {
    val rule = new ExtensionRule(nativeStringId)
    rule.id should be("abc")
  }

  it must "be correctly using autoId if enabled" in {
    Rule.enableAutoID()
    val rule = new ExtensionRule(nativeStringNoId)
    Rule.disableAutoID()
    rule.id should not be Rule.UNKNOWN_ID
  }

}
