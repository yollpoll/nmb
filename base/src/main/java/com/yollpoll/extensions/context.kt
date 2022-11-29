package com.yollpoll.extensions

import android.content.Context
import android.content.res.Configuration
import android.net.ConnectivityManager
import android.net.NetworkInfo

/**
 * Created by spq on 2022/11/18
 */
//是否开启黑暗模式
fun Context.isDarkMod(): Boolean {
    val curMod = this.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
    return curMod == Configuration.UI_MODE_NIGHT_YES
}

fun Context.isNetConnected(): Boolean {
    val manager = this.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val info = manager.allNetworkInfo
    info.forEach {
        if (it.state == NetworkInfo.State.CONNECTED) {
            return true
        }
    }
    return false
}