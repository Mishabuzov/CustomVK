package ru.home.customviewapplication

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.MarginLayoutParams.WRAP_CONTENT
import androidx.core.view.*

class FlexboxLayout @JvmOverloads constructor(
    context: Context,
    attributeSet: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ViewGroup(context, attributeSet, defStyleAttr) {

    init {
        setWillNotDraw(true)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        val desiredWidth = MeasureSpec.getSize(widthMeasureSpec)
        var height = paddingTop
        var currentRowWidth = paddingLeft
        var maxChildHeightAtRow = 0

        children.forEach { child ->
            measureChildWithMargins(child, widthMeasureSpec, 0, heightMeasureSpec, height)
            currentRowWidth += child.marginStart

            if (currentRowWidth + child.measuredWidth + child.marginEnd > desiredWidth - paddingRight) {
                currentRowWidth = paddingLeft + child.marginStart
                height += maxChildHeightAtRow
                maxChildHeightAtRow = 0
            }
            currentRowWidth += child.measuredWidth + child.marginEnd
            if (child.getHeightWithMargins() > maxChildHeightAtRow) {
                maxChildHeightAtRow = child.getHeightWithMargins()
            }
        }

        setMeasuredDimension(desiredWidth, resolveSize(height, heightMeasureSpec))
    }

    private fun View.getHeightWithMargins() = measuredHeight + marginTop + marginBottom

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        var currentLeft = l + paddingLeft
        var currentTop = t + paddingTop
        var maxChildHeightAtRow = 0

        children.forEach { child ->
            currentLeft += child.marginStart
            if (currentLeft + child.measuredWidth + child.marginEnd > measuredWidth - paddingRight) {
                currentLeft = paddingLeft + child.marginStart
                currentTop += maxChildHeightAtRow
                maxChildHeightAtRow = 0
            }
            val childTop = currentTop + child.marginTop
            val currentRight = currentLeft + child.measuredWidth
            child.layout(
                currentLeft,
                childTop,
                currentRight,
                childTop + child.measuredHeight
            )
            currentLeft = currentRight + child.marginEnd
            if (child.getHeightWithMargins() > maxChildHeightAtRow) {
                maxChildHeightAtRow = child.getHeightWithMargins()
            }
        }
    }

    override fun generateLayoutParams(attrs: AttributeSet) = MarginLayoutParams(context, attrs)

    override fun generateDefaultLayoutParams() = MarginLayoutParams(WRAP_CONTENT, WRAP_CONTENT)

    override fun generateLayoutParams(p: LayoutParams) = MarginLayoutParams(p)

    override fun checkLayoutParams(p: LayoutParams) = p is MarginLayoutParams

}