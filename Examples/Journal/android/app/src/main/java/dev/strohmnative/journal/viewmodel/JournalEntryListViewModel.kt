package dev.strohmnative.journal.viewmodel

import dev.strohmnative.journal.model.JournalEntry
import dev.strohmnative.strohm.viewmodels.KeyedArrayViewModel


class JournalEntryListViewModel: KeyedArrayViewModel<JournalEntry> {

    constructor(entries: List<JournalEntry>)
            : super(entries, "entries", listOf("entries"), JournalEntry) {
        this.sorter = Comparator<JournalEntry>{ e1, e2 ->
            - e1.created.compareTo(e2.created)
        }
    }
}
