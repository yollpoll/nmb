package com.yollpoll.base

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