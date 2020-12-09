package ru.home.customvk.presentation.posts_screen.adapter

import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView

class PostTouchHelperCallback(private val adapter: SwipeHelperAdapter) :
    ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.START or ItemTouchHelper.END) {

    override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean = false

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
        if (direction == ItemTouchHelper.START) {
            adapter.onItemDismiss(viewHolder.adapterPosition)
        } else if (direction == ItemTouchHelper.END) {
            adapter.onItemLike(viewHolder.adapterPosition)
        }
    }

    interface SwipeHelperAdapter {
        fun onItemDismiss(position: Int)
        fun onItemLike(position: Int)
    }
}
