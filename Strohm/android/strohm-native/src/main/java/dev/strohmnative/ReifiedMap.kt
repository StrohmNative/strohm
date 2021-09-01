package dev.strohmnative

inline fun <reified K, reified V> Map<*, *>.asMapOfType(): Map<K, V>? =
    if (all { it.key is K && it.value is V })
        @Suppress("UNCHECKED_CAST")
        this as Map<K, V> else
        null

inline fun <reified K> Map<*, *>.asMapOfKeyType(): Map<K, Any>? =
    if (all { it.key is K })
        @Suppress("UNCHECKED_CAST")
        this as Map<K, Any> else
        null
