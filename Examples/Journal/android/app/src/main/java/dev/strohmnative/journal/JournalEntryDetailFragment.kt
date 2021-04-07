package dev.strohmnative.journal

import android.os.Bundle
import androidx.fragment.app.Fragment
import com.google.android.material.appbar.CollapsingToolbarLayout
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import dev.strohmnative.journal.databinding.Formatters
import dev.strohmnative.journal.databinding.JournalEntryDetailBinding
import dev.strohmnative.journal.dummy.DummyContent
import dev.strohmnative.journal.model.JournalEntry
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.*

/**
 * A fragment representing a single JournalEntry detail screen.
 * This fragment is either contained in a [JournalEntryListActivity]
 * in two-pane mode (on tablets) or a [JournalEntryDetailActivity]
 * on handsets.
 */
class JournalEntryDetailFragment : Fragment() {

    private lateinit var binding: JournalEntryDetailBinding
    private var item: JournalEntry? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.let { bundle ->
            if (bundle.containsKey(getString(R.string.fragment_journal_entry_detail))) {
                item = bundle.getParcelable(getString(R.string.fragment_journal_entry_detail))
                activity?.findViewById<CollapsingToolbarLayout>(R.id.toolbar_layout)?.title =
                    item?.title
                activity?.findViewById<TextView>(R.id.subtitle)?.text =
                    Formatters.formatInstant(item?.created)
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = JournalEntryDetailBinding.inflate(inflater)
        binding.journalEntry = item

        return binding.root
    }

}