package com.yollpoll.nmb.view.widgets

import android.widget.ImageView
import androidx.databinding.BindingAdapter
import com.bumptech.glide.Glide
import com.yollpoll.nmb.net.imgThumbUrl
import com.yollpoll.nmb.net.imgUrl

class BindAdapter {
    companion object {
        @BindingAdapter(value = ["thumbUrl"], requireAll = true)
        fun thumbUrl(view: ImageView, url: String) {
            //图片加载
            Glide.with(view.context)
                .asBitmap()
                .apply(getCommonGlideOptions(view.context))
                .load(imgThumbUrl + url)
                .into(view)
        }

        @BindingAdapter(value = ["imgUrl"], requireAll = true)
        fun realImgUrl(view: ImageView, url: String) {
            //图片加载
            Glide.with(view.context)
                .asBitmap()
                .apply(getCommonGlideOptions(view.context))
                .load(imgUrl + url)
                .into(view)
        }
    }
}