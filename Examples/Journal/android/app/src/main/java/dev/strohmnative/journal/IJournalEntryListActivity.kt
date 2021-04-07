package dev.strohmnative.journal

import dev.strohmnative.journal.model.JournalEntry

interface IJournalEntryListActivity {
    fun inflateJournalEntryDetailFragment(journalEntry: JournalEntry)
}