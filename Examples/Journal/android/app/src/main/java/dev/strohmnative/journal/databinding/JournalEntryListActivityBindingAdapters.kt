package dev.strohmnative.journal.databinding

import JournalEntryListAdapter
import androidx.databinding.BindingAdapter
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import dev.strohmnative.journal.model.JournalEntry
import dev.strohmnative.journal.viewmodel.JournalEntryListViewModel

@BindingAdapter("journalEntries")
fun RecyclerView.setJournalEntries(entries: List<JournalEntry>) {
    val layoutManager = this.layoutManager
    if (layoutManager == null) {
        this.layoutManager = LinearLayoutManager(this.context)
    }
    val twoPane = false // TODO: determine from layout
    this.adapter = JournalEntryListAdapter(this.context, entries, twoPane)
}