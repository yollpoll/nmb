package com.yollpoll.nmb

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.util.TypedValue
import androidx.annotation.ChecksSdkIntAtLeast
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import com.google.android.material.color.DynamicColors
import com.google.android.material.color.DynamicColorsOptions
import com.squareup.moshi.Moshi
import com.yollpoll.base.NMBApplication
import com.yollpoll.base.R
import com.yollpoll.base.logE
import com.yollpoll.base.logI
import com.yollpoll.framework.extensions.getBean
import com.yollpoll.framework.extensions.getString
import com.yollpoll.framework.extensions.getStringWithFLow
import com.yollpoll.framework.extensions.shortToast
import com.yollpoll.nmb.db.MainDB
import com.yollpoll.nmb.model.bean.CookieBean
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collectLatest
import kotlin.coroutines.CoroutineContext

@HiltAndroidApp
class App : NMBApplication() {
    companion object {
        lateinit var INSTANCE: App
    }

    var cookie: CookieBean? = null
    override fun onCreate() {
        super.onCreate()
        INSTANCE = this
        // Apply dynamic color
        if (isDynamicColorAvailable()) {
            DynamicColors.applyToActivitiesIfAvailable(this)
        } else {
            DynamicColors.applyToActivitiesIfAvailable(
                this,
                DynamicColorsOptions.Builder().setThemeOverlay(R.style.NmbTheme_Overlay).build()
            )
        }
        GlobalScope.launch(Dispatchers.IO) {
            cookie = MainDB.getInstance().getCookieDao().queryUsed()
        }
    }


    @SuppressLint("DefaultLocale")
    @ChecksSdkIntAtLeast(api = Build.VERSION_CODES.S)
    fun isDynamicColorAvailable(): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
            return false
        }
        return true
    }

    /**
     * 获取attr中的颜色
     */
    fun getAttrColor(id: Int): Int {
        return this.getAttrColor(id)
    }

}