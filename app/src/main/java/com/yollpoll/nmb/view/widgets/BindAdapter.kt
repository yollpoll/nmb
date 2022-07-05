package com.yollpoll.nmb.view.widgets

import android.widget.ImageView
import androidx.databinding.BindingAdapter

class BindAdapter {
    companion object{
        @BindingAdapter(value = ["imgUrl"],requireAll = true)
        fun imgUrl(view:ImageView,url:String){

        }
    }
}