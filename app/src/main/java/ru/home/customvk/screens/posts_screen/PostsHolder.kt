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
     * Since comments/sharing functional is not yet implemented, its binding is the same
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
        timeTextView.text = post.publicationDate

        mainTextView.bindTextContent(post.text)
        // bind footer
        likeButton.bindLikes(post)
        commentButton.bindAdditionalButtons(post.commentsCount)
        shareButton.bindAdditionalButtons(post.sharesCount)
        viewingTextView.bindTextContent(post.viewings.toString())
    }
}

class ImagePostHolder(itemView: View, onLikeListener: (Int) -> Unit) : TextPostHolder(itemView, onLikeListener) {
    override fun bind(post: Post) {
        super.bind(post)
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