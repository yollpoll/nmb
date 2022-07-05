package com.yollpoll.utils

import android.text.Selection
import android.text.Spannable
import android.text.method.BaseMovementMethod
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.view.MotionEvent
import android.widget.TextView
import com.yollpoll.base.logE
import com.yollpoll.utils.ClickSpanMovementMethod

/**
 * Created by yollpoll on 2018/4/27.
 */
class ClickSpanMovementMethod : LinkMovementMethod() {
    override fun onTouchEvent(widget: TextView, buffer: Spannable, event: MotionEvent): Boolean {
        val action = event.actionMasked
        if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_DOWN) {
            var x = event.x.toInt()
            var y = event.y.toInt()
            x -= widget.totalPaddingLeft
            y -= widget.totalPaddingTop
            x += widget.scrollX
            y += widget.scrollY
            val layout = widget.layout
            val line = layout.getLineForVertical(y)
            val off = layout.getOffsetForHorizontal(line, x.toFloat())
            val link = buffer.getSpans(off, off, ClickableSpan::class.java)

            if (link.isNotEmpty()) {
                if (action == MotionEvent.ACTION_UP) {
                    link.forEach {
                        it.onClick(widget)
                    }
//                    var target: ClickableSpan? = null
//
//                    for (clickable in link) {
//                        if (clickable is MyClickableSpan) {
//                            target = clickable
//                        }
//                    }
//                    if (target != null) {
//                        target.onClick(widget)
//                    } else {
//                        link[0].onClick(widget)
//                    }
                } else {
                    Selection.setSelection(
                        buffer, buffer.getSpanStart(link[0]),
                        buffer.getSpanEnd(link[0])
                    )
                }
                return true
            } else {
                Selection.removeSelection(buffer)
            }
        }
        return false
    }

    override fun initialize(widget: TextView, text: Spannable) {
        Selection.removeSelection(text)
    }

    companion object {
        private var sInstance: ClickSpanMovementMethod? = null
        val instance: ClickSpanMovementMethod?
            get() {
                if (sInstance == null) {
                    sInstance = ClickSpanMovementMethod()
                }
                return sInstance
            }
    }
}