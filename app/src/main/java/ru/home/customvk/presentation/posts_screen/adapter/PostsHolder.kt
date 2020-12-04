package ru.home.customvk.presentation.posts_screen.adapter

import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import kotlinx.android.synthetic.main.view_post.view.*
import ru.home.customvk.R
import ru.home.customvk.domain.Post

open class TextPostHolder(itemView: View, val onLikeAction: (Int) -> Unit) : RecyclerView.ViewHolder(itemView) {

    private companion object {
        private const val DELAY_FOR_DISABLING_LIKE_AFTER_CLICK_MS = 500L
    }

    private fun ImageView.setupImage(imageUrl: String) {
        this.clearImageView()
        Glide.with(this)
            .load(imageUrl)
            .into(this)
    }

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
        likeButton.setOnClickListener {
            isEnabled = false
            onLikeAction(adapterPosition)
            postDelayed({ isEnabled = true }, DELAY_FOR_DISABLING_LIKE_AFTER_CLICK_MS)
        }
    }

    /**
     * Binding of comments/sharing button
     */
    private fun Button.bindAdditionalButtons(count: Int) {
        if (count > 0) {
            text = count.toString()
            compoundDrawablePadding = resources.getDimensionPixelSize(R.dimen.post_action_buttons_drawable_padding)
        } else {
            text = ""
            compoundDrawablePadding = 0
        }
        setOnClickListener(null)
    }

    open fun bind(post: Post) {
        with(itemView) {
            // bind header
            avatarImageView.setupImage(post.source.sourceIconUrl)
            groupNameTextView.text = post.source.sourceName
            timeTextView.text = post.readablePublicationDate

            mainTextView.bindTextContent(post.text)
            // bind footer
            likeButton.bindLikes(post)
            commentButton.bindAdditionalButtons(post.commentsCount)
            shareButton.bindAdditionalButtons(post.sharesCount)
            viewingTextView.bindTextContent(post.viewings.toString())

            postImageView.isVisible = false
        }
    }
}

private fun ImageView.clearImageView() = Glide.with(this).clear(this)

class ImagePostHolder(
    itemView: View,
    val onShareAction: (Bitmap, String) -> Unit,
    onLikeListener: (Int) -> Unit
) : TextPostHolder(itemView, onLikeListener) {

    private fun ImageView.setupImageAndConfigureSharing(imageUrl: String) {
        this.clearImageView()
        Glide.with(this)
            .asBitmap()
            .load(imageUrl)
            .into(object : CustomTarget<Bitmap>() {
                override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                    setImageBitmap(resource)
                    postImageView.isVisible = true
                    itemView.shareButton.setOnClickListener { onShareAction(resource, imageUrl) }
                }

                override fun onLoadCleared(placeholder: Drawable?) = Unit
            })
    }

    override fun bind(post: Post) {
        super.bind(post)
        // bind picture
        with(itemView.postImageView) {
            if (post.pictureUrl.isNotBlank()) {
                setupImageAndConfigureSharing(post.pictureUrl)
            }
        }
    }
}
