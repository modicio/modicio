package modic.io.logic

import kotlin.reflect.KFunction2
import modic.io.model.*


object PredefinedFunctions {


    // find a dynamic way if needed JAVA reflection.
    private val functionMap: Map<String, KFunction2<Map<String, Any>, Fragment, Any>> = mapOf(
        "exampleFunction" to this::exampleFunction,
        "checkDeadline" to this::checkDeadline
    )

    private fun exampleFunction(params: Map<String, Any>, fragment: Fragment): String {
        return "Hello"
    }

    private fun checkDeadline(params: Map<String, Any>, fragment: Fragment): Boolean {
        // todo implement complicated functions ...
        val startTime = params["startTime"]
        return false
    }

    private fun defaultFunction(params: Map<String, Any>, fragment: Fragment): String {
        return "Function not found"
    }

    fun callFunction(scrip: Script, fragment: Fragment, node: Node, instanceService: InstanceService): Any? {
        val function = functionMap[scrip.name] ?: this::defaultFunction
        val args = createArgs(fragment, scrip.resolverMap())
        val output = function(args, fragment)
        if (scrip.actionType == "button"){
            return output
        }
        // only write to attributes that exist ASSUME THAT
        val resultName = "IsDeadLineCrossed" // todo return by output
        if (node.doesAttributeExist(resultName)){
            val attribute =  fragment.getAttributeInstance(resultName)
            attribute.anyValue = output.toString()
            instanceService.setAttributes(attribute)
        }
        return null
    }

    private fun createArgs(fragment: Fragment, resolver: Map<String, String>): Map<String, Any> {
        /**
         * Creates arguments for a function by mapping the instance's anyValue based on the provided resolver.
         * It uses the resolver's values as attribute names to fetch anyValue from the fragment's instance.
         * The resulting map pairs function parameter names with their corresponding attributeInstanceValues.
         *
         * Example:
         * Resolver: {description=Description}
         * If the instance's "Description" attribute has a value of "hello", the output will be {description="hello"}.
         *
         * @param fragment Initialized fragment to get the attribute Instance value.
         * @param resolver Mapping of argument name needed in function to argument name of Node.
         * @return A new dictionary with keys having Function parameter names and attributeInstanceValues in the values.
         */
        return resolver.mapValues { fragment.instance?.accessor()?.attributeByName(it.value)!!.anyValue }
    }

}