import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import dev.strohmnative.journal.IJournalEntryListActivity
import dev.strohmnative.journal.R
import dev.strohmnative.journal.databinding.JournalEntryListContentBinding
import dev.strohmnative.journal.model.JournalEntry
import dev.strohmnative.journal.viewmodel.JournalEntryListViewModel

class JournalEntryListAdapter(
//    private val parentActivity: JournalEntryListActivity,
    private val context: Context,
    private val entries: List<JournalEntry>,
    private val twoPane: Boolean
) :
    RecyclerView.Adapter<JournalEntryListAdapter.BindingHolder>() {

//    private val onClickListener: View.OnClickListener

//    init {
//        onClickListener = View.OnClickListener { v ->
//            val item = v.tag as JournalEntry
//            if (twoPane) {
//                val fragment = JournalEntryDetailFragment().apply {
//                    arguments = Bundle().apply {
//                        putString(JournalEntryDetailFragment.ARG_ITEM_ID, item.id)
//                    }
//                }
//                parentActivity.supportFragmentManager
//                    .beginTransaction()
//                    .replace(R.id.journal_entry_detail_container, fragment)
//                    .commit()
//            } else {
//                val intent = Intent(v.context, JournalEntryDetailActivity::class.java).apply {
//                    putExtra(JournalEntryDetailFragment.ARG_ITEM_ID, item.id)
//                }
//                v.context.startActivity(intent)
//            }
//        }
//    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BindingHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.journal_entry_list_content, parent, false)
        return BindingHolder(view)
    }

    override fun onBindViewHolder(holder: BindingHolder, position: Int) {
        val item = entries[position]
//        holder.contentView.text = item.title
//        holder.creationDateView.text = dateFormatter.format(item.created)
//
//        with(holder.itemView) {
//            tag = item
//            setOnClickListener(onClickListener)
//        }
        holder.binding?.journalEntry = item
        holder.binding?.iJournalEntryListActivity = context as IJournalEntryListActivity
    }

    override fun getItemCount() = entries.size

    inner class BindingHolder(view: View) : RecyclerView.ViewHolder(view) {
        val binding: JournalEntryListContentBinding? = DataBindingUtil.bind(view)
    }
}