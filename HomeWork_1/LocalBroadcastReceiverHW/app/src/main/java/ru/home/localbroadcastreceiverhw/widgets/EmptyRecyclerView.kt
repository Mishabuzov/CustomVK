package ru.home.localbroadcastreceiverhw.widgets

import android.content.Context
import android.util.AttributeSet
import android.view.View
import androidx.recyclerview.widget.RecyclerView

class EmptyRecyclerView : RecyclerView {
    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(
        context,
        attrs,
        defStyle
    )

    internal lateinit var emptyView: View

    fun checkIfEmptyAndShow() = if (adapter!!.itemCount > 0) showRecycler() else showEmptyView()

    private fun showRecycler() {
        emptyView.visibility = GONE
        visibility = VISIBLE
    }

    private fun showEmptyView() {
        emptyView.visibility = VISIBLE
        visibility = GONE
    }
}