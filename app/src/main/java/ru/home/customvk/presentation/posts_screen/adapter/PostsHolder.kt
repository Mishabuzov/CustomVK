package ru.home.customvk.presentation.posts_screen.adapter

import android.graphics.Bitmap
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import ru.home.customvk.domain.Post

class TextPostHolder(itemView: View, val onLikeAction: (Int) -> Unit) : RecyclerView.ViewHolder(itemView) {

    private val postHolderHelper = PostHolderHelper()

    fun bind(post: Post) {
        with(postHolderHelper) {
            itemView.bindPostWithoutImage(
                post = post,
                adapterPosition = adapterPosition,
                onLikeAction = onLikeAction
            )
        }
    }
}

class ImagePostHolder(
    itemView: View,
    val onLikeAction: (Int) -> Unit,
    val onShareAction: (Bitmap, String) -> Unit
) : RecyclerView.ViewHolder(itemView) {

    private val postHolderHelper = PostHolderHelper()

    fun bind(post: Post) {
        with(postHolderHelper) {
            itemView.bindPostWithImage(
                post = post,
                adapterPosition = adapterPosition,
                onLikeAction = onLikeAction,
                onShareAction = onShareAction
            )
        }
    }
}
