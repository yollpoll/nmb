package com.yollpoll.nmb.adapter

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.DiffUtil
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.yollpoll.base.MyDiff
import com.yollpoll.framework.utils.getBoolean
import com.yollpoll.nmb.KEY_BIG_IMG
import com.yollpoll.nmb.model.bean.ImgTuple
import com.yollpoll.nmb.net.imgThumbUrl
import com.yollpoll.nmb.net.imgUrl
import com.yollpoll.nmb.router.DispatchClient
import com.yollpoll.nmb.view.activity.ImageFragment

class ImagePagerAdapter(
    private val fragmentActivity: FragmentActivity,
    var imageList: List<String> = arrayListOf(),
) :
    FragmentStateAdapter(fragmentActivity) {

    override fun getItemCount(): Int {
        return imageList.size
    }

    override fun createFragment(position: Int): Fragment {
        val fragment = ImageFragment()
        val args = Bundle()
        args.putString("url", imageList[position])
        fragment.arguments = args
        return fragment
    }

    fun submitData(newData: List<String>) {
        val diffResult =
            DiffUtil.calculateDiff(
                MyDiff(
                    imageList,
                    newData,
                    { old, new ->
                        old == new
                    },
                    { old, new ->
                        old == new
                    }
                ), true
            )
        diffResult.dispatchUpdatesTo(this)
        imageList = newData
    }
}

class ThreadImagePagerAdapter(
    private val fragmentActivity: FragmentActivity,
    var imageList: List<ImgTuple> = arrayListOf(),
) :
    FragmentStateAdapter(fragmentActivity) {
    private val opThumbBigImg = getBoolean(KEY_BIG_IMG, false)
    private val imgHead = if (opThumbBigImg) imgThumbUrl else imgUrl

    override fun getItemCount(): Int {
        return imageList.size
    }

    override fun createFragment(position: Int): Fragment {
        val fragment = ImageFragment()
        val args = Bundle()
        args.putString("url", imgHead + imageList[position].img + imageList[position].ext)
        fragment.arguments = args
        return fragment
    }

    fun submitData(newData: List<ImgTuple>) {
        val diffResult =
            DiffUtil.calculateDiff(
                MyDiff(
                    imageList,
                    newData,
                    { old, new ->
                        old == new
                    },
                    { old, new ->
                        old == new
                    }
                ), true
            )
        diffResult.dispatchUpdatesTo(this)
        imageList = newData
    }
}