package com.yollpoll.base

import android.content.Context
import android.util.TypedValue
import com.yollpoll.arch.log.LogUtils

fun String.logI() {
    LogUtils.i(this)
}

fun String.logE() {
    LogUtils.e(this)
}

fun String.logD() {
    LogUtils.d(this)
}

fun Context.getAttrColor(id: Int): Int {
    val typedValue = TypedValue()
    theme.resolveAttribute(id, typedValue, true)
    return typedValue.data
}
