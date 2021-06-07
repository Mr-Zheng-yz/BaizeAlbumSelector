package com.baize.albumselector.view

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView

class LinearVerticalDecoration(
    private val dividerHeight: Int = 2,
    private val dividerColor: Int = -1,
    private val marginLeft: Int = 0,
    private val marginRight: Int = 0,
    private val marginTop: Int = 0
) : RecyclerView.ItemDecoration() {

    private val dividerPaint: Paint by lazy {
        Paint()
    }

    init {
        dividerPaint.color = dividerColor
    }

    override fun onDraw(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        super.onDraw(c, parent, state)
        val itemCount = parent.adapter?.itemCount ?: 0
        if (itemCount == 0) {
            return
        }
        if (dividerColor == -1) {
            return
        }
        val childCount = parent.childCount
        for (i in 0 until childCount) {
            val child = parent.getChildAt(i)
            val position = parent.getChildAdapterPosition(child)
            if (childCount > 1) {
                val left = child.left + marginLeft
                val right = child.right - marginRight
                when (position) {
                    0 -> {
                        c.drawRect(
                            (left) * 1.0f,
                            (child.top) * 1.0f - marginTop,
                            (right) * 1.0f,
                            (child.top) * 1.0f,
                            dividerPaint
                        )
                        c.drawRect(
                            (left) * 1.0f,
                            (child.bottom) * 1.0f,
                            (right) * 1.0f,
                            (child.bottom + dividerHeight) * 1.0f,
                            dividerPaint
                        )
                    }
                    else -> {
                        c.drawRect(
                            (left) * 1.0f,
                            (child.bottom) * 1.0f,
                            (right) * 1.0f,
                            (child.bottom + dividerHeight) * 1.0f,
                            dividerPaint
                        )
                    }
                }
            }
        }
    }

    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        super.getItemOffsets(outRect, view, parent, state)
        val itemCount = parent.adapter?.itemCount ?: 0
        val position = parent.getChildAdapterPosition(view)
        if (itemCount == 0) {
            return
        }
        when (position) {
            0 -> {
                outRect.top = marginTop
                outRect.bottom = dividerHeight
            }
            itemCount - 1 -> {
                outRect.bottom =
                    dividerHeight
            }
            else -> {
                outRect.bottom = dividerHeight
            }
        }
    }
}