package com.yollpoll.utils

import android.graphics.*

/**
 * 裁剪完以后会回收旧bitmap
 */
fun Bitmap.centerCrop(width: Int, height: Int): Bitmap {
    val desRate: Float = width.toFloat() / height
    val srcRate: Float = this.width.toFloat() / this.height
    var newWidth = this.width
    var newHeight = this.height
    var dx = 0
    var dy = 0
    if (desRate == srcRate) {
        return this
    } else if (srcRate > desRate) {
        newWidth = (this.height * desRate).toInt()
        dx = (this.width - newWidth) / 2
    } else {
        newHeight = (this.width / desRate).toInt()
        dy = (this.height - newHeight) / 2
    }
    val res = Bitmap.createBitmap(this, dx, dy, newWidth, newHeight)
    this.recycle()
    return res
}

//设置圆角
fun Bitmap.roundCorners(radius: Float): Bitmap {
    val res = Bitmap.createBitmap(this.width, this.height, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(res)
    val paint = Paint()
    paint.isAntiAlias = true
    paint.shader = BitmapShader(this, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP)
    canvas.drawRoundRect(0f, 0f, this.width.toFloat(), this.height.toFloat(), radius, radius, paint)
    this.recycle()
    return res
}