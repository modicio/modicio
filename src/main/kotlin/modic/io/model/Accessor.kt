package modic.io.model

/**
 * The Accessor is a helper class for doing model and instance traversal for easier access of often used field.
 * The Fragment must be autowired() before the Accessor can be used!
 */
class Accessor(private val instance: Instance,
               private val positionObject: IObject) {

    private val attributeRelation: MutableMap<Attribute, AttributeInstance> = HashMap()
    private val deepAttributeMap: Map<String, Map<String, AttributeInstance>>
    private val deepFlatAttributeRelation: MutableMap<Attribute, AttributeInstance>

    init {
        val ais = positionObject.getAttributeInstances()
        val adf = getObjectNode().getAttributes()
        adf.forEach { attribute ->
            val instance = ais.find { i -> i.attributeUri == attribute.uri }
            attributeRelation[attribute] = instance!!
        }
        deepAttributeMap = calculateDeepAttributeMap()
        deepFlatAttributeRelation = calculateDeepFlatAttributeRelation()
    }

    private fun calculateDeepAttributeMap(): Map<String, Map<String, AttributeInstance>> {
        val globalMap: MutableMap<String, Map<String, AttributeInstance>> = HashMap()
        val localMap: MutableMap<String, AttributeInstance> = HashMap()
        attributeRelation.forEach{ entry -> localMap[entry.key.uri] = entry.value}
        globalMap[getObjectNode().uri] = localMap
        getParents().forEach { parentObject ->
            val parentGlobalMap = parentObject.instance!!.accessor().deepAttributeMap()
            globalMap.putAll(parentGlobalMap)
        }
        return globalMap.toMap()
    }

    private fun calculateDeepFlatAttributeRelation(): MutableMap<Attribute, AttributeInstance> {
        val resultMap: MutableMap<Attribute, AttributeInstance> = HashMap()
        resultMap.putAll(attributeRelation)
        getParents().forEach { parentObject ->
            resultMap.putAll(parentObject.instance!!.accessor().deepFlatAttributeRelation())
        }
        return resultMap
    }

    fun getObject(): IObject {
        return positionObject
    }

    fun getObjectNode(): Node {
        return positionObject.node!!
    }

    fun getParents(): List<IObject> {
        val parentURIs = getObjectNode().getParentRelations().map { p -> p.uri }
        val scopedObjects = positionObject.instance!!.getObjects()
        return scopedObjects.filter { obj -> obj.instanceOf in parentURIs }
    }

    /**
     * Get an AttributeInstance by the name of the Attribute.
     * This method searches only in the local object (inherited attributes are not found)
     */
    fun attributeByName(attributeName: String): AttributeInstance? {
        val attribute = attributeRelation.keys.find { key -> key.name == attributeName }
        return attributeByDefinition(attribute)
    }

    /**
     * Get an AttributeInstance by the URI of the Attribute.
     * This method searches only in the local object (inherited attributes are not found)
     */
    fun attributeByUri(attributeURI: String): AttributeInstance? {
        val attribute = attributeRelation.keys.find { key -> key.uri == attributeURI }
        return attributeByDefinition(attribute)
    }

    fun attributeByDefinition(attribute: Attribute?): AttributeInstance? {
        if(attribute != null){
            return attributeRelation[attribute]
        }
        return null
    }

    fun definitionByAttribute(attributeInstance: AttributeInstance): Attribute {
        return getObjectNode().getAttributes().find { a -> a.uri == attributeInstance.attributeUri }!!
    }

    /**
     * Get the attributes of the IObject and its parents in a structured map.
     * NODE-URI -> ATTRIBUTE-URI -> ATTRIBUTE-INSTANCE
     */
    fun deepAttributeMap(): Map<String, Map<String, AttributeInstance>> {
        return deepAttributeMap
    }

    fun deepFlatAttributeRelation(): Map<Attribute, AttributeInstance> {
        return deepFlatAttributeRelation.toMap()
    }

    /**
     * Get an AttributeInstance by the name of the Attribute.
     * This method searches in the whole upper inheritance hierarchy
     */
    fun deepAttributeByName(attributeName: String): AttributeInstance? {
        val key = deepFlatAttributeRelation.keys.find { a -> a.name == attributeName }
        if(key != null) return deepFlatAttributeRelation[key]!!
        return null
    }

    /**
     * Get an AttributeInstance by the URI of the Attribute.
     * This method searches in the whole upper inheritance hierarchy
     */
    fun deepAttributeByURI(attributeURI: String): AttributeInstance? {
        return deepFlatAttributeRelation.values.find { v -> v.attributeUri == attributeURI }
    }

    fun composites(): List<CompositionInstance> {
        return positionObject.getCompositionInstances()
    }

    fun compositesByRule(compositionURI: String): List<CompositionInstance> {
        return positionObject.getCompositionInstances().filter { ci -> ci.compositionUri == compositionURI }
    }

}