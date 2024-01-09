package modic.io.logic

import modic.io.controller.InstanceController
import kotlin.reflect.KFunction2
import modic.io.logic.InstanceService
import modic.io.model.*
import modic.io.repository.FragmentRepository


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
        // todo implement complicated functions ...
        val description = params["description"] as? String ?: return "" // Provide a default value
        return description + "a"
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
        val resultName = "Result" // todo return by output
        if (node.doesAttributeExist(resultName)){
            val attribute =  fragment.getAttributeInstance(resultName)
            attribute.anyValue = "My first project"
            instanceService.setAttributes(attribute)
        }
        else{
            // todo changes not persistent

            // settings
            val uri = "modicio:demo.project.$resultName"

            // create attribute and add it to node
            val attribute = Attribute(0, uri, resultName, "string")
            node.addAttribute(attribute)

            // create instance and add it to object
            val instance = AttributeInstance(0, uri, output.toString())
            val o = fragment.instance?.getObjects()?.find { obj -> obj.instanceOf == node.uri }
            o!!.addAttributeInstance(instance)

            // save
            fragment.instance!!.updateHeader()
            // fragmentRepository.save(fragment)
            instanceService.setAttributes(instance)

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