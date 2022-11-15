package com.yollpoll.base

import com.yollpoll.arch.log.LogUtils
import com.yollpoll.framework.extensions.getString
import com.yollpoll.framework.fast.FastApplication
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

/**
 * Created by spq on 2022/6/22
 */
open class NMBApplication :FastApplication(){
    override fun onCreate() {
        super.onCreate()
        LogUtils.init(this,"NMB",BuildConfig.DEBUG)
    }

}