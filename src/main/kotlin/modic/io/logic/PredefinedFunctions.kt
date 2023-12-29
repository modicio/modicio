package modic.io.logic
import modic.io.model.Fragment
import modic.io.model.Script

object PredefinedFunctions {

    private val functionMap: Map<String, (Map<String, Any>, Fragment) -> Unit> = mapOf(
        "exampleFunction" to this::exampleFunction,
        "example2Function" to this::example2Function
    )
    private fun exampleFunction(params: Map<String, Any>, fragment: Fragment) {
        println("this is a exampleFunction")
    }
    private fun example2Function(params: Map<String, Any>, fragment: Fragment) {
        println("this is a example2Function")
    }

    private fun defaultFunction(params: Map<String, Any>, fragment: Fragment) {
        println("Function not found")
    }

    fun callFunction(scrip: Script, fragment: Fragment) {
        val function = functionMap[scrip.name] ?: this::defaultFunction
        function(scrip.resolverMap(), fragment)
    }
}