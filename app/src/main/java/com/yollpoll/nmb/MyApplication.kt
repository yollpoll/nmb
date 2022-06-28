package com.yollpoll.nmb

import android.annotation.SuppressLint
import android.os.Build.VERSION
import android.os.Build.VERSION_CODES
import androidx.annotation.ChecksSdkIntAtLeast
import com.google.android.material.color.DynamicColors
import com.google.android.material.color.DynamicColorsOptions
import com.yollpoll.base.NMBApplication
import dagger.hilt.android.HiltAndroidApp


/**
 * Created by spq on 2022/6/22
 */
@HiltAndroidApp
class MyApplication : NMBApplication() {
    override fun onCreate() {
        super.onCreate()
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
    @ChecksSdkIntAtLeast(api = VERSION_CODES.S)
    fun isDynamicColorAvailable(): Boolean {
        if (VERSION.SDK_INT < VERSION_CODES.S) {
            return false
        }
        return true
    }

}