package dev.strohmnative.viewmodels

import android.util.Log
import dev.strohmnative.*

open class SimpleViewModel<EntryType>(
    initialData: EntryType,
    propName: PropName,
    propPath: PropPath,
    private val instanceFactory: ConstructableFromDictionary<EntryType>
): ViewModelBase<EntryType>(initialData, propName, propPath) {
    override fun propToData(prop: Prop): EntryType? {
      if (prop.first != propName) { return null; }

      val m = prop.second as? Map<*, *> ?: return null
        val rawData = m.asMapOfKeyType<String>() ?: return null
        var data: EntryType? = instanceFactory.createFromDict(rawData) ?: return null

        Log.d("dev.strohmnative", "Received entry: $data")
        return data
    }
}
