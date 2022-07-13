package com.yollpoll.nmb.view.widgets.tag

import android.app.Activity
import android.content.Intent
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.TextView
import com.yollpoll.nmb.view.widgets.tag.ChooseTagActivity
import com.yollpoll.nmb.view.activity.NewThreadActivity
import java.util.ArrayList

/**
 * Created by 鹏祺 on 2017/6/13.
 */
class ChooseTagActivity : Activity() {
    private var listForums: MutableList<ChildForm> = ArrayList<ChildForm>()
    private var clgTags: ChangeLineViewGroup? = null
    private var tagId: String? = null
    private fun initView() {
        clgTags = findViewById<View>(R.id.clg_tags) as ChangeLineViewGroup
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        window.requestFeature(Window.FEATURE_CONTENT_TRANSITIONS)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_choose_tag)
        window.setBackgroundDrawable(ColorDrawable(resources.getColor(R.color.transparent)))
        initView()
        initData()
    }

    private fun initData() {
        tagId = intent.getStringExtra("tagId")
        listForums = SPUtiles.getTags()
        //        removeTimeLine();
        for (i in listForums.indices) {
            val tvTag = LayoutInflater.from(this).inflate(R.layout.item_tag, null, false)
                .findViewById<View>(R.id.tv_tag) as TextView
            val viewGroup = tvTag.parent as ViewGroup
            viewGroup?.removeAllViews()
            tvTag.setText(listForums[i].getName())
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                tvTag.transitionName = listForums[i].getId()
            }
            tvTag.setTag(R.id.tag_first, listForums[i].getId())
            tvTag.setTag(R.id.tag_second, listForums[i].getName())
            tvTag.setOnClickListener(onTagClickListener)
            clgTags.addView(tvTag)
        }
    }

    private val onTagClickListener = View.OnClickListener { v ->
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            for (i in 0 until clgTags.getChildCount()) {
                if (tagId == clgTags.getChildAt(i).getTransitionName()) {
                    clgTags.getChildAt(i).setTransitionName("")
                    break
                }
            }
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            v.transitionName = tagId
        }
        val intent = intent
        intent.putExtra("tagId", v.getTag(R.id.tag_first) as String)
        intent.putExtra("tagName", v.getTag(R.id.tag_second) as String)
        newThreadActivity.setTagName(v.getTag(R.id.tag_second) as String)
        this@ChooseTagActivity.setResult(RESULT_OK, intent)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            finishAfterTransition()
        } else {
            finish()
        }
    }

    //暂时去掉时间线功能
    private fun removeTimeLine() {
        val iterator: MutableIterator<*> = listForums.iterator()
        while (iterator.hasNext()) {
            val childForm: ChildForm = iterator.next() as ChildForm
            if (childForm.getId().equals("-1")) {
                iterator.remove()
            }
        }
    }

    companion object {
        const val CHOOSE_TAG = 1
        private var newThreadActivity: NewThreadActivity? = null
        fun gotoChooseTagActivity(
            activity: Activity,
            tagId: String?,
            vararg pairs: Pair<View?, String?>?
        ) {
            newThreadActivity = activity as NewThreadActivity
            val options: ActivityOptionsCompat = ActivityOptionsCompat
                .makeSceneTransitionAnimation(activity, pairs)
            val intent = Intent(activity, ChooseTagActivity::class.java)
            intent.putExtra("tagId", tagId)
            activity.startActivityForResult(intent, CHOOSE_TAG, options.toBundle())
        }
    }
}