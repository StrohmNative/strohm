package dev.strohmnative.journal.databinding

import JournalEntryListAdapter
import androidx.databinding.BindingAdapter
import androidx.recyclerview.widget.RecyclerView
import dev.strohmnative.journal.viewmodel.JournalEntryListViewModel

@BindingAdapter("viewModel")
fun RecyclerView.setViewModel(viewModel: JournalEntryListViewModel) {
    val twoPane = false // TODO: determine from layout
    this.adapter = JournalEntryListAdapter(this.context, viewModel, twoPane)
}