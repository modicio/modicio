package modicio.core.monitoring

import scala.collection.mutable.ListBuffer

/*
	A Class is defined by its variants and versions.
 */
class Class(var typeName: String, var typeIdentity: String){
	var variants: ListBuffer[Variant] = new ListBuffer[Variant]
	def addVariant(variantId: String, variantTime: Long): Variant = {
		val newVariant: Variant = new Variant(variantTime, variantId)
		variants.addOne(newVariant)
		newVariant
	}
	
	def deleteVariant(variantId: String): Unit = {
		variants = variants.filter(_.variantId != variantId)
	}
	
	def accept(visitor: Visitor): Unit = {
		this.variants = visitor.visitVariant(this)
	}
	
	def getVariant(variantId: String): Option[Variant] = variants.find(v => v.variantId == variantId)
}

