package ru.home.customvk.presentation.posts_screen.adapter

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.marginBottom
import androidx.core.view.marginEnd
import androidx.core.view.marginStart
import androidx.core.view.marginTop
import kotlinx.android.synthetic.main.view_post.view.*
import ru.home.customvk.R

class PostLayout @JvmOverloads constructor(
    context: Context,
    attributeSet: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ViewGroup(context, attributeSet, defStyleAttr) {

    init {
        LayoutInflater.from(context).inflate(R.layout.view_post, this, true)
    }

    private fun ImageView.setImageSizeParams(desiredWidth: Int) {
        if (drawable == null) {
            minimumHeight = 0
        } else if (drawable.intrinsicWidth < desiredWidth || drawable.intrinsicHeight < minimumHeight) {
            scaleType = ImageView.ScaleType.CENTER_CROP
        }
    }

    private fun TextView.getTextHeight() =
            if (text.isBlank()) {
                measuredHeight
            } else {
                getHeightWithMargins()
            }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val desiredWidth = MeasureSpec.getSize(widthMeasureSpec)
        var height = paddingTop + paddingBottom

        fun measurePostHeader() {
            measureChildWithMargins(avatarImageView, widthMeasureSpec, 0, heightMeasureSpec, height)
            height += avatarImageView.getHeightWithMargins()

            // setting up params of group name and time of publishing post.
            measureChildWithMargins(groupNameTextView, widthMeasureSpec, 0, heightMeasureSpec, height)
            measureChildWithMargins(
                timeTextView, widthMeasureSpec, 0, heightMeasureSpec,
                height + groupNameTextView.getHeightWithMargins()
            )
        }

        fun measurePostContent() {
            measureChildWithMargins(mainTextView, widthMeasureSpec, 0, heightMeasureSpec, height)
            height += mainTextView.getTextHeight()

            postImageView.setImageSizeParams(desiredWidth)
            measureChildWithMargins(postImageView, widthMeasureSpec, 0, heightMeasureSpec, height)
            height += postImageView.getHeightWithMargins()
        }

        fun measurePostFooter() {
            // measure bottom buttons
            measureChildWithMargins(viewingTextView, widthMeasureSpec, 0, heightMeasureSpec, height)

            measureChildWithMargins(likeButton, widthMeasureSpec, 0, heightMeasureSpec, height)
            measureChildWithMargins(commentButton, widthMeasureSpec, 0, heightMeasureSpec, height)
            measureChildWithMargins(shareButton, widthMeasureSpec, 0, heightMeasureSpec, height)
            // these 3 buttons have the same sizes and margins.
            height += likeButton.getHeightWithMargins()
        }
        measurePostHeader()
        measurePostContent()
        measurePostFooter()
        setMeasuredDimension(desiredWidth, resolveSize(height, heightMeasureSpec))
    }

    private fun View.layoutElement(currentLeft: Int, currentTop: Int, expandRightBoundTillEnd: Boolean = false) {
        val currentRight = if (expandRightBoundTillEnd) {
            getRightBoundOfPost(this)
        } else {
            currentLeft + marginStart + measuredWidth
        }
        layout(
            currentLeft + marginStart,
            currentTop + marginTop,
            currentRight,
            currentTop + marginTop + measuredHeight
        )
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        var currentLeft = paddingStart
        var currentTop = paddingTop

        fun layoutPostHeader() {
            avatarImageView.layoutElement(currentLeft, currentTop)
            val avatarBottom = currentTop + avatarImageView.getHeightWithMargins()

            // layout text(name & time) in post's header.
            currentLeft += avatarImageView.getWidthWithMargins()
            groupNameTextView.layoutElement(currentLeft, currentTop, true)
            currentTop += groupNameTextView.getHeightWithMargins()

            timeTextView.layoutElement(currentLeft, currentTop, true)
            currentTop = avatarBottom  // since avatar is bigger that name_&_time in post's header.
        }

        fun layoutPostContent() {
            mainTextView.layoutElement(paddingStart, currentTop, true)
            currentTop += mainTextView.getTextHeight()

            postImageView.layoutElement(paddingLeft, currentTop, true)
            currentTop += postImageView.getHeightWithMargins()
        }

        fun layoutPostFooter() {
            likeButton.layoutElement(paddingStart, currentTop)
            currentLeft = paddingStart + likeButton.getWidthWithMargins()

            commentButton.layoutElement(currentLeft, currentTop)
            currentLeft += commentButton.getWidthWithMargins()

            shareButton.layoutElement(currentLeft, currentTop)

            currentLeft = measuredWidth - paddingEnd - viewingTextView.getWidthWithMargins()
            viewingTextView.layoutElement(currentLeft, currentTop, true)
        }
        layoutPostHeader()
        layoutPostContent()
        layoutPostFooter()
    }

    private fun getRightBoundOfPost(view: View) = measuredWidth - view.marginEnd - paddingEnd

    private fun View.getHeightWithMargins(): Int = measuredHeight + marginTop + marginBottom

    private fun View.getWidthWithMargins(): Int = measuredWidth + marginStart + marginEnd


    // Setting Layout params methods:
    override fun generateLayoutParams(attrs: AttributeSet?) = MarginLayoutParams(context, attrs)

    override fun generateDefaultLayoutParams() = MarginLayoutParams(WRAP_CONTENT, WRAP_CONTENT)

    override fun generateLayoutParams(p: LayoutParams?) = MarginLayoutParams(p)

    override fun checkLayoutParams(p: LayoutParams?) = p is MarginLayoutParams
}