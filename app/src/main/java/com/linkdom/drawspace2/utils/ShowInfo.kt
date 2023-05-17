package com.linkdom.drawspace2.utils

import android.content.Context
import android.widget.Toast

fun makeToast(context: Context, s: String) {
    Toast.makeText(context, s, Toast.LENGTH_SHORT).show()
}

fun makeToast(enable: Boolean, context: Context, s: String) {
    if (enable) {
        Toast.makeText(context, s, Toast.LENGTH_SHORT).show()
    }
}