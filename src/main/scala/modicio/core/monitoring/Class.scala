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

