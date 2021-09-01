package dev.strohmnative

import com.google.gson.Gson

typealias CommsHandlerArguments = Map<String, Any>
typealias CommsHandlerFunction = (CommsHandlerArguments) -> Unit

internal class JsonComms {
    private var registeredFunctions: MutableMap<String, CommsHandlerFunction> = mutableMapOf()

    fun encode(map: Map<String, Any>): String {
        val data = Gson().toJson(map)
        return data.replace("\"", "\\\"")
    }

    fun encode(collection: Collection<Any>): String {
        val data = Gson().toJson(collection)
        return data.replace("\"", "\\\"")
    }

    fun registerHandlerFunction(name: String, function: CommsHandlerFunction) {
        registeredFunctions[name] = function
    }

    fun jsToNative(message: String) {
        @Suppress("UNCHECKED_CAST")
        val json = Gson().fromJson(message, HashMap::class.java) as Map<String, Any>
        val functionName = json["function"] as String
        registeredFunctions[functionName]?.let { f -> f(json) }
    }
}
