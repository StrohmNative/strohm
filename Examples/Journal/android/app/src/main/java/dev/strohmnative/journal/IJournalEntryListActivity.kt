package dev.strohmnative.journal

import android.view.View
import dev.strohmnative.journal.model.JournalEntry

interface IJournalEntryListActivity {
    fun inflateJournalEntryDetailFragment(v: View, journalEntry: JournalEntry)
}