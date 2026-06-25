package com.unitx.hyphen_android.rv

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView

class OffsetDecorator(private val bottomOffsetPx: Int) : RecyclerView.ItemDecoration() {
    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
        val position = parent.getChildAdapterPosition(view)
        val itemCount = parent.adapter?.itemCount ?: 0
        if (position == RecyclerView.NO_POSITION) {
            outRect.bottom = 0
            return
        }
        outRect.bottom = if (position == itemCount - 1) bottomOffsetPx else 0
    }
}