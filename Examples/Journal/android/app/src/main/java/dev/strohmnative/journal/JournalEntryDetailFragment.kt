package dev.strohmnative.journal

import android.os.Bundle
import androidx.fragment.app.Fragment
import com.google.android.material.appbar.CollapsingToolbarLayout
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
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

    /**
     * The dummy content this fragment is presenting.
     */
    private var item: JournalEntry? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.let {
            if (it.containsKey(ARG_ITEM_ID)) {
                // Load the dummy content specified by the fragment
                // arguments. In a real-world scenario, use a Loader
                // to load content from a content provider.
                item = DummyContent.ITEM_MAP[it.getString(ARG_ITEM_ID)]
                activity?.findViewById<CollapsingToolbarLayout>(R.id.toolbar_layout)?.title =
                    item?.title
                activity?.findViewById<TextView>(R.id.subtitle)?.text =
                    dateFormatter.format(item?.created)
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.journal_entry_detail, container, false)

        // Show the dummy content as text in a TextView.
        item?.let {
            rootView.findViewById<TextView>(R.id.journal_entry_detail).text = it.text
        }

        return rootView
    }

    companion object {
        /**
         * The fragment argument representing the item ID that this fragment
         * represents.
         */
        const val ARG_ITEM_ID = "item_id"

        val dateFormatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM)
            .withLocale(Locale.getDefault())
            .withZone(ZoneId.systemDefault())
    }
}