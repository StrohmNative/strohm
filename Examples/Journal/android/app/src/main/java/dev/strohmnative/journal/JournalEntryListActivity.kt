package dev.strohmnative.journal

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.NestedScrollView
import androidx.databinding.DataBindingUtil
import com.google.android.material.snackbar.Snackbar
import dev.strohmnative.journal.databinding.ActivityJournalEntryListBinding
import dev.strohmnative.journal.databinding.JournalEntryListBinding
import dev.strohmnative.journal.model.JournalEntry

/**
 * An activity representing a list of Pings. This activity
 * has different presentations for handset and tablet-size devices. On
 * handsets, the activity presents a list of items, which when touched,
 * lead to a [JournalEntryDetailActivity] representing
 * item details. On tablets, the activity presents the list of items and
 * item details side-by-side using two vertical panes.
 */
class JournalEntryListActivity : AppCompatActivity(), IJournalEntryListActivity {

    /**
     * Whether or not the activity is in two-pane mode, i.e. running on a tablet
     * device.
     */
    private var twoPane: Boolean = false
    private lateinit var binding: ActivityJournalEntryListBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_journal_entry_list)

        setSupportActionBar(binding.toolbar)
        binding.toolbar.title = title

        binding.fab.setOnClickListener { view ->
            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show()
        }

        if (findViewById<NestedScrollView>(R.id.journal_entry_detail_container) != null) {
            // The detail container view will be present only in the
            // large-screen layouts (res/values-w900dp).
            // If this view is present, then the
            // activity should be in two-pane mode.
            twoPane = true
        }

        init()
    }

    private fun init() {
        val fragment = JournalEntryListFragment()
        val tx = supportFragmentManager.beginTransaction()
        tx.replace(R.id.main_container, fragment, getString(R.string.fragment_journal_entry_list))
        tx.commit()
    }

    override fun inflateJournalEntryDetailFragment(journalEntry: JournalEntry) {
        val fragment = JournalEntryDetailFragment()

        val bundle = Bundle()
        bundle.putParcelable(getString(R.string.intent_journal_entry), journalEntry)
        fragment.arguments = bundle

        val tx = supportFragmentManager.beginTransaction()
        tx.replace(R.id.main_container, fragment, getString(R.string.fragment_journal_entry_detail))
        tx.addToBackStack(getString(R.string.fragment_journal_entry_detail))
        tx.commit()
    }
}