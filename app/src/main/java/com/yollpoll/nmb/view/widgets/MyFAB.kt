package com.yollpoll.nmb.view.widgets

import android.app.Application
import android.content.Context
import android.os.Build
import android.util.AttributeSet
import android.util.Log
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import androidx.coordinatorlayout.widget.CoordinatorLayout
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.yollpoll.arch.log.LogUtils
import com.yollpoll.base.logD
import com.yollpoll.base.logE
import com.yollpoll.base.logI
import com.yollpoll.framework.extensions.shortToast
import com.yollpoll.nmb.R
import kotlin.math.abs

class MyFAB : FloatingActionButton, CoordinatorLayout.AttachedBehavior {
    constructor(context: Context) : super(context)
    constructor(context: Context, attributeSet: AttributeSet) : super(context, attributeSet)
    constructor(context: Context, attributeSet: AttributeSet, defStyleAttr: Int) : super(
        context,
        attributeSet,
        defStyleAttr
    )

    var onTop: (() -> Boolean)? = null
    var onBottom: (() -> Boolean)? = null
    var onLeft: (() -> Boolean)? = null
    var onRight: (() -> Boolean)? = null
    var onClick: (() -> Boolean)? = null
    var onDoubleClick: (() -> Boolean)? = null

    private val gestureDetector =
        GestureDetector(context, object : GestureDetector.SimpleOnGestureListener() {
            override fun onSingleTapUp(e: MotionEvent): Boolean {
                "onClick".logI()
                onClick?.invoke()
                return true
            }

            override fun onDown(e: MotionEvent): Boolean {
                return true
            }

            override fun onFling(
                e1: MotionEvent,
                e2: MotionEvent,
                velocityX: Float,
                velocityY: Float
            ): Boolean {
                val x1 = e1?.x ?: 0f
                val y1 = e1?.y ?: 0f
                val x2 = e2?.x ?: 0f
                val y2 = e2?.y ?: 0f
                val dx = x2 - x1
                val dy = y2 - y1
                if (abs(dx) > abs(dy)) {
                    //横向移动
                    return if (dx > 0) {
                        //向右
                        onRight?.invoke() ?: super.onFling(e1, e2, velocityX, velocityY)
                    } else {
                        //向左
                        onLeft?.invoke() ?: super.onFling(e1, e2, velocityX, velocityY)
                    }
                } else {
                    //竖向移动
                    return if (dy > 0) {
                        //向下
                        onBottom?.invoke() ?: super.onFling(e1, e2, velocityX, velocityY)
                    } else {
                        //向上
                        onTop?.invoke() ?: super.onFling(e1, e2, velocityX, velocityY)
                    }
                }
            }
        })

    override fun dispatchTouchEvent(event: MotionEvent): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            gestureDetector.onTouchEvent(event)
        } else {
            gestureDetector.onTouchEvent(event)
        }

    }
}

class MyFBABehavior : CoordinatorLayout.Behavior<MyFAB>() {
    override fun layoutDependsOn(
        parent: CoordinatorLayout,
        child: MyFAB,
        dependency: View
    ): Boolean {
        return dependency.id == R.id.rv_forum
    }

    override fun onDependentViewChanged(
        parent: CoordinatorLayout,
        child: MyFAB,
        dependency: View
    ): Boolean {
        child.x = (dependency.x - child.width);
        return true
    }
}