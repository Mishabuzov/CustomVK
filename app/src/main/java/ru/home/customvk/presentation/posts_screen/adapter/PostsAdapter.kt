package ru.home.customvk.presentation.posts_screen.adapter

import android.graphics.Bitmap
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import ru.home.customvk.R
import ru.home.customvk.domain.Post

class PostsAdapter(
    private val onLikeListener: (Int) -> Unit,
    private val onRemoveSwipeListener: (Int) -> Unit,
    private val onShareAction: (Bitmap, String) -> Unit
) : RecyclerView.Adapter<ViewHolder>(), PostTouchHelperCallback.SwipeHelperAdapter {

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

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.post_list_item, parent, false)
        return if (viewType == TYPE_TEXT_POST) {
            TextPostHolder(
                itemView = itemView,
                onLikeAction = { position -> onItemLike(position) }
            )
        } else {
            ImagePostHolder(
                itemView = itemView,
                onLikeAction = { position -> onItemLike(position) },
                onShareAction = onShareAction,
            )
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val itemViewType = getItemViewType(position)
        if (itemViewType == TYPE_TEXT_POST) {
            (holder as TextPostHolder).bind(posts[position])
        } else if (itemViewType == TYPE_IMAGE_POST) {
            (holder as ImagePostHolder).bind(posts[position])
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (posts[position].pictureUrl == null) {
            TYPE_TEXT_POST
        } else {
            TYPE_IMAGE_POST
        }
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
