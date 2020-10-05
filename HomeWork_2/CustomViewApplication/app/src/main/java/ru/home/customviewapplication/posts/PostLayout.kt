package ru.home.customviewapplication.posts

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.ImageView
import androidx.core.view.marginBottom
import androidx.core.view.marginEnd
import androidx.core.view.marginStart
import androidx.core.view.marginTop
import kotlinx.android.synthetic.main.view_post.view.*
import ru.home.customviewapplication.R


class PostLayout @JvmOverloads constructor(
    context: Context,
    attributeSet: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ViewGroup(context, attributeSet, defStyleAttr) {

    init {
        LayoutInflater.from(context).inflate(R.layout.view_post, this, true)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val desiredWidth = MeasureSpec.getSize(widthMeasureSpec)

        var height = avatarImageView.marginTop
        measureChildWithMargins(avatarImageView, widthMeasureSpec, 0, heightMeasureSpec, height)
        height += avatarImageView.measuredHeight + avatarImageView.marginBottom

        // setting up params of group name and time of publishing post.
        measureChildWithMargins(
            groupNameTextView, widthMeasureSpec, 0, heightMeasureSpec,
            groupNameTextView.marginTop
        )
        measureChildWithMargins(
            timeTextView, widthMeasureSpec, 0, heightMeasureSpec,
            groupNameTextView.measuredHeight + groupNameTextView.marginTop
        )

        measureChildWithMargins(mainTextView, widthMeasureSpec, 0, heightMeasureSpec, height)
        if (mainTextView.text.isNullOrBlank().not()) {
            height += mainTextView.measuredHeight + mainTextView.marginBottom
        }

        with(postImageView) {
            if (drawable == null) {
                postImageView.minimumHeight = 0
            } else if (drawable.intrinsicWidth < desiredWidth || drawable.intrinsicHeight < minimumHeight) {
                postImageView.scaleType = ImageView.ScaleType.CENTER_CROP
            }
        }
        measureChildWithMargins(postImageView, widthMeasureSpec, 0, heightMeasureSpec, height)
        height += postImageView.measuredHeight

        height += likeButton.marginTop  // these 3 buttons have the same margins.
        measureChildWithMargins(likeButton, widthMeasureSpec, 0, heightMeasureSpec, height)
        measureChildWithMargins(commentButton, widthMeasureSpec, 0, heightMeasureSpec, height)
        measureChildWithMargins(shareButton, widthMeasureSpec, 0, heightMeasureSpec, height)

        val viewingTextViewHeight = height + viewingTextView.marginTop - likeButton.marginTop
        measureChildWithMargins(viewingTextView, widthMeasureSpec, 0, heightMeasureSpec, viewingTextViewHeight)
        height += maxOf(likeButton.measuredHeight, shareButton.measuredHeight, commentButton.measuredHeight)
        height += likeButton.marginBottom
        setMeasuredDimension(desiredWidth, resolveSize(height, heightMeasureSpec))
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        var currentLeft = l + avatarImageView.marginStart
        var currentTop = t + avatarImageView.marginTop

        avatarImageView.layout(
            currentLeft,
            currentTop,
            currentLeft + avatarImageView.measuredWidth,
            currentTop + avatarImageView.measuredHeight
        )
        val avatarBottom = currentTop + avatarImageView.measuredHeight + avatarImageView.marginBottom

        // layout text(name & time) in post's header.
        currentLeft = avatarImageView.measuredWidth + avatarImageView.marginEnd + groupNameTextView.marginStart
        currentTop += groupNameTextView.marginTop - avatarImageView.marginTop
        groupNameTextView.layout(
            currentLeft,
            currentTop,
            measuredWidth - groupNameTextView.marginEnd,
            currentTop + groupNameTextView.measuredHeight
        )
        currentTop += groupNameTextView.measuredHeight

        timeTextView.layout(
            currentLeft,
            currentTop,
            measuredWidth - timeTextView.marginEnd,
            currentTop + timeTextView.measuredHeight
        )
        currentTop = avatarBottom

        if (mainTextView.text.isNullOrBlank().not()) {
            currentLeft = l + mainTextView.marginStart
            mainTextView.layout(
                currentLeft,
                currentTop,
                measuredWidth - mainTextView.marginEnd,
                currentTop + mainTextView.measuredHeight
            )
            currentTop += mainTextView.measuredHeight + mainTextView.marginBottom
        }

        currentLeft = l
        postImageView.layout(
            currentLeft,
            currentTop,
            measuredWidth,
            currentTop + postImageView.measuredHeight
        )
        currentTop += postImageView.measuredHeight + likeButton.marginTop
        currentLeft += likeButton.marginStart

        likeButton.layout(
            currentLeft,
            currentTop,
            currentLeft + likeButton.measuredWidth,
            currentTop + likeButton.measuredHeight
        )
        currentLeft += likeButton.measuredWidth + commentButton.marginStart

        commentButton.layout(
            currentLeft,
            currentTop,
            currentLeft + commentButton.measuredWidth,
            currentTop + commentButton.measuredHeight
        )
        currentLeft += commentButton.measuredWidth + shareButton.marginStart

        shareButton.layout(
            currentLeft,
            currentTop,
            currentLeft + shareButton.measuredWidth,
            currentTop + shareButton.measuredHeight
        )

        currentLeft = r - viewingTextView.marginEnd - viewingTextView.measuredWidth
        currentTop += viewingTextView.marginTop - likeButton.marginTop
        viewingTextView.layout(
            currentLeft,
            currentTop,
            currentLeft + viewingTextView.measuredWidth,
            currentTop + viewingTextView.measuredHeight
        )
    }

    override fun generateLayoutParams(attrs: AttributeSet?) = MarginLayoutParams(context, attrs)

    override fun generateDefaultLayoutParams() = MarginLayoutParams(WRAP_CONTENT, WRAP_CONTENT)

    override fun generateLayoutParams(p: LayoutParams?) = MarginLayoutParams(p)

    override fun checkLayoutParams(p: LayoutParams?) = p is MarginLayoutParams

}