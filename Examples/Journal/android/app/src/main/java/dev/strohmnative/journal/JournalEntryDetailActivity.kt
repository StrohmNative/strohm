package dev.strohmnative.journal

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.WindowManager
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import dev.strohmnative.journal.databinding.ActivityJournalEntryDetailBinding
import dev.strohmnative.journal.model.JournalEntry
import dev.strohmnative.journal.viewmodel.JournalEntryDetailViewModel
import dev.strohmnative.strohm.Strohm

class JournalEntryDetailActivity : AppCompatActivity() {
    lateinit var binding: ActivityJournalEntryDetailBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_journal_entry_detail)

        setSupportActionBar(binding.detailToolbar)

        binding.fab.setOnClickListener { _ ->
            val dialogView = layoutInflater.inflate(R.layout.change_title_dialog, null)
            val editField = dialogView.findViewById<EditText>(R.id.editTitle)
            binding.viewModel?.data?.value?.title?.let { editField.setText(it) }

            val alert = AlertDialog.Builder(this)
            alert.setView(dialogView)
            alert.setTitle("Change Title")

            alert.setPositiveButton("Update") { _, _ ->
                binding.viewModel?.data?.let { data ->
                    data.value = data.value?.let { entry ->
                        entry.title = editField.text.toString()
                        Strohm.getInstance().dispatch("update-entry", entry)
                        entry
                    }
                }
            }
            alert.setNegativeButton("Cancel") { _, _ -> }
            val d = alert.create()
            editField.requestFocus()
            d.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE)
            editField.setSelection(editField.text.length)
            d.show()
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
