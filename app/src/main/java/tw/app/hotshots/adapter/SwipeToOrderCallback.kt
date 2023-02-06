package tw.app.hotshots.adapter

import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView

class SwipeToOrderCallback(dragDirs: Int, swipeDirs: Int) : ItemTouchHelper.SimpleCallback(
    dragDirs,
    swipeDirs
) {
    override fun onMove(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        target: RecyclerView.ViewHolder
    ): Boolean {
        recyclerView.adapter?.notifyItemMoved(
            viewHolder.bindingAdapterPosition,
            target.bindingAdapterPosition
        )
        return true
    }

    override fun isItemViewSwipeEnabled(): Boolean {
        return false
    }


    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {

    }
}