package ru.home.customvk.post

import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.view_post.view.*
import ru.home.customvk.R

open class TextPostHolder(itemView: View, val onLikeAction: (Int) -> Post) : RecyclerView.ViewHolder(itemView) {
    companion object {
        private const val DRAWABLE_RESOURCE_DIR = "drawable"
    }

    protected fun ImageView.setImageResourceByName(resourceName: String) =
        setImageResource(resources.getIdentifier(resourceName, DRAWABLE_RESOURCE_DIR, context.packageName))

    private fun TextView.bindTextContent(textContent: String) =
        if (textContent.isBlank()) {
            text = ""
            visibility = GONE
        } else {
            visibility = VISIBLE
            text = textContent
        }

    private fun Button.bindLikes(post: Post) {
        fun bindLikeContent(post: Post) {
            var likeDrawable = R.drawable.ic_like_24
            if (post.likesCount > 0) {
                text = post.likesCount.toString()
                if (post.isFavorite) {
                    likeDrawable = R.drawable.ic_liked_24
                }
                setCompoundDrawablesWithIntrinsicBounds(likeDrawable, 0, 0, 0)
                compoundDrawablePadding =
                    resources.getDimensionPixelSize(R.dimen.post_action_buttons_drawable_padding)
            } else {
                text = ""
                compoundDrawablePadding = 0
                setCompoundDrawablesWithIntrinsicBounds(likeDrawable, 0, 0, 0)
            }
        }
        bindLikeContent(post)
        likeButton.setOnClickListener { bindLikeContent(onLikeAction(adapterPosition)) }
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
        avatarImageView.setImageResourceByName(post.groupLogo)
        groupNameTextView.text = post.groupName
        timeTextView.text = post.date

        mainTextView.bindTextContent(post.textContent)
        // bind footer
        likeButton.bindLikes(post)
        commentButton.bindAdditionalButtons(post.commentsCount)
        shareButton.bindAdditionalButtons(post.sharesCount)
        viewingTextView.bindTextContent(post.viewings)
    }
}

class ImagePostHolder(itemView: View, onLikeListener: (Int) -> Post) : TextPostHolder(itemView, onLikeListener) {
    override fun bind(post: Post) {
        super.bind(post)
        // bind picture
        with(itemView.postImageView) {
            if (post.pictureName.isNotBlank()) {
                setImageResourceByName(post.pictureName)
            } else {
                setImageResource(0)
            }
        }
    }
}