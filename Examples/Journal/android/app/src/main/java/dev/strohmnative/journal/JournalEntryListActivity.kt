package dev.strohmnative.journal

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import dev.strohmnative.journal.databinding.ActivityJournalEntryListBinding
import dev.strohmnative.journal.model.JournalEntry
import dev.strohmnative.strohm.Strohm

class JournalEntryListActivity : AppCompatActivity(), JournalEntryListActivityActions {
    private lateinit var binding: ActivityJournalEntryListBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_journal_entry_list)

        setSupportActionBar(binding.toolbar)
        binding.toolbar.title = title

        binding.fab.setOnClickListener {
            Strohm.getInstance().dispatch("new-entry")
        }

        init()
    }

    private fun init() {
        val fragment = JournalEntryListFragment()
        val tx = supportFragmentManager.beginTransaction()
        tx.replace(R.id.main_container, fragment, getString(R.string.fragment_journal_entry_list))
        tx.commit()
    }

    override fun showJournalEntryDetails(v: View, journalEntry: JournalEntry) {
        val intent = Intent(v.context, JournalEntryDetailActivity::class.java).apply {
            putExtra(getString(R.string.fragment_journal_entry_detail), journalEntry)
        }
        v.context.startActivity(intent)
    }
}
