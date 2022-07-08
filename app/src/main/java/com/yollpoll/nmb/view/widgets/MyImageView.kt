package com.yollpoll.nmb.view.widgets

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import androidx.appcompat.widget.AppCompatImageView
import com.yollpoll.base.logI
import kotlin.math.sqrt

//支持手势操作imageview
class MyImageView : AppCompatImageView {
    constructor(context: Context) : super(context)
    constructor(context: Context, attributeSet: AttributeSet) : super(context, attributeSet)

    var oldDis: Double = 0.0//两个手指之间的距离
    var downX0 = 0.0f//第一根手指的x坐标
    var downY0 = 0.0f
    var downX1 = 0.0f
    var downY1 = 0.0f
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        "onTouch:${event?.pointerCount}".logI()
        when (event?.action) {
            MotionEvent.ACTION_POINTER_DOWN -> {
                downX0 = event.getX(0)
                downY0 = event.getY(0)
                downX1 = event.getX(1)
                downY1 = event.getY(1)
                oldDis = spacing(event)
            }
            MotionEvent.ACTION_MOVE -> {
                if (event.pointerCount == 2) {
                    val x0 = event.getX(0)
                    val x1 = event.getX(1)
                    val y0 = event.getY(0)
                    val y1 = event.getY(1)

                    val changeX0 = (x0 - downX0)
                    val changeX1 = x1 - downX1
                    val changeY0 = (y0 - downY0)
                    val changeY1 = y1 - downY1

                    val lessX = (changeX0 / 2 + changeX1 / 2)
                    val lessY = (changeY0 / 2 + changeY1 / 2)
//                        setSelfPivot(-lessX, -lessY)
                    setPivot(200f, 200f)
                    "onTouch: lexxX ${lessX} lessy $lessY".logI()


//                    val newDis = spacing(event)
//
//                    val sacle = scaleX+(newDis - oldDis) / oldDis
                    return true
                }
            }
        }
        return true

    }

    /**
     * 计算两个点的距离
     *
     * @param event
     * @return
     */
    private fun spacing(event: MotionEvent): Double {
        return if (event.pointerCount == 2) {
            val x = event.getX(0) - event.getX(1)
            val y = event.getY(0) - event.getY(1)
            sqrt((x * x + y * y).toDouble())
        } else {
            0f.toDouble()
        }
    }

    /**
     * 平移画面，当画面的宽或高大于屏幕宽高时，调用此方法进行平移
     *
     * @param x
     * @param y
     */
    private fun setPivot(x: Float, y: Float) {
        pivotX = x
        pivotY = y
        postInvalidate()
    }

    /**
     * 移动
     *
     * @param lessX
     * @param lessY
     */
    private fun setSelfPivot(lessX: Float, lessY: Float) {
        var setPivotX = 0f
        var setPivotY = 0f
        setPivotX = pivotX + lessX
        setPivotY = pivotY + lessY
        if (setPivotX < 0 && setPivotY < 0) {
            setPivotX = 0f
            setPivotY = 0f
        } else if (setPivotX > 0 && setPivotY < 0) {
            setPivotY = 0f
            if (setPivotX > width) {
                setPivotX = width.toFloat()
            }
        } else if (setPivotX < 0 && setPivotY > 0) {
            setPivotX = 0f
            if (setPivotY > height) {
                setPivotY = height.toFloat()
            }
        } else {
            if (setPivotX > width) {
                setPivotX = width.toFloat()
            }
            if (setPivotY > height) {
                setPivotY = height.toFloat()
            }
        }
        setPivot(setPivotX, setPivotY)
    }
}