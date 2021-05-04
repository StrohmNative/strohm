package dev.strohmnative.strohm

interface ConstructableFromDictionary<out T> {
    fun createFromDict(dict: Map<String, Any>): T?
}

interface ConvertableToDictionary {
    fun toDict(): Map<String, Any>
}
