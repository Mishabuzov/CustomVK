package ru.home.customvk.screens.posts_screen

import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.view_post.view.*
import ru.home.customvk.R
import ru.home.customvk.models.local.Post

open class TextPostHolder(itemView: View, val onLikeAction: (Int) -> Unit) : RecyclerView.ViewHolder(itemView) {

    protected fun ImageView.loadImage(imageUrl: String) = Glide.with(this)
        .load(imageUrl)
        .into(this)

    private fun TextView.bindTextContent(textContent: String) =
        if (textContent.isBlank()) {
            text = ""
            visibility = GONE
        } else {
            visibility = VISIBLE
            text = textContent
        }

    private fun Button.bindLikes(post: Post) {
        var likeDrawable = R.drawable.ic_like_24
        if (post.likesCount > 0) {
            text = post.likesCount.toString()
            if (post.isLiked) {
                likeDrawable = R.drawable.ic_liked_24
            }
            compoundDrawablePadding = resources.getDimensionPixelSize(R.dimen.post_action_buttons_drawable_padding)
        } else {
            text = ""
            compoundDrawablePadding = 0
        }
        setCompoundDrawablesWithIntrinsicBounds(likeDrawable, 0, 0, 0)
        likeButton.setOnClickListener { onLikeAction(adapterPosition) }
    }

    /**
     * Binding of comments/sharing button
     */
    private fun Button.bindAdditionalButtons(count: Int) =
        if (count > 0) {
            text = count.toString()
            compoundDrawablePadding = resources.getDimensionPixelSize(R.dimen.post_action_buttons_drawable_padding)
        } else {
            text = ""
            compoundDrawablePadding = 0
        }

    open fun bind(post: Post) = with(itemView) {
        // bind header
        avatarImageView.loadImage(post.source.sourceIconUrl)
        groupNameTextView.text = post.source.sourceName
        timeTextView.text = post.readablePublicationDate

        mainTextView.bindTextContent(post.text)
        // bind footer
        likeButton.bindLikes(post)
        commentButton.bindAdditionalButtons(post.commentsCount)
        shareButton.bindAdditionalButtons(post.sharesCount)
        viewingTextView.bindTextContent(post.viewings.toString())
    }
}

class ImagePostHolder(
    itemView: View,
    val onShareAction: (String) -> Unit,
    onLikeListener: (Int) -> Unit
) : TextPostHolder(itemView, onLikeListener) {

    override fun bind(post: Post) {
        super.bind(post)
        itemView.shareButton.setOnClickListener { onShareAction(post.pictureUrl) }
        // bind picture
        with(itemView.postImageView) {
            if (post.pictureUrl.isNotBlank()) {
                loadImage(post.pictureUrl)
            } else {
                setImageResource(0)
            }
        }
    }

}