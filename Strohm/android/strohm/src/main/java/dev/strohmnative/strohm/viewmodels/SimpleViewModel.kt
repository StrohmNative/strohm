package dev.strohmnative.strohm.viewmodels

import android.util.Log
import dev.strohmnative.strohm.*

open class SimpleViewModel<EntryType>(
    initialData: EntryType,
    propName: PropName,
    propPath: PropPath,
    private val instanceFactory: ConstructableFromDictionary<EntryType>
): ViewModelBase<EntryType>(initialData, propName, propPath) {
    override fun propsToData(props: Props): EntryType? {
        val m = props[this.propName] as? Map<*, *> ?: return null
        val rawData = m.asMapOfType<PropName, Props>() ?: return null
        var data: EntryType? = instanceFactory.createFromDict(rawData) ?: return null

        Log.d("dev.strohmnative.strohm", "Received entry: $data")
        return data
    }
}