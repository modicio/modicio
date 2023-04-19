/**
 * Copyright 2023 Minji Kim
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
package modicio.codi.values

import modicio.core.values.ConcreteValue
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should

class ConcreteValueSpec extends AnyFlatSpec with should.Matchers {
	
	"An AssociationValue" must "be correctly constructed from a (native value) DSL string with id" in {
		val associationValue = ConcreteValue.create(ConcreteValue.ASSOCIATION_VALUE, "relation", Seq("def", "instanceName", "true"), Option("abc"))
		associationValue.isAssociationValue should be(true)
		associationValue.getValueType should be(ConcreteValue.ASSOCIATION_VALUE)
		associationValue.serialise() should be("abc:ASSOCIATION:(def:instanceName:true)")
		associationValue.serialiseSimple() should be("ASSOCIATION := def:instanceName final=true")
		val thrown = the[UnsupportedOperationException] thrownBy associationValue.getAttributeDescriptor
		thrown.getMessage should equal(null)
	}
	
	"An AttributeValue" must "be correctly constructed from a (native value) DSL string with id" in {
		val attributeValue = ConcreteValue.create(ConcreteValue.ATTRIBUTE_VALUE, "Content", Seq("contentValue", "true"), Option("ghi"))
		attributeValue.isAttributeValue should be(true)
		attributeValue.getValueType should be(ConcreteValue.ATTRIBUTE_VALUE)
		attributeValue.serialise() should be("ghi:ATTRIBUTE:(contentValue:true)")
		attributeValue.serialiseSimple() should be("ATTRIBUTE := contentValue final=true")
		val thrown = the[UnsupportedOperationException] thrownBy attributeValue.getAssociationDescriptor
		thrown.getMessage should equal(null)
	}
	
}
