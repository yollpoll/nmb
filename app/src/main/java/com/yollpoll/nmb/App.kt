package com.yollpoll.nmb

import android.annotation.SuppressLint
import android.os.Build
import androidx.annotation.ChecksSdkIntAtLeast
import com.google.android.material.color.DynamicColors
import com.google.android.material.color.DynamicColorsOptions
import com.yollpoll.base.NMBApplication
import com.yollpoll.base.R
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class App :NMBApplication(){
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
    @ChecksSdkIntAtLeast(api = Build.VERSION_CODES.S)
    fun isDynamicColorAvailable(): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
            return false
        }
        return true
    }
}