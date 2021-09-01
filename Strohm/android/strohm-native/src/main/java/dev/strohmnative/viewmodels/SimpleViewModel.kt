package dev.strohmnative.viewmodels

import android.util.Log
import dev.strohmnative.*

open class SimpleViewModel<EntryType>(
    initialData: EntryType,
    propName: PropName,
    propPath: PropPath,
    private val instanceFactory: ConstructableFromDictionary<EntryType>
): ViewModelBase<EntryType>(initialData, propName, propPath) {
    override fun propsToData(props: Props): EntryType? {
        val m = props[this.propName] as? Map<*, *> ?: return null
        val rawData = m.asMapOfKeyType<String>() ?: return null
        var data: EntryType? = instanceFactory.createFromDict(rawData) ?: return null

        Log.d("dev.strohmnative", "Received entry: $data")
        return data
    }
}
