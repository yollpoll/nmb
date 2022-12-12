package com.yollpoll.nmb

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.annotation.ChecksSdkIntAtLeast
import androidx.core.content.pm.ShortcutInfoCompat
import androidx.core.content.pm.ShortcutManagerCompat
import androidx.core.graphics.drawable.IconCompat
import androidx.datastore.core.DataStore
import com.google.android.material.color.DynamicColors
import com.google.android.material.color.DynamicColorsOptions
import com.yollpoll.arch.message.MessageManager
import com.yollpoll.base.NMBApplication
import com.yollpoll.floweventbus.FlowEventBus
import com.yollpoll.framework.extensions.getString
import com.yollpoll.framework.extensions.putString
import com.yollpoll.framework.extensions.saveBean
import com.yollpoll.nmb.db.MainDB
import com.yollpoll.nmb.model.bean.CookieBean
import com.yollpoll.nmb.net.NEW_THREAD
import com.yollpoll.nmb.view.activity.NewThreadActivity
import com.yollpoll.skin.SkinTheme
import com.yollpoll.skin.skinTheme
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.*
import javax.inject.Inject

@HiltAndroidApp
class App : NMBApplication() {
    companion object {
        lateinit var INSTANCE: App
    }

    @Inject
    lateinit var crashHandler: MyCrashHandler
    var cookie: CookieBean? = null
    lateinit var androidId: String//订阅id

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
            androidId = Settings.System.getString(contentResolver, Settings.Secure.ANDROID_ID)
        }
        initFastAction()
        //全局异常捕获
//        Thread.setDefaultUncaughtExceptionHandler(crashHandler)


    }


    @SuppressLint("DefaultLocale")
    @ChecksSdkIntAtLeast(api = Build.VERSION_CODES.S)
    fun isDynamicColorAvailable(): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
            return false
        }
        return true
    }

    private fun initFastAction() {
        //动态方式添加一
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
            val shortScan = ShortcutInfoCompat.Builder(this, "new_thread")//唯一标识id
                .setShortLabel(getString(R.string.new_thread))//短标签
                .setIcon(IconCompat.createWithResource(this, R.mipmap.ic_launcher))//图标
                //跳转的目标，定义Activity
                .setIntent(
                    Intent(
                        "com.yollpoll.nmb.newThread",
                        null,
                        this,
                        NewThreadActivity::class.java
                    )
                )
                .build()
            //执行添加操作
            ShortcutManagerCompat.addDynamicShortcuts(this, mutableListOf(shortScan))
        }
    }


//    var appSkinTheme: SkinTheme = SkinTheme.NULL
//        set(value) {
//            field = value
//            skinTheme=value
//            GlobalScope.launch {
//                putString("theme", value.name)
//            }
//        }
//
//    private fun initTheme() {
//        GlobalScope.launch {
//            appSkinTheme = SkinTheme.valueOf(getString("theme", SkinTheme.NULL.name))
//            skinTheme=appSkinTheme
//        }
//    }


}