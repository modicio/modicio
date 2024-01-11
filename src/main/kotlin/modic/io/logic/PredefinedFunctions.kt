package modic.io.logic

import kotlin.reflect.KFunction1
import modic.io.model.*
import java.sql.Timestamp
import java.text.SimpleDateFormat


object PredefinedFunctions {

    private val timestampFormat = SimpleDateFormat("dd.MM.yyyy HH:mm:ss")


    private fun safeParseTimestamp(timestampStr: String): Timestamp? {
        return try {
            Timestamp(timestampFormat.parse(timestampStr).time)
        } catch (e: Exception) {
            null
        }
    }

    // find a dynamic way if needed JAVA reflection.
    private val functionMap: Map<String, KFunction1<Map<String, String>, HashMap<String, String>>> = mapOf(
        "checkDeadline" to this::checkDeadline,
    )

    private fun defaultFunction(params: Map<String, String>): HashMap<String, String> {
        return hashMapOf("outputValue" to "Function not found", "outputAttribute" to "")
    }

    private fun checkDeadline(params: Map<String, String>): HashMap<String, String> {
        val deadlineString = params["deadline"]
        val endTimeString = params["endTime"]
        val parsedDeadline = deadlineString?.let { safeParseTimestamp(it) }
        val parsedEndTime = endTimeString?.let { safeParseTimestamp(it) }
        val isDeadlineCrossed = parsedEndTime?.after(parsedDeadline) == true
        return hashMapOf("outputValue" to isDeadlineCrossed.toString(), "outputAttribute" to "IsDeadLineCrossed")
    }

    fun callFunction(scrip: Script, fragment: Fragment, node: Node, instanceService: InstanceService): Any {
        val function = functionMap[scrip.name] ?: this::defaultFunction
        val args = createArgs(fragment, scrip.resolverMap())
        val functionOutput = function(args)
        val output = functionOutput["outputValue"] ?: return 400
        if (scrip.actionType == "button") {
            return output
        }
        val resultName = functionOutput["outputAttribute"] ?: return 400
        if (node.doesAttributeExist(resultName)) {
            val attribute = fragment.getAttributeInstance(resultName)
            attribute.anyValue = output
            instanceService.setAttributes(attribute)
        }
        return 200
    }

    private fun createArgs(fragment: Fragment, resolver: Map<String, String>): Map<String, String> {
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