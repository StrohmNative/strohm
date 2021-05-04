package dev.strohmnative.journal

import android.content.Intent
import android.os.Bundle
import android.util.Log
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity
import android.view.MenuItem
import androidx.databinding.DataBindingUtil
import dev.strohmnative.journal.databinding.ActivityJournalEntryDetailBinding
import dev.strohmnative.journal.model.JournalEntry
import dev.strohmnative.journal.viewmodel.JournalEntryDetailViewModel

/**
 * An activity representing a single JournalEntry detail screen. This
 * activity is only used on narrow width devices. On tablet-size devices,
 * item details are presented side-by-side with a list of items
 * in a [JournalEntryListActivity].
 */
class JournalEntryDetailActivity : AppCompatActivity() {

    lateinit var binding: ActivityJournalEntryDetailBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_journal_entry_detail)

        setSupportActionBar(binding.detailToolbar)

        binding.fab.setOnClickListener { view ->
            Snackbar.make(view, "Replace with your own detail action", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show()
        }

        // Show the Up button in the action bar.
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val item: JournalEntry? = intent.getParcelableExtra(getString(R.string.fragment_journal_entry_detail))
        init(item)
        item?.let { binding.viewModel = JournalEntryDetailViewModel(it) }
        binding.lifecycleOwner = this
    }

    private fun init(item: JournalEntry?) {
        val fragment = JournalEntryDetailFragment()
        item?.let {
            val bundle = Bundle()
            bundle.putParcelable(getString(R.string.fragment_journal_entry_detail), it)
            fragment.arguments = bundle
        }
        val tx = supportFragmentManager.beginTransaction()
        tx.replace(R.id.journal_entry_detail_container, fragment, getString(R.string.fragment_journal_entry_detail))
        tx.commit()
    }

    override fun onOptionsItemSelected(item: MenuItem) =
        when (item.itemId) {
            android.R.id.home -> {
                // This ID represents the Home or Up button. In the case of this
                // activity, the Up button is shown. For
                // more details, see the Navigation pattern on Android Design:
                //
                // http://developer.android.com/design/patterns/navigation.html#up-vs-back

                navigateUpTo(Intent(this, JournalEntryListActivity::class.java))
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
}
