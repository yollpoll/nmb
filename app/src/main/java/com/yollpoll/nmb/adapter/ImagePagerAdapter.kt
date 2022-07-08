package com.yollpoll.nmb.adapter

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.yollpoll.nmb.router.DispatchClient
import com.yollpoll.nmb.view.activity.ImageFragment

class ImagePagerAdapter(
    private val imageList: List<String>,
    private val fragmentActivity: FragmentActivity
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
}