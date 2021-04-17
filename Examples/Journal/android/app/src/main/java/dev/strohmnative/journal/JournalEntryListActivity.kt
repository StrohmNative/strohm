package dev.strohmnative.journal

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.NestedScrollView
import androidx.databinding.DataBindingUtil
import dev.strohmnative.journal.databinding.ActivityJournalEntryListBinding
import dev.strohmnative.journal.model.JournalEntry
import dev.strohmnative.strohm.Strohm

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

        binding.fab.setOnClickListener { _ ->
            Strohm.getInstance().dispatch("new-entry")
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

    override fun inflateJournalEntryDetailFragment(v: View, journalEntry: JournalEntry) {
        if (twoPane) {
            val fragment = JournalEntryDetailFragment().apply {
                val bundle = Bundle()
                bundle.putParcelable(
                    getString(R.string.fragment_journal_entry_detail),
                    journalEntry
                )
            }
            supportFragmentManager
                .beginTransaction()
                .replace(R.id.journal_entry_detail_container, fragment)
                .commit()
        } else {
            val intent = Intent(v.context, JournalEntryDetailActivity::class.java).apply {
                putExtra(getString(R.string.fragment_journal_entry_detail), journalEntry)
            }
            v.context.startActivity(intent)
        }
    }
}