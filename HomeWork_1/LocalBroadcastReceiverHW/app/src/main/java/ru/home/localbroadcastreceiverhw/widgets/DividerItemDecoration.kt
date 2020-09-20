package ru.home.localbroadcastreceiverhw.widgets

import android.R
import android.content.Context
import android.graphics.Canvas
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class DividerItemDecoration(context: Context) : RecyclerView.ItemDecoration() {

    private val divider: Drawable? by lazy {
        val a = context
            .obtainStyledAttributes(intArrayOf(R.attr.listDivider))
        val drawable = a.getDrawable(0)
        a.recycle()
        drawable
    }

    private var orientation = -1

    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        super.getItemOffsets(outRect, view, parent, state)
        if (divider == null) {
            return
        }
        val position = parent.getChildAdapterPosition(view)
        if (position == RecyclerView.NO_POSITION || position == 0) {
            return
        }
        if (orientation == -1) {
            getOrientation(parent)
        }
        if (orientation == LinearLayoutManager.VERTICAL) {
            outRect.top = divider?.intrinsicHeight ?: 0
        } else {
            outRect.left = divider?.intrinsicWidth ?: 0
        }
    }

    override fun onDrawOver(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        if (divider == null) {
            super.onDrawOver(c, parent, state)
            return
        }

        // Initialization needed to avoid compiler warning
        var left = 0
        var right = 0
        var top = 0
        var bottom = 0
        val size: Int
        val orientation = if (this.orientation != -1) this.orientation else getOrientation(parent)
        val childCount = parent.childCount
        if (orientation == LinearLayoutManager.VERTICAL) {
            size = divider?.intrinsicHeight ?: 0
            left = parent.paddingLeft
            right = parent.width - parent.paddingRight
        } else { //horizontal
            size = divider?.intrinsicWidth ?: 0
            top = parent.paddingTop
            bottom = parent.height - parent.paddingBottom
        }
        for (i in 1 until childCount) {
            val child = parent.getChildAt(i)
            val params = child.layoutParams as RecyclerView.LayoutParams
            if (orientation == LinearLayoutManager.VERTICAL) {
                top = child.top - params.topMargin - size
                bottom = top + size
            } else { //horizontal
                left = child.left - params.leftMargin
                right = left + size
            }
            divider?.setBounds(left, top, right, bottom)
            divider?.draw(c)
        }
        // show last divider
    }

    private fun getOrientation(parent: RecyclerView): Int {
        if (orientation == -1) {
            if (parent.layoutManager is LinearLayoutManager) {
                val layoutManager: LinearLayoutManager =
                    parent.layoutManager as LinearLayoutManager
                orientation = layoutManager.orientation
            }
        }
        return orientation
    }
}