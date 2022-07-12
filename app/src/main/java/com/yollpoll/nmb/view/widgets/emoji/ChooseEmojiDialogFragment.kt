package com.yollpoll.nmb.view.widgets.emoji

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager.widget.PagerAdapter
import com.yollpoll.base.BaseDialogFragment
import androidx.viewpager.widget.ViewPager
import com.google.android.material.tabs.TabLayout
import com.yollpoll.base.logI
import com.yollpoll.nmb.R
import java.util.ArrayList
import kotlin.math.log

/**
 * Created by 鹏祺 on 2017/6/15.
 */
@SuppressLint("ValidFragment")
class ChooseEmojiDialogFragment(private val onEmojiClickListener: ((word: String?, id: Int, fragment: DialogFragment?) -> Unit)?) :
    BaseDialogFragment() {
    private var tabEmoji: TabLayout? = null
    private var vpEmoji: ViewPager? = null
    private var mAdapter: EmojiPageAdapter? = null

    //viewpager
    private val title: MutableList<String> = ArrayList()
    private val views: MutableList<View> = ArrayList()

    //recyclerview
    private var listPicEmoji: List<Int> = ArrayList()
    private var listWordEmoji: List<String> = ArrayList()
    private var rvWordEmoji: RecyclerView? = null
    private var rvPicEmoji: RecyclerView? = null
    private var wordEmojiAdapter: WordEmojiAdapter? = null
    private var picEmojiAdapter: PicEmojiAdapter? = null
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.dialog_choose_emoji, null)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView(view)
        initData()
    }


    private fun initData() {
        title.add("颜文字")
        title.add("芦苇娘")
        //颜文字
        listWordEmoji = EmojiUtils.wordEmojiList
        wordEmojiAdapter = WordEmojiAdapter(listWordEmoji) { _, position ->
            onEmojiClickListener?.invoke(
                listWordEmoji[position],
                0,
                this@ChooseEmojiDialogFragment
            )
        }
        rvWordEmoji = RecyclerView(requireContext())
        rvWordEmoji!!.adapter = wordEmojiAdapter
        val wordManager = GridLayoutManager(activity, 4, GridLayoutManager.VERTICAL, false)
        rvWordEmoji!!.layoutManager = wordManager
        //芦苇娘
        listPicEmoji = EmojiUtils.picLwnEmojiList
        picEmojiAdapter = PicEmojiAdapter(listPicEmoji) { _, position ->
            onEmojiClickListener?.invoke(
                "",
                listPicEmoji[position],
                this@ChooseEmojiDialogFragment
            )
        }
        rvPicEmoji = RecyclerView(requireContext())
        rvPicEmoji!!.adapter = picEmojiAdapter
        val picManager = GridLayoutManager(activity, 4, GridLayoutManager.VERTICAL, false)
        rvPicEmoji!!.layoutManager = picManager
        //viewpager
        views.add(rvWordEmoji!!)
        views.add(rvPicEmoji!!)
        mAdapter = EmojiPageAdapter(views, title)
        vpEmoji!!.setAdapter(mAdapter)
        tabEmoji!!.tabMode = TabLayout.MODE_FIXED
        tabEmoji!!.setupWithViewPager(vpEmoji)
    }

    private fun initView(view: View) {
        tabEmoji = view.findViewById<View>(R.id.tab_emoji) as TabLayout
        vpEmoji = view.findViewById<View>(R.id.vp_emoji) as ViewPager
    }

//    private val onWordEmojiItemClickListener = { _: View, position: Int ->
//        onEmojiClickListener?.invoke(
//            listWordEmoji[position],
//            0,
//            this@ChooseEmojiDialogFragment
//        )
//    }
//    private val onPicEmojiItemClickListener = { _: View, position: Int ->
//        onEmojiClickListener?.invoke(
//            "",
//            listPicEmoji[position],
//            this@ChooseEmojiDialogFragment
//        )
//    }

//    constructor() {
//        this.onEmojiClickListener = onEmojiClickListener
//    }

//    interface OnEmojiClickListener {
//        fun onClick(word: String?, id: Int, fragment: DialogFragment?)
//    }

    private inner class EmojiPageAdapter(
        private val views: List<View>,
        private val titles: List<String>
    ) : PagerAdapter() {
        override
        fun getPageTitle(position: Int): CharSequence {
            return titles[position]
        }

        override
        fun getCount(): Int {
            return views.size
        }

        override
        fun isViewFromObject(view: View, `object`: Any): Boolean {
            return view === `object`
        }

        override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
            super.destroyItem(container, position, `object`)
            container.removeView(views[position])

        }

        override
        fun instantiateItem(container: ViewGroup, position: Int): Any {
            container.addView(views[position])
            return views[position]
        }
    }
}