package com.unitx.hyphen_android.extension

import android.view.View
import androidx.core.view.isGone
import androidx.core.view.isInvisible
import androidx.core.view.isVisible

fun View.gone(){
    if (this.isGone) return
    this.visibility = View.GONE
}

fun View.visible() {
    if (this.isVisible) return
    this.visibility = View.VISIBLE
}

fun View.invisible() {
    if (this.isInvisible) return
    this.visibility = View.INVISIBLE
}

fun View.visibleIf(condition: Boolean) {
    visibility = if (condition) View.VISIBLE else View.GONE
}

fun View.reverseVisibility(){
    this.visibility = if (this.isGone) View.VISIBLE else if (this.isInvisible) View.VISIBLE else View.GONE
}

fun View.getReversedVisibility() = if (this.isGone) View.VISIBLE else if (this.isInvisible) View.VISIBLE else View.GONE