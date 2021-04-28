package dev.strohmnative.strohm.viewmodels

import androidx.databinding.Bindable
import dev.strohmnative.strohm.*
import androidx.databinding.library.baseAdapters.BR

open class KeyedArrayViewModel<EntryType>(
    initialData: List<EntryType>,
    propName: PropName,
    propPath: PropPath,
    private val instanceFactory: ConstructableFromDictionary<EntryType>
) : ViewModelBase<List<EntryType>>(initialData, propName, propPath) {
    var sorter: Comparator<EntryType>? = null
    var entries: List<EntryType>
        @Bindable get() = this.data
        set(value) { this.data = value }

    override fun propsToData(props: Props): List<EntryType>? {
        val m = props[this.propName] as? Map<*, *> ?: return null
        val rawData = m.asMapOfType<PropName, Props>() ?: return null
        var data = rawData.values.mapNotNull(instanceFactory::createFromDict)
        return sorter?.let { data.sortedWith(it) } ?: data
    }

    override fun notifyPropertyChanged(fieldId: Int) {
        super.notifyPropertyChanged(fieldId)
        if (fieldId == BR.data) {
            super.notifyPropertyChanged(BR.entries)
        }
    }
}
