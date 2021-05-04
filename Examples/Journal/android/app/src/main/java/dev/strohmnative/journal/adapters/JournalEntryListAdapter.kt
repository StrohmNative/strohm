import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import dev.strohmnative.journal.JournalEntryListActivityActions
import dev.strohmnative.journal.R
import dev.strohmnative.journal.databinding.JournalEntryListContentBinding
import dev.strohmnative.journal.model.JournalEntry

class JournalEntryListAdapter(
    private val context: Context,
    private val entries: List<JournalEntry>,
) :
    RecyclerView.Adapter<JournalEntryListAdapter.BindingHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BindingHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.journal_entry_list_content, parent, false)
        return BindingHolder(view)
    }

    override fun onBindViewHolder(holder: BindingHolder, position: Int) {
        val item = entries[position]
        holder.binding?.journalEntry = item
        holder.binding?.activityActions = context as JournalEntryListActivityActions
    }

    override fun getItemCount() = entries.size

    inner class BindingHolder(view: View) : RecyclerView.ViewHolder(view) {
        val binding: JournalEntryListContentBinding? = DataBindingUtil.bind(view)
    }
}
