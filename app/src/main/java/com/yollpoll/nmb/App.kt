package com.yollpoll.nmb

import android.annotation.SuppressLint
import android.os.Build
import android.util.TypedValue
import androidx.annotation.ChecksSdkIntAtLeast
import com.google.android.material.color.DynamicColors
import com.google.android.material.color.DynamicColorsOptions
import com.yollpoll.base.NMBApplication
import com.yollpoll.base.R
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class App : NMBApplication() {
    companion object {
        lateinit var INSTANCE: App
    }

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
    fun getAttrColor(id: Int):Int {
        return this.getAttrColor(id)
    }
}