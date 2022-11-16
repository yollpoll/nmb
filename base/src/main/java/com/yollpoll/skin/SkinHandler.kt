package com.yollpoll.skin

import android.content.Context
import android.opengl.Visibility
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.RelativeLayout
import androidx.cardview.widget.CardView
import androidx.core.view.children
import androidx.core.view.marginTop
import androidx.core.view.setMargins
import com.yollpoll.base.R
import com.yollpoll.base.logI
import com.yollpoll.framework.extensions.dp2px

/**
 * Created by spq on 2022/11/16
 */
val MaterialItemLine = Pair<String, SkinHandler>(ITEM_LINE) { name, view, attrs ->
    view.visibility = View.GONE
    view
}
val MaterialItem = Pair<String, SkinHandler>(ITEM) { name, view, attrs ->
    if (view is ViewGroup) {
        val cardView = CardView(view.context)
        //获取构造方法
        val constructor =
            view.javaClass.getConstructor(Context::class.java, AttributeSet::class.java)
        constructor.isAccessible = true
        val realLayout = constructor.newInstance(view.context, attrs) as ViewGroup

        view.children.forEach {
            realLayout.addView(it)
        }
        realLayout.setBackgroundColor(view.context.resources.getColor(R.color.black))
        cardView.addView(realLayout)
        cardView.elevation=view.context.dp2px(20f)
        cardView.radius=view.context.dp2px(10f)
        view.removeAllViews()
        "xxxxxxxxxxx".logI()
//        view.addView(cardView)
    }
    "yyyyyyyyyyyyy".logI()

    view
}
