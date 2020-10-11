package ru.home.customvk.post

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.ItemTouchHelper.*
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import ru.home.customvk.R

class PostAdapter(
    afterUpdateAction: () -> Unit,
    private val onLikeListener: (Int) -> Post,
    private val onRemoveSwipeListener: (Int) -> Unit
) : RecyclerView.Adapter<TextPostHolder>(), SwipeHelperAdapter {

    companion object {
        private const val TYPE_TEXT_POST = 0
        private const val TYPE_IMAGE_POST = 1
    }

    private val postsDiffer = AsyncListDiffer(this, PostDiffCallback())

    init {
        postsDiffer.addListListener { _, _ -> afterUpdateAction() }
    }

    var posts: MutableList<Post> = mutableListOf()
        set(value) {
            field = value
            postsDiffer.submitList(value)
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TextPostHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.post_list_item, parent, false)
        return if (viewType == TYPE_TEXT_POST) {
            TextPostHolder(itemView, onLikeListener)
        } else {
            ImagePostHolder(itemView, onLikeListener)
        }
    }

    override fun onBindViewHolder(holder: TextPostHolder, position: Int) =
        holder.bind(posts[position])

    override fun getItemViewType(position: Int): Int =
        if (posts[position].pictureName.isBlank()) {
            TYPE_TEXT_POST
        } else {
            TYPE_IMAGE_POST
        }

    override fun getItemCount(): Int = posts.size

    override fun onItemDismiss(position: Int) {
        onRemoveSwipeListener(posts[position].id)
        posts.removeAt(position)
        notifyItemRemoved(position)
    }

    override fun onItemLike(position: Int) {
        onLikeListener(position)
        notifyItemChanged(position)
    }
}

class PostDiffCallback : DiffUtil.ItemCallback<Post>() {
    override fun areItemsTheSame(oldPost: Post, newPost: Post): Boolean = oldPost.id == newPost.id

    override fun areContentsTheSame(oldPost: Post, newPost: Post): Boolean =
        PostUtils.isVisibleContentEquals(oldPost, newPost)
}

interface SwipeHelperAdapter {
    fun onItemDismiss(position: Int)
    fun onItemLike(position: Int)
}

class ItemTouchHelperCallback(private val adapter: SwipeHelperAdapter) :
    ItemTouchHelper.SimpleCallback(UP or DOWN, START or END) {

    override fun getDragDirs(recyclerView: RecyclerView, viewHolder: ViewHolder): Int = 0

    override fun onMove(recyclerView: RecyclerView, viewHolder: ViewHolder, target: ViewHolder): Boolean = false

    override fun onSwiped(viewHolder: ViewHolder, direction: Int) {
        if (direction == START) {
            adapter.onItemDismiss(viewHolder.adapterPosition)
        } else if (direction == END) {
            adapter.onItemLike(viewHolder.adapterPosition)
        }
    }
}