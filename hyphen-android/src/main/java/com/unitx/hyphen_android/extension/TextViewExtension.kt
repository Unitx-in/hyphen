package com.unitx.hyphen_android.extension

import android.view.MotionEvent
import android.view.View
import android.widget.TextView

fun TextView.setOnCompoundDrawableClickListener(
    onStart: ((TextView) -> Unit)? = null,
    onTop: ((TextView) -> Unit)? = null,
    onEnd: ((TextView) -> Unit)? = null,
    onBottom: ((TextView) -> Unit)? = null
) {
    setOnTouchListener { view, event ->
        if (event.action == MotionEvent.ACTION_UP) {
            val drawables = compoundDrawablesRelative
            val x = event.x.toInt()
            val y = event.y.toInt()

            drawables[0]?.let { drawableStart ->
                val bounds = drawableStart.bounds
                val drawableWidth = bounds.width()
                if (layoutDirection == View.LAYOUT_DIRECTION_RTL) {
                    if (x >= width - paddingEnd - drawableWidth && x <= width - paddingEnd) {
                        onEnd?.invoke(this)
                        view.performClick()
                        return@setOnTouchListener true
                    }
                } else {
                    if (x >= paddingStart && x <= paddingStart + drawableWidth) {
                        onStart?.invoke(this)
                        view.performClick()
                        return@setOnTouchListener true
                    }
                }
            }

            drawables[2]?.let { drawableEnd ->
                val bounds = drawableEnd.bounds
                val drawableWidth = bounds.width()
                if (layoutDirection == View.LAYOUT_DIRECTION_RTL) {
                    if (x >= paddingStart && x <= paddingStart + drawableWidth) {
                        onStart?.invoke(this)
                        view.performClick()
                        return@setOnTouchListener true
                    }
                } else {
                    if (x >= width - paddingEnd - drawableWidth && x <= width - paddingEnd) {
                        onEnd?.invoke(this)
                        view.performClick()
                        return@setOnTouchListener true
                    }
                }
            }

            drawables[1]?.let { drawableTop ->
                val bounds = drawableTop.bounds
                val drawableHeight = bounds.height()
                if (y >= paddingTop && y <= paddingTop + drawableHeight) {
                    onTop?.invoke(this)
                    view.performClick()
                    return@setOnTouchListener true
                }
            }

            drawables[3]?.let { drawableBottom ->
                val bounds = drawableBottom.bounds
                val drawableHeight = bounds.height()
                if (y >= height - paddingBottom - drawableHeight && y <= height - paddingBottom) {
                    onBottom?.invoke(this)
                    view.performClick()
                    return@setOnTouchListener true
                }
            }
        }
        false
    }

    // Ensure accessibility services recognize the view as clickable
    isClickable = true
}

