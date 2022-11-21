package com.yollpoll.extensions

import android.content.Context
import android.content.res.Configuration

/**
 * Created by spq on 2022/11/18
 */
//是否开启黑暗模式
fun Context.isDarkMod(): Boolean {
    val curMod = this.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
    return curMod == Configuration.UI_MODE_NIGHT_YES
}