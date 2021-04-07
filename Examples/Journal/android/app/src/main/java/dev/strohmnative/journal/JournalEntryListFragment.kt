package dev.strohmnative.journal

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import dev.strohmnative.journal.databinding.JournalEntryListBinding
import dev.strohmnative.journal.dummy.DummyContent

class JournalEntryListFragment: Fragment() {

    lateinit var binding: JournalEntryListBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = JournalEntryListBinding.inflate(inflater)
        binding.journalEntries = DummyContent.ITEMS
        return binding.root
    }
}