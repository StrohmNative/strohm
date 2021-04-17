package dev.strohmnative.journal.viewmodel

import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.databinding.BaseObservable
import androidx.databinding.Bindable
import dev.strohmnative.journal.BR
import dev.strohmnative.journal.model.JournalEntry
import dev.strohmnative.strohm.PropName
import dev.strohmnative.strohm.PropPath
import dev.strohmnative.strohm.Props
import dev.strohmnative.strohm.Strohm
import java.util.*


class JournalEntryListViewModel: BaseObservable {
    @Bindable
    var entries: List<JournalEntry>

    private var propName: PropName
    private var propPath: PropPath
    private var subscriptionId: UUID? = null
    private val handler = Handler(Looper.getMainLooper())

    constructor(entries: List<JournalEntry>) {
        this.entries = entries
        this.propName = "entries"
        this.propPath = listOf("entries")
        Strohm.getInstance().subscribe(mapOf(propName to propPath), ::receiveProps) {
            subscriptionId -> this.subscriptionId = subscriptionId
        }
    }

    fun receiveProps(props: Props): Unit {
        propsToData(props)?.let { data ->
            handler.post(Runnable {
                // TODO: trigger observable?
                this.entries = data
//                notifyChange()
                notifyPropertyChanged(BR.entries)
            })
        }
    }

    fun propsToData(props: Props): List<JournalEntry>? {
        val m = props[this.propName] as? Map<*, *>
        if (m == null) return null

        val rawData = m.asMapOfType<PropName, Props>()
        Log.d("!!!!!!", "Received props: ${rawData}")
        if (rawData == null) return null

        val data = rawData.values.mapNotNull(JournalEntry::createFromDict)
        Log.d("!!!!!!", "Received entries: $data")
        return data
    }

}

inline fun <reified K, reified V> Map<*, *>.asMapOfType(): Map<K, V>? =
    if (all { it.key is K && it.value is V })
        @Suppress("UNCHECKED_CAST")
        this as Map<K, V> else
        null
