package com.yollpoll.nmb.view.widgets

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.RelativeLayout
import androidx.cardview.widget.CardView
import com.yollpoll.base.NMBApplication
import com.yollpoll.nmb.App

/**
 * Created by spq on 2022/11/14
 */
class ThemeContainer : LinearLayout {
    val theme = App.INSTANCE.theme
    var realContainer: ViewGroup = LinearLayout(context)

    constructor(context: Context) : super(context) {
        initContainer()
    }

    constructor(context: Context, attributes: AttributeSet) : super(context, attributes) {
        initContainer()
    }

    constructor(context: Context, attributes: AttributeSet, defStyleAttr: Int) : super(
        context,
        attributes,
        defStyleAttr
    ) {
        initContainer()
    }

    private fun initContainer() {
        when(theme){
            "material"->{
                realContainer=CardView(context)
            }
            "common"->{
                realContainer=LinearLayout(context)
            }
            else->{

            }
        }
        addView(realContainer)
    }

    override fun addView(child: View?) {
        realContainer.addView(child)
    }

}