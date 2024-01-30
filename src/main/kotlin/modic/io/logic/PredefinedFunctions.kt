package modic.io.logic

import modic.io.model.*
import java.sql.Timestamp
import java.text.SimpleDateFormat
import kotlin.reflect.KFunction1
import kotlin.reflect.full.memberFunctions
import kotlin.reflect.jvm.jvmErasure

object PredefinedFunctions {

    private val timestampFormat = SimpleDateFormat("dd.MM.yyyy HH:mm:ss")
    private val eventListeners = arrayListOf<String>() // todo call these functions with a Listener.

    // match "name of function" to actual function of all the functions using Java Reflection
    private val functionMap: Map<String, KFunction1<Map<String, String>, String>> by lazy {
        this::class.memberFunctions
            .filter { it.parameters.size == 2 }
            .filter { it.parameters[1].type.jvmErasure == Map::class }
            .filter { it.returnType.jvmErasure == String::class }
            .mapNotNull { function ->
                val castedFunction = function as? KFunction1<Map<String, String>, String>
                castedFunction?.let { function.name to it }
            }
            .toMap()
    }

    private fun safeParseTimestamp(timestampStr: String): Timestamp? {
        return try {
            Timestamp(timestampFormat.parse(timestampStr).time)
        } catch (e: Exception) {
            null
        }
    }

    private fun defaultFunction(params: Map<String, String>): String {
        return "Function not found"
    }

    internal fun checkDeadline(params: Map<String, String>): String {
        val deadlineString = params["deadline"]
        val endTimeString = params["endTime"]
        val parsedDeadline = deadlineString?.let { safeParseTimestamp(it) }
        val parsedEndTime = endTimeString?.let { safeParseTimestamp(it) }
        return (parsedEndTime?.after(parsedDeadline) == true).toString()
    }

    internal fun resetInt(params: Map<String, String>): String {
        return "0"
    }

    internal fun calculateRemainingHours(params: Map<String, String>): String {
        val hoursWorked = params["hoursWorked"]?.toIntOrNull()
        val totalHours = params["totalHours"]?.toIntOrNull()
        return when {
            hoursWorked == null || totalHours == null -> "Invalid input"
            hoursWorked > totalHours -> "0"
            else -> (totalHours - hoursWorked).toString()
        }
    }

    fun callFunction(script: Script, fragment: Fragment, instanceService: InstanceService?): Unit {
        val function = functionMap[script.name] ?: this::defaultFunction
        val args = createArgs(fragment, script.resolverMap())
        val functionOutput = function.call(this, args)
        if (script.actionType == "RealTimeUpdater") {
            eventListeners.add(script.name)
        }
        val outputAttributeName = script.anyValue

        // Set attribute
        val attribute = fragment.getAttributeInstance(outputAttributeName)
        attribute.anyValue = functionOutput
        instanceService?.setAttributes(attribute)
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