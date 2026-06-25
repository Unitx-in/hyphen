package com.unitx.hyphen_android.extension

import android.content.res.Resources
import android.util.TypedValue

fun Int.dp() = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, this.toFloat(), Resources.getSystem().displayMetrics)
    .toInt()

fun Float.dp(): Float = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, this, Resources.getSystem().displayMetrics)

fun Boolean.reverse() = !this

fun <E>List<E>.secondLast() = this[this.size-2]
