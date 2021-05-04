package dev.strohmnative.journal.viewmodel

import dev.strohmnative.journal.model.JournalEntry
import dev.strohmnative.strohm.viewmodels.KeyedArrayViewModel

class JournalEntryListViewModel(
    entries: List<JournalEntry>
) : KeyedArrayViewModel<JournalEntry>(entries, "entries", listOf("entries"), JournalEntry) {
    init {
        this.sorter = Comparator { e1, e2 ->
            - e1.created.compareTo(e2.created)
        }
    }
}
