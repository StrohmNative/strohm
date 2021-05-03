package dev.strohmnative.strohm.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import dev.strohmnative.strohm.*

open class KeyedArrayViewModel<EntryType>(
    initialData: List<EntryType>,
    propName: PropName,
    propPath: PropPath,
    private val instanceFactory: ConstructableFromDictionary<EntryType>
) : ViewModelBase<List<EntryType>>(initialData, propName, propPath) {
    var sorter: Comparator<EntryType>? = null
    var entries: LiveData<List<EntryType>> = Transformations.map(data) {
        data -> data as List<EntryType>
    }

    override fun propsToData(props: Props): List<EntryType>? {
        val m = props[this.propName] as? Map<*, *> ?: return null
        val rawData = m.asMapOfType<PropName, Props>() ?: return null
        var data = rawData.values.mapNotNull(instanceFactory::createFromDict)
        return sorter?.let { data.sortedWith(it) } ?: data
    }
}
