package modicio.core.monitoring

/*
	A Class is defined by its variants and versions.
 */
class Class(var identity: String){
	var variants: List[Variant] = _
	def addVariant(variantId: String, variantTime: Long): Variant = {
		val newVariant: Variant = new Variant(variantTime, variantId)
		variants :+= newVariant
		newVariant
	}
	
	def deleteVariant(variantId: String): Unit = {
		variants = variants.filter(_.variantId != variantId)
	}
	
	def getVariant(variantId: String): Option[Variant] = {
		variants.find(v => v.variantId == variantId)
	}
	
	def accept(visitor: Visitor): Unit = {
		this.variants = visitor.visitVariant(this)
	}

}

