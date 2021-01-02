package dev.strohmNative

import org.json.JSONObject

internal class JsonComms {
    fun encode(map: Map<String, Any>): String {
        val data = JSONObject(map).toString()
        return data.replace("\"", "\\\"")
    }
}
