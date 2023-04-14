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
package modicio.core.monitoring

import io.circe.generic.JsonCodec

import scala.collection.mutable.ListBuffer

/*
	A Class is defined by its variants and versions.
 */
@JsonCodec
case class Class(var typeName: String, var typeIdentity: String, var variants: ListBuffer[Variant] = new ListBuffer[Variant]){
	def addVariant(variantId: String, variantTime: Long): Variant = {
		val newVariant: Variant = Variant(variantTime, variantId)
		variants.addOne(newVariant)
		newVariant
	}
	
	def deleteVariant(variant: Variant): Unit = {
		this.variants = variants.filter(_ != variant)
	}
	
	def getVariant(variantId: String): Option[Variant] = variants.find(vs => vs.variantId == variantId)
	
}

