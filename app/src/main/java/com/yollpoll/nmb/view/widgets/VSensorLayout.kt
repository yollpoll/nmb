package com.example.sensorlayout

import android.content.Context
import android.graphics.Canvas
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Parcel
import android.os.Parcelable
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.Scroller
import androidx.core.view.children
import androidx.core.view.get
import kotlin.math.abs
import kotlin.math.log

const val BOTTOM_SCALE = 1.05f
const val TOP_SCALE = 1.02f
const val MIDDLE_SCALE = 1f

const val FILTER_ALPHA = 0.25f
class VSensorLayout(context: Context, attributeSet: AttributeSet?) :
    LinearLayout(context, attributeSet), SensorEventListener {

    constructor(context: Context) : this(context, null)

    lateinit var mSensorManager: SensorManager
    lateinit var mAcceleSensor: Sensor    // 重力传感器
    lateinit var mMagneticSensor: Sensor  // 地磁场传感器
    private var mScroller: Scroller = Scroller(context)

    private var bottomScale = BOTTOM_SCALE    //背景缩放
    private var topScale = TOP_SCALE      //前景缩放
    private var middleScale = MIDDLE_SCALE      //前景缩放

    private val scaleRate = (topScale - 1) / (bottomScale - 1)

    init {
        initData()
    }

    fun initData() {
        mSensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        // 重力传感器
        mAcceleSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        // 地磁场传感器
        mMagneticSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)
        //注册监听
        mSensorManager.registerListener(this, mAcceleSensor, SensorManager.SENSOR_DELAY_GAME)
        mSensorManager.registerListener(this, mMagneticSensor, SensorManager.SENSOR_DELAY_GAME)

        Log.i("spq", "child count$childCount")

    }



    //touch拖动
//    var downX: Float = 0f
//    var downY: Float = 0f
//    var moveX: Float = 0f
//    var moveY: Float = 0f
//    var finalX: Int = 0
//    var finalY: Int = 0
//    override fun onTouchEvent(event: MotionEvent): Boolean {
//        val x = event.x
//        val y = event.y
//        when (event.action) {
//            MotionEvent.ACTION_DOWN -> {
//                downX = x
//                downY = y
//            }
//            MotionEvent.ACTION_MOVE -> {
//                moveX = downX - x
//                moveY = downY - y
////                Log.i("spq","x:$moveX y:${moveY}")
//                mScroller.startScroll(
//                    finalX, finalY, moveX.toInt(),
//                    moveY.toInt(), 0
//                )
//                invalidate()
//            }
//            MotionEvent.ACTION_UP -> {
//                finalX = mScroller.finalX
//                finalY = mScroller.finalY
//            }
//        }
//        return true
//    }

    //替换所有child
//    override fun onFinishInflate() {
//        super.onFinishInflate()
//        if(childCount==0)
//            return
//        val list=children.toList()
//        removeAllViews()
//        Log.i("spq","count: $childCount")
//        list.forEach {
//            addView(VScaleView(context,it,it.tag.let { tag ->
//                if((tag is String)&&tag=="bottom"){
//                    return@let bottomScale
//                }else if((tag is String)&&tag=="top"){
//                    return@let topScale
//                }else{
//                    return@let 1f
//                }
//            }))
//        }
//        Log.i("spq","count: $childCount")
//    }

    //根据标签缩放
    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        children.forEach { child ->
            val scale: Float = when (child.tag) {
                "top" -> topScale
                "bottom" -> bottomScale
                "middle" -> middleScale
                else -> return@forEach
            }
            val cWidth = child.measuredWidth
            val cHeight = child.measuredHeight
            val l = (right - left - cWidth * scale) / 2
            val t = (bottom - top - cHeight * scale) / 2
            val r = l + cWidth * scale
            val b = t + cHeight * scale
            child.layout(l.toInt(), t.toInt(), r.toInt(), b.toInt())
        }
    }

    override fun onInterceptTouchEvent(ev: MotionEvent?): Boolean {
        return true
    }


    var mAcceleValues :FloatArray?=null
    var mMageneticValues :FloatArray?=null

    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type == Sensor.TYPE_ACCELEROMETER) {
            mAcceleValues = lowPass(event.values.clone(),mAcceleValues)
        }
        if (event?.sensor?.type == Sensor.TYPE_MAGNETIC_FIELD) {
            mMageneticValues = lowPass(event.values.clone(),mMageneticValues)
        }

        if (mAcceleValues==null || mMageneticValues==null) {
            return
        }

        val values = FloatArray(3)//3
        val R = FloatArray(9)//9
        SensorManager.getRotationMatrix(R, FloatArray(9), mAcceleValues, mMageneticValues);
        SensorManager.getOrientation(R, values)
        // x轴的偏转角度
        values[1] = Math.toDegrees(values[1].toDouble()).toFloat()
        // y轴的偏转角度
        values[2] = Math.toDegrees(values[2].toDouble()).toFloat()
        dispatchScroll(values[1], values[2])
    }

    val mDegreeYMin = -20//y轴最小偏移角度
    val mDegreeYMax = 20

    val mDegreeXMin = -20//x轴最小偏移角度
    val mDegreeXMax = 20


    var mDirection = 1

    var mScrollX:Int = 0
    var mScrollY:Int = 0


    fun dispatchScroll(degreeX: Float, mDegreeY: Float) {
        if (mDegreeY <= 0 && mDegreeY > mDegreeYMin) {
            mScrollX = ((((mDegreeY / abs(mDegreeYMin)) * ((bottomScale-1)*width/2)* mDirection).toInt()))
        } else if (mDegreeY > 0 && mDegreeY < mDegreeYMax) {
            mScrollX = ((((mDegreeY / abs(mDegreeYMax)) * ((bottomScale-1)*width/2) * mDirection).toInt()))
        }

        val mDegreeX=degreeX + 50//实际使用中，一般手机差不多是-60度左右，所以x=x- -60 作为原始角度

        if (mDegreeX <= 0 && mDegreeX > mDegreeXMin) {
            mScrollY = ((((mDegreeX / abs(mDegreeXMin)) * ((bottomScale-1)*height/2) * mDirection).toInt()))
        } else if (mDegreeX > 0 && mDegreeX < mDegreeXMax) {
            mScrollY = ((((mDegreeX / abs(mDegreeXMax)) * ((bottomScale-1)*height/2) * mDirection).toInt()))
        }

        mScroller.startScroll(0, 0, mScrollX, mScrollY,0)
        invalidate()
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
    }

    override fun computeScroll() {
        super.computeScroll()
        if (mScroller.computeScrollOffset()) {//判断滚动是否完成，true说明滚动尚未完成，false说明滚动已经完成
            children.forEach {
                if (it.tag == "bottom") {
                    it.scrollTo(
                        (mScroller.currX),
                        (-mScroller.currY)
                    )
                } else if (it.tag == "top") {
                    it.scrollTo(
                        (-mScroller.currX * scaleRate).toInt(),
                        (mScroller.currY * scaleRate).toInt()
                    )
                }
            }
            invalidate()//触发view重绘
        }
    }

    /**
     * 滤波算法，过滤传感器噪音
     */
    private fun lowPass(input: FloatArray, output: FloatArray?): FloatArray {
        if (output == null) return input
        for (i in input.indices) {
            output[i] = output[i] + FILTER_ALPHA*(input[i] - output[i])
        }
        return output
    }
}