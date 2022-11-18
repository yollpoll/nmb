package com.yollpoll.base

import com.yollpoll.arch.log.LogUtils
import com.yollpoll.framework.extensions.getString
import com.yollpoll.framework.extensions.putString
import com.yollpoll.framework.fast.FastApplication
import com.yollpoll.skin.SkinTheme
import com.yollpoll.skin.skinTheme
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

/**
 * Created by spq on 2022/6/22
 */
open class NMBApplication : FastApplication() {
    companion object {
        lateinit var INSTANCE:NMBApplication
    }

    override fun onCreate() {
        super.onCreate()
        INSTANCE=this
        initTheme()
        LogUtils.init(this, "NMB", BuildConfig.DEBUG)
    }

    var appSkinTheme: SkinTheme = SkinTheme.NULL
        set(value) {
            field = value
            skinTheme = value
            GlobalScope.launch {
                putString("theme", value.name)
            }
        }

    private fun initTheme() {
        GlobalScope.launch {
            appSkinTheme = SkinTheme.valueOf(getString("theme", SkinTheme.NULL.name))
            skinTheme = appSkinTheme
        }
    }

}