package dev.strohmnative.journal.viewmodel

import android.os.Handler
import android.os.Looper
import androidx.databinding.BaseObservable
import androidx.databinding.Bindable
import androidx.databinding.Observable
import dev.strohmnative.journal.BR
import dev.strohmnative.journal.model.JournalEntry
import dev.strohmnative.strohm.PropName
import dev.strohmnative.strohm.PropPath
import dev.strohmnative.strohm.Props
import dev.strohmnative.strohm.Strohm
import java.util.*
import kotlin.Comparator


class JournalEntryListViewModel: KeyedArrayViewModel<JournalEntry> {

    constructor(entries: List<JournalEntry>)
            : super(entries, "entries", listOf("entries"), JournalEntry) {
        this.sorter = Comparator<JournalEntry>{ e1, e2 ->
            - e1.created.compareTo(e2.created)
        }
    }
}

inline fun <reified K, reified V> Map<*, *>.asMapOfType(): Map<K, V>? =
    if (all { it.key is K && it.value is V })
        @Suppress("UNCHECKED_CAST")
        this as Map<K, V> else
        null

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

abstract class ViewModelBase<DataType>(
    initialData: DataType,
    propName: PropName,
    propPath: PropPath
) : BaseObservable() { // TODO: use LiveData
    private var subscriptionId: UUID? = null
    val propName: PropName
    val propPath: PropPath
    @Bindable var data: DataType = initialData

    private val handler = Handler(Looper.getMainLooper())

    init {
        this.propName = "entries"
        this.propPath = listOf("entries")
        Strohm.getInstance().subscribe(mapOf(propName to propPath), ::receiveProps) {
                subscriptionId -> this.subscriptionId = subscriptionId
        }
    }

    fun receiveProps(props: Props): Unit {
        propsToData(props)?.let { data ->
            handler.post(Runnable {
                this.data = data
                notifyPropertyChanged(BR.data)
            })
        }
    }

    abstract fun propsToData(props: Props): DataType?
}

interface ConstructableFromDictionary<out T> {
    fun createFromDict(dict: Map<String, Any>): T?
}