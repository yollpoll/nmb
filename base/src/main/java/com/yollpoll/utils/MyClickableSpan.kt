package com.yollpoll.utils

import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.text.TextPaint
import android.text.style.ClickableSpan
import android.view.View
import com.yollpoll.base.logE
import com.yollpoll.framework.extensions.shortToast

/**
 * Created by 鹏祺 on 2018/4/27.
 */
class MyClickableSpan(
    private val url: String,
    private val onClickListener: ((String) -> Unit)? = null
) : ClickableSpan() {
    override fun onClick(widget: View) {
        onClickListener?.invoke(url)
    }

    override fun updateDrawState(ds: TextPaint) {
        super.updateDrawState(ds)
        ds.isUnderlineText = true
        ds.color = Color.parseColor("#7cb342")
    }
}