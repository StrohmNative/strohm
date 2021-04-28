package dev.strohmnative.strohm

inline fun <reified K, reified V> Map<*, *>.asMapOfType(): Map<K, V>? =
    if (all { it.key is K && it.value is V })
        @Suppress("UNCHECKED_CAST")
        this as Map<K, V> else
        null

