package ru.home.customvk.presentation.posts_screen.adapter

import android.graphics.Bitmap
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.swiperefreshlayout.widget.CircularProgressDrawable
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.view_post.view.*
import ru.home.customvk.R
import ru.home.customvk.domain.Post
import ru.home.customvk.utils.PostUtils.convertMillisTimestampToHumanReadableDate

class PostHolderHelper {

    private companion object {
        private const val DELAY_FOR_DISABLING_LIKE_AFTER_CLICK_MS = 500L
    }

    private fun ImageView.setupImage(imageUrl: String) {
        this.clearImageView()
        Glide.with(this)
            .load(imageUrl)
            .into(this)
    }

    private fun ImageView.clearImageView() = Glide.with(this).clear(this)

    private fun TextView.bindTextContent(textContent: String) {
        if (textContent.isBlank()) {
            text = ""
            isVisible = false
        } else {
            text = textContent
            isVisible = true
        }
    }

    private fun Button.bindLikes(post: Post, adapterPosition: Int, onLikeAction: (Int) -> Unit) {
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

    /**
     * Call this method on Holder's itemView for its binding in case if post doesn't contain any photo.
     */
    fun View.bindPostWithoutImage(
        post: Post,
        adapterPosition: Int,
        onLikeAction: (Int) -> Unit
    ) {
        with(this) {
            // bind header
            avatarImageView.setupImage(post.source.sourceIconUrl)
            groupNameTextView.text = post.source.sourceName
            timeTextView.text = post.creationDateMillis.convertMillisTimestampToHumanReadableDate()

            mainTextView.bindTextContent(post.text)
            // bind footer
            likeButton.bindLikes(post, adapterPosition, onLikeAction)
            commentButton.bindAdditionalButtons(post.commentsCount)
            shareButton.bindAdditionalButtons(post.sharesCount)
            viewingTextView.bindTextContent(post.viewings.toString())

            postImageView.isVisible = false
        }
    }

    private fun ImageView.createProgressDrawable(): CircularProgressDrawable {
        return CircularProgressDrawable(context).apply {
            strokeWidth = 10f
            centerRadius = 50f
            setColorFilter(ContextCompat.getColor(context, R.color.postImageCircularProgressColor))
            start()
        }
    }

    private fun ImageView.setupImageAndConfigureSharing(itemView: View, imageUrl: String, onShareAction: (Bitmap, String) -> Unit) {
        this.clearImageView()
        Glide.with(this)
            .asBitmap()
            .load(imageUrl)
            .listener(
                PostImageLoadingListener { loadedBitmap ->
                    itemView.shareButton.setOnClickListener { onShareAction(loadedBitmap, imageUrl) }
                    clearColorFilter()
                }
            )
            .placeholder(createProgressDrawable())
            .into(this)
    }

    /**
     * Call this method on Holder's itemView for its binding in case if post contains photo-attachment.
     */
    fun View.bindPostWithImage(
        post: Post,
        adapterPosition: Int,
        onLikeAction: (Int) -> Unit,
        onShareAction: (Bitmap, String) -> Unit
    ) {
        this.bindPostWithoutImage(post, adapterPosition, onLikeAction)  // bind main part of the post.

        // bind picture
        if (post.pictureUrl.isNotBlank()) {
            postImageView.isVisible = true
            postImageView.setupImageAndConfigureSharing(this, post.pictureUrl, onShareAction)
        }
    }

}
