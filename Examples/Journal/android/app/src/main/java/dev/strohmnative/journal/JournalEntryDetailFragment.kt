package dev.strohmnative.journal

import android.os.Bundle
import androidx.fragment.app.Fragment
import com.google.android.material.appbar.CollapsingToolbarLayout
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowInsets
import android.widget.TextView
import androidx.core.view.updatePadding
import dev.strohmnative.journal.databinding.Formatters
import dev.strohmnative.journal.databinding.JournalEntryDetailBinding
import dev.strohmnative.journal.model.JournalEntry
import dev.strohmnative.journal.viewmodel.JournalEntryDetailViewModel
import dev.strohmnative.strohm.Strohm

/**
 * A fragment representing a single JournalEntry detail screen.
 * This fragment is either contained in a [JournalEntryListActivity]
 * in two-pane mode (on tablets) or a [JournalEntryDetailActivity]
 * on handsets.
 */
class JournalEntryDetailFragment : Fragment() {

    private lateinit var binding: JournalEntryDetailBinding
    private lateinit var item: JournalEntry

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.let { bundle ->
            if (bundle.containsKey(getString(R.string.fragment_journal_entry_detail))) {
                item = bundle.getParcelable(getString(R.string.fragment_journal_entry_detail))!!
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = JournalEntryDetailBinding.inflate(inflater)
        binding.viewModel = JournalEntryDetailViewModel(item)

        binding.journalEntryDetail.setOnApplyWindowInsetsListener { view, insets ->
            view.updatePadding(bottom = insets.systemWindowInsetBottom)
            insets
        }

        binding.lifecycleOwner = this

        return binding.root
    }

    override fun onPause() {
        binding.viewModel?.data?.value?.let { entry ->
            Strohm.getInstance().dispatch("update-entry", entry)
        }
        super.onPause()
    }
}
