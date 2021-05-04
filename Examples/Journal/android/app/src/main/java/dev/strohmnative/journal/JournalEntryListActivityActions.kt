package dev.strohmnative.journal

import android.view.View
import dev.strohmnative.journal.model.JournalEntry

interface JournalEntryListActivityActions {
    fun showJournalEntryDetails(v: View, journalEntry: JournalEntry)
}
