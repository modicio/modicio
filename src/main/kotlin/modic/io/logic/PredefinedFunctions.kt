package modic.io.logic

import modic.io.model.Attribute
import modic.io.model.Fragment
import modic.io.model.Node
import modic.io.model.Script
import kotlin.reflect.KFunction2


object PredefinedFunctions {

    // find a dynamic way if needed JAVA reflection.
    private val functionMap: Map<String, KFunction2<Map<String, Any>, Fragment, Any>> = mapOf(
        "exampleFunction" to this::exampleFunction,
        "addHelloToDescription" to this::addHelloToDescription
    )

    private fun exampleFunction(params: Map<String, Any>, fragment: Fragment): String {
        return "Hello"
    }

    private fun addHelloToDescription(params: Map<String, Any>, fragment: Fragment): String {
        val description = params["description"] as? String ?: return "" // Provide a default value
        return description + "a"
    }

    private fun defaultFunction(params: Map<String, Any>, fragment: Fragment): String {
        return "Function not found"
    }

    private fun addAttributeFromFragment(fragment: Fragment) {
        // todo get the node of the arguments, get the number to add and the uri of the node
        // node2.addAttribute(Attribute(0, "modicio:demo.project.Description", "Description", "string"))

    }

    fun callFunction(scrip: Script, fragment: Fragment, node: Node): Any {


        // <Attribute uri="modicio:demo.project.title" name="Ttle" type="phrase" />
        // node.addAttribute(Attribute(0, node.uri + ".Result", "Result", "string"))
        val function = functionMap[scrip.name] ?: this::defaultFunction
        // println(fragment.model?.getAttributesWithValues())
        val args = mergeDictionaries(fragment.model?.getAttributesWithValues(), scrip.resolverMap())
        return function(args, fragment)
    }

    private fun mergeDictionaries(dict1: Map<String, String>?, dict2: Map<String, String>): Map<String, Any> {
        /**
         * Mapping to create the args for the functions.
         *
         * @param dict1 Attributes names and its values.
         * @param dict2 Attributes names to Function parameter names.
         * @return A new dictionary with keys having Function parameter names and attribute values in the values.
         */
        return dict2.mapNotNull { (key, value) -> dict1?.get(value)?.let { key to it } }.toMap()
    }

}