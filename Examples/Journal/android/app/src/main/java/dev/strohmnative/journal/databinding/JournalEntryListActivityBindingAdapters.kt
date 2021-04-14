package dev.strohmnative.journal.databinding

import JournalEntryListAdapter
import android.view.View
import androidx.databinding.BindingAdapter
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import dev.strohmnative.journal.viewmodel.JournalEntryListViewModel

val NUM_COLUMNS = 2

@BindingAdapter("journalEntryList")
fun RecyclerView.setJournalEntryList(journalEntries: List<JournalEntry>) {
    if (this.layoutManager == null) {
        this.layoutManager = GridLayoutManager(this.context, NUM_COLUMNS)
    }
    if (this.adapter == null) {
        val twoPane = false // TODO: determine from layout
        this.adapter =
            JournalEntryListAdapter(this.context, journalEntries, twoPane)
    }
}
@BindingAdapter("viewModel")
fun RecyclerView.setViewModel(viewModel: JournalEntryListViewModel) {
    val twoPane = false // TODO: determine from layout
    this.adapter = JournalEntryListAdapter(this.context, viewModel, twoPane)
}