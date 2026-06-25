package com.unitx.hyphen_android.extension

import android.content.Context
import com.google.android.material.R
import com.google.android.material.checkbox.MaterialCheckBox

fun MaterialCheckBox.setOnCheckedChangeWithAutoColorChange(context: Context, onCheckedChange: (MaterialCheckBox, Boolean) -> Unit){
    this.setOnCheckedChangeListener { buttonView, isChecked ->

        val attrColor = if (isChecked) R.attr.colorSecondary
        else R.attr.hintTextColor

        (buttonView as MaterialCheckBox).buttonTintList = context.themeColorStateList(attrColor)
        buttonView.setTextColor(context.themeColor(attrColor))
        onCheckedChange(buttonView, isChecked)
    }
}

fun MaterialCheckBox.setIsCheckedWithColor(context: Context, isChecked: Boolean){
    this.isChecked = isChecked
    val attrColor = if (isChecked) R.attr.colorSecondary
    else R.attr.hintTextColor
    this.buttonTintList = context.themeColorStateList(attrColor)
    this.setTextColor(context.themeColor(attrColor))
}
