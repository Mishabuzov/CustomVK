package ru.home.customvk.screens.posts_screen

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.ItemTouchHelper.END
import androidx.recyclerview.widget.ItemTouchHelper.START
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import ru.home.customvk.R
import ru.home.customvk.models.local.Post

class PostAdapter(
    private val onLikeListener: (Int) -> Unit,
    private val onRemoveSwipeListener: (Int) -> Unit
) : RecyclerView.Adapter<TextPostHolder>(), PostTouchHelperCallback.SwipeHelperAdapter {

    companion object {
        private const val TYPE_TEXT_POST = 0
        private const val TYPE_IMAGE_POST = 1
    }

    private val postsDiffer = AsyncListDiffer(this, PostDiffCallback())

    var posts: List<Post> = emptyList()
        set(value) {
            field = value
            postsDiffer.submitList(value)
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TextPostHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.post_list_item, parent, false)
        return if (viewType == TYPE_TEXT_POST) {
            TextPostHolder(itemView) { position -> onItemLike(position) }
        } else {
            ImagePostHolder(itemView) { position -> onItemLike(position) }
        }
    }

    override fun onBindViewHolder(holder: TextPostHolder, position: Int) = holder.bind(posts[position])

    override fun getItemViewType(position: Int): Int =
        if (posts[position].pictureUrl.isBlank()) {
            TYPE_TEXT_POST
        } else {
            TYPE_IMAGE_POST
        }

    override fun getItemCount(): Int = posts.size

    override fun onItemDismiss(position: Int) = onRemoveSwipeListener(position)

    override fun onItemLike(position: Int) = onLikeListener(position)

    class PostDiffCallback : DiffUtil.ItemCallback<Post>() {
        override fun areItemsTheSame(oldPost: Post, newPost: Post): Boolean = oldPost.postId == newPost.postId
                && oldPost.source.sourceId == newPost.source.sourceId

        override fun areContentsTheSame(oldPost: Post, newPost: Post): Boolean = oldPost == newPost
    }
}

class PostTouchHelperCallback(private val adapter: SwipeHelperAdapter) :
    ItemTouchHelper.SimpleCallback(0, START or END) {

    override fun onMove(recyclerView: RecyclerView, viewHolder: ViewHolder, target: ViewHolder): Boolean = false

    override fun onSwiped(viewHolder: ViewHolder, direction: Int) {
        if (direction == START) {
            adapter.onItemDismiss(viewHolder.adapterPosition)
        } else if (direction == END) {
            adapter.onItemLike(viewHolder.adapterPosition)
        }
    }

    interface SwipeHelperAdapter {
        fun onItemDismiss(position: Int)
        fun onItemLike(position: Int)
    }
}