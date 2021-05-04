package dev.strohmnative.journal.view

import android.graphics.Canvas
import android.widget.RelativeLayout
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView

class RecyclerItemTouchHelper(
    dragDirs: Int,
    swipeDirs: Int,
    private val listener: RecyclerItemTouchHelperListener
) : ItemTouchHelper.SimpleCallback(dragDirs, swipeDirs) {
    override fun onMove(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        target: RecyclerView.ViewHolder
    ): Boolean {
        return true
    }

    override fun onSelectedChanged(viewHolder: RecyclerView.ViewHolder?, actionState: Int) {
        withForegroundView(viewHolder) { foregroundView ->
            getDefaultUIUtil().onSelected(foregroundView)
        }
    }

    override fun onChildDrawOver(
        c: Canvas,
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder?,
        dX: Float,
        dY: Float,
        actionState: Int,
        isCurrentlyActive: Boolean
    ) {
        withForegroundView(viewHolder) { foregroundView ->
            getDefaultUIUtil().onDrawOver(
                c, recyclerView, foregroundView, dX, dY, actionState, isCurrentlyActive
            )
        }
    }

    override fun clearView(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder) {
        withForegroundView(viewHolder) {
            getDefaultUIUtil().clearView(it)
        }
    }

    override fun onChildDraw(
        c: Canvas,
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        dX: Float,
        dY: Float,
        actionState: Int,
        isCurrentlyActive: Boolean
    ) {
        withForegroundView(viewHolder) {
            getDefaultUIUtil().onDraw(c, recyclerView, it, dX, dY, actionState, isCurrentlyActive)
        }
    }

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
        listener.onSwiped(viewHolder, direction, viewHolder.adapterPosition)
    }

    private fun withForegroundView(
        viewHolder: RecyclerView.ViewHolder?,
        block: (RelativeLayout) -> Unit
    ) {
        (viewHolder as? JournalEntryListAdapter.BindingHolder)?.let {
            it.binding?.viewForeground?.let(block)
        }
    }
}

interface RecyclerItemTouchHelperListener {
    fun onSwiped(viewHolder: RecyclerView.ViewHolder?, direction: Int, position: Int)
}
