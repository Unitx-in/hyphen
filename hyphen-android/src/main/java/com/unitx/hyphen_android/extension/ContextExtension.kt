package com.unitx.hyphen_android.extension

import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.content.res.Resources
import android.util.TypedValue
import androidx.annotation.AttrRes
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat


fun Context.themeColor(@AttrRes attrResId: Int): Int {
    val typedValue = TypedValue()
    val resolved = theme.resolveAttribute(attrResId, typedValue, true)
    if (!resolved) throw IllegalArgumentException("Attribute not found: $attrResId")

    // Return color resource if available, otherwise the actual color int
    return if (typedValue.resourceId != 0) {
        // This is a reference like @color/md_theme_light_primary
        ContextCompat.getColor(this, typedValue.resourceId)
    } else {
        // This is a literal value like 0xff1d1d1d
        typedValue.data
    }
}

fun Context.themeColorStateList(@AttrRes attrResId: Int): ColorStateList {
    val typedValue = TypedValue()
    val resolved = theme.resolveAttribute(attrResId, typedValue, true)

    if (!resolved) {
        throw IllegalArgumentException("Attribute not found: $attrResId")
    }

    return if (typedValue.resourceId != 0) {
        // Resource reference like @color/color_selector
        ResourcesCompat.getColorStateList(resources, typedValue.resourceId, theme)
            ?: throw Resources.NotFoundException("ColorStateList resource not found: ${typedValue.resourceId}")
    } else {
        // Fallback to single color state list
        ColorStateList.valueOf(typedValue.data)
    }
}

fun Context.restartApp() {
    val intent = packageManager.getLaunchIntentForPackage(packageName)?.apply {
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
    }
    startActivity(intent)
}

