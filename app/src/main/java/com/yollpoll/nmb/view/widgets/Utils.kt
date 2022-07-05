package com.yollpoll.nmb.view.widgets

import android.app.Activity
import android.content.Context
import android.graphics.Rect
import android.os.Build
import androidx.drawerlayout.widget.DrawerLayout
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.yollpoll.framework.extensions.dp2px
import com.yollpoll.nmb.R

/**
 * 左侧drawerlayout手势冲突处理
 */
fun DrawerLayout.initLeftDrawerLayout(context: Activity) {
    val screenHeight = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        context.windowManager.currentWindowMetrics.bounds.height()
    } else {
        context.windowManager.defaultDisplay.height
    }
    if (screenHeight == 0) {
        return
    }
    val rect = Rect(0, 0, 20, screenHeight / 2)
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        this.systemGestureExclusionRects = listOf(rect)
    }
}

/**
 * 右侧drawelayout手势冲突处理
 */
fun DrawerLayout.initRightDrawerLayout(context: Activity) {
    val screenHeight = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        context.windowManager.currentWindowMetrics.bounds.height()
    } else {
        context.windowManager.defaultDisplay.height
    }
    val screenWidth = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        context.windowManager.currentWindowMetrics.bounds.width()
    } else {
        context.windowManager.defaultDisplay.width
    }
    if (screenWidth == 0 || height == 0) {
        return
    }

    val rect = Rect(screenWidth - 20, 0, screenWidth, screenHeight / 2)
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        this.systemGestureExclusionRects = listOf(rect)
    }
}

fun getCommonGlideOptions(context: Context): RequestOptions {
    return RequestOptions()
        .transform(CenterCrop()
            , RoundedCorners(
               context.dp2px(10f)
                    .toInt()
        )
    )
}