package dev.strohmnative.journal

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import dev.strohmnative.journal.databinding.JournalEntryListBinding
import dev.strohmnative.journal.view.RecyclerItemTouchHelper
import dev.strohmnative.journal.view.RecyclerItemTouchHelperListener
import dev.strohmnative.journal.viewmodel.JournalEntryListViewModel
import dev.strohmnative.strohm.Strohm

class JournalEntryListFragment: Fragment(), RecyclerItemTouchHelperListener {

    lateinit var binding: JournalEntryListBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = JournalEntryListBinding.inflate(inflater)
        binding.viewModel = JournalEntryListViewModel(listOf())
        binding.lifecycleOwner = this

        val itemTouchHelperCallback: ItemTouchHelper.SimpleCallback = RecyclerItemTouchHelper(0, ItemTouchHelper.LEFT, this)
        ItemTouchHelper(itemTouchHelperCallback).attachToRecyclerView(binding.journalEntryList)

        return binding.root
    }

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder?, direction: Int, position: Int) {
        viewHolder?.adapterPosition?.let { index ->
            binding.viewModel?.entries?.value?.get(index)
        }?.let { entry ->
            Strohm.getInstance().dispatch("remove-entry", mapOf("entry/id" to entry.id))
        }
    }
}
