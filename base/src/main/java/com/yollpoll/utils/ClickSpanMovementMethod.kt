package com.yollpoll.utils

import android.text.Layout
import android.text.Selection
import android.text.Spannable
import android.text.Spanned
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.view.MotionEvent
import android.view.View
import android.widget.TextView

/**
 * Created by yollpoll on 2018/4/27.
 */
class ClickSpanMovementMethod : LinkMovementMethod() {
    override fun onTouchEvent(widget: TextView, buffer: Spannable, event: MotionEvent): Boolean {
        val action = event.actionMasked
        if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_DOWN) {
            var x = event.x.toInt()
            var y = event.y.toInt()
//            x -= widget.totalPaddingLeft
//            y -= widget.totalPaddingTop
//            x += widget.scrollX
//            y += widget.scrollY
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
                        buffer, buffer.getSpanStart(link[0]), buffer.getSpanEnd(link[0])
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

class LinkMovementMethodOverride : View.OnTouchListener {
    override fun onTouch(v: View, event: MotionEvent): Boolean {
        val widget = v as TextView
        val text: Any = widget.text
        if (text is Spanned) {
            val buffer: Spanned = text as Spanned
            val action = event.action
            if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_DOWN) {
                var x = event.x.toInt()
                var y = event.y.toInt()
                x -= widget.totalPaddingLeft
                y -= widget.totalPaddingTop
                x += widget.scrollX
                y += widget.scrollY
                val layout: Layout = widget.layout
                val line: Int = layout.getLineForVertical(y)
                val off: Int = layout.getOffsetForHorizontal(line, x.toFloat())
                val link: Array<ClickableSpan> = buffer.getSpans(
                    off, off, ClickableSpan::class.java
                )
                if (link.size != 0) {
                    if (action == MotionEvent.ACTION_UP) {
                        link[0].onClick(widget)
                    } else if (action == MotionEvent.ACTION_DOWN) {
                        // Selection only works on Spannable text. In our case setSelection doesn't work on spanned text
                        //Selection.setSelection(buffer, buffer.getSpanStart(link[0]), buffer.getSpanEnd(link[0]));
                    }
                    return true
                }
            }
        }
        return false
    }
}