package com.yollpoll.nmb.view.widgets

import android.app.Activity
import android.content.Context
import android.graphics.Rect
import android.os.Build
import android.view.View
import androidx.drawerlayout.widget.DrawerLayout
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.yollpoll.base.getAttrColor
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
        .transform(
            CenterCrop(), RoundedCorners(
                context.dp2px(10f)
                    .toInt()
            )
        )
}

//初始化swiperefresh
fun SwipeRefreshLayout.init(context: Context, onRefresh: (() -> Unit)? = null) {
    this.setColorSchemeColors(
        context.getAttrColor(R.attr.colorPrimary),
        context.getAttrColor(R.attr.colorSecondary),
        context.getAttrColor(R.attr.colorTertiary),
    )
    this.setOnRefreshListener(onRefresh)
}

//viewpager2适配器
private const val MIN_SCALE = 0.85f
private const val MIN_ALPHA = 0.5f

class ZoomOutPageTransformer : ViewPager2.PageTransformer {
    override fun transformPage(view: View, position: Float) {
        view.apply {
            val pageWidth = width
            val pageHeight = height
            when {
                position < -1 -> { // [-Infinity,-1)
                    // This page is way off-screen to the left.
                    alpha = 0f
                }
                position <= 1 -> { // [-1,1]
                    // Modify the default slide transition to shrink the page as well
                    val scaleFactor = Math.max(MIN_SCALE, 1 - Math.abs(position))
                    val vertMargin = pageHeight * (1 - scaleFactor) / 2
                    val horzMargin = pageWidth * (1 - scaleFactor) / 2
                    translationX = if (position < 0) {
                        horzMargin - vertMargin / 2
                    } else {
                        horzMargin + vertMargin / 2
                    }

                    // Scale the page down (between MIN_SCALE and 1)
                    scaleX = scaleFactor
                    scaleY = scaleFactor

                    // Fade the page relative to its size.
                    alpha = (MIN_ALPHA +
                            (((scaleFactor - MIN_SCALE) / (1 - MIN_SCALE)) * (1 - MIN_ALPHA)))
                }
                else -> { // (1,+Infinity]
                    // This page is way off-screen to the right.
                    alpha = 0f
                }
            }
        }
    }
}
