package dev.strohmnative.journal.viewmodel

import dev.strohmnative.journal.model.JournalEntry
import dev.strohmnative.strohm.viewmodels.SimpleViewModel

class JournalEntryDetailViewModel(
    initialData: JournalEntry
) : SimpleViewModel<JournalEntry>(
    initialData, "entry",
    listOf("entries", initialData.id),
    JournalEntry) {
}
