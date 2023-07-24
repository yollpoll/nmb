package com.yollpoll.utils

import android.graphics.BlurMaskFilter
import android.graphics.Color
import android.text.SpannableString
import android.text.Spanned
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.style.ForegroundColorSpan
import android.text.style.MaskFilterSpan
import android.text.style.URLSpan
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.TextView
import com.yollpoll.base.logE
import java.util.regex.Pattern

/**
 * Created by yollpoll on 2016/9/1.
 */
object TransFormContent {
    fun trans(
        content: Spanned, tv: TextView, onClickListener: ((String) -> Unit)? = null
    ) {
        //herf
        val spannableString = SpannableString(content)
        val urlSpans = spannableString.getSpans(0, content.length, URLSpan::class.java)
        urlSpans.forEach {
            spannableString.setSpan(
                MyClickableSpan(it.url, onClickListener),
                spannableString.getSpanStart(it),
                spannableString.getSpanEnd(it),
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }
        //串引用
        val pattern = Pattern.compile(">>No.\\d*")
        val matcher = pattern.matcher(content)
        while (matcher.find()) {
            val group = matcher.group()
            spannableString.setSpan(object : ClickableSpan() {
                override fun onClick(widget: View) {
                    onClickListener?.invoke(group)
                }

                override fun updateDrawState(ds: TextPaint) {
                    super.updateDrawState(ds)
                    ds.isUnderlineText = true
                    ds.color = Color.parseColor("#7cb342")
                }
            }, matcher.start(), matcher.end(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        }

        tv.text = spannableString
//        tv.setLinkTextColor(Color.parseColor("#7cb342"))
        //使用自定义的MovementMethod,解决子view不能把事件传递给父view的问题
        tv.movementMethod = ClickSpanMovementMethod.instance
        //setMovementMethod以后，会自动把tv的focusable 设置为true影响外部的点击事件，需要手动设置false
        tv.isFocusable = false
        tv.isClickable = false
        tv.isLongClickable = false
        tv.setOnTouchListener(LinkMovementMethodOverride())
    }
}