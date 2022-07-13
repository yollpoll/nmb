package com.yollpoll.nmb.view.widgets.tag

import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.Intent
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.view.*
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityOptionsCompat
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewModelScope
import com.yollpoll.annotation.annotation.Route
import com.yollpoll.arch.annotation.Extra
import com.yollpoll.base.NMBActivity
import com.yollpoll.floweventbus.FlowEventBus
import com.yollpoll.framework.dispatch.DispatchRequest
import com.yollpoll.framework.extensions.getList
import com.yollpoll.framework.fast.FastViewModel
import com.yollpoll.nmb.ACTION_TAG_NAME
import com.yollpoll.nmb.KEY_FORUM_LIST
import com.yollpoll.nmb.R
import com.yollpoll.nmb.databinding.ActivityChooseTagBinding
import com.yollpoll.nmb.model.bean.Forum
import com.yollpoll.nmb.model.bean.ForumDetail
import com.yollpoll.nmb.router.DispatchClient
import com.yollpoll.nmb.router.ROUTE_CHOOSE_TAG
import com.yollpoll.nmb.view.activity.ACTION_NEW
import com.yollpoll.nmb.view.widgets.tag.ChooseTagActivity
import com.yollpoll.nmb.view.activity.NewThreadActivity
import com.yollpoll.nmb.view.widgets.ChangeLineViewGroup
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.ArrayList
import javax.inject.Inject
import androidx.core.util.Pair
import com.yollpoll.nmb.ACTION_TAG_ID
import kotlinx.coroutines.delay

fun gotoChooseTag(activity: Activity, tagId: String, vararg pairs: Pair<View, String>) {
    val options: ActivityOptionsCompat = ActivityOptionsCompat
        .makeSceneTransitionAnimation(activity, *pairs)

    val intent = Intent(activity, ChooseTagActivity::class.java)
    intent.putExtra("tagId", tagId)
    activity.startActivityForResult(intent, ChooseTagActivity.CHOOSE_TAG, options.toBundle())

}

/**
 * Created by 鹏祺 on 2017/6/13.
 */
@AndroidEntryPoint
@Route(url = ROUTE_CHOOSE_TAG)
class ChooseTagActivity : NMBActivity<ActivityChooseTagBinding, ChooseTagVm>() {
    @Extra
    private var tagId: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        window.requestFeature(Window.FEATURE_CONTENT_TRANSITIONS)
        super.onCreate(savedInstanceState)
        window.setBackgroundDrawable(ColorDrawable(resources.getColor(R.color.transparent)))

        lifecycleScope.launch {
            initData()
        }
    }

    private suspend fun initData() {
        vm.initData()
        for (i in vm.listForums!!.indices) {
            val tvTag = LayoutInflater.from(this).inflate(R.layout.item_tag, null, false)
                .findViewById<View>(R.id.tv_tag) as TextView
            val viewGroup = tvTag.parent as ViewGroup
            viewGroup.removeAllViews()
            tvTag.text = vm.listForums!![i].name
            tvTag.transitionName = vm.listForums!![i].id
            tvTag.setTag(R.id.tag_first, vm.listForums!![i].id)
            tvTag.setTag(R.id.tag_second, vm.listForums!![i].name)
            tvTag.setOnClickListener(onTagClickListener)
            mDataBinding.clgTags.addView(tvTag)
        }
    }

    private val onTagClickListener = View.OnClickListener { v ->
        for (i in 0 until mDataBinding.clgTags.childCount) {
            if (tagId == mDataBinding.clgTags.getChildAt(i).transitionName) {
                mDataBinding.clgTags.getChildAt(i).transitionName = ""
                break
            }
        }
        v.transitionName = tagId
        //发送消息
        vm.selectTag(v.getTag(R.id.tag_second) as String,v.getTag(R.id.tag_first) as String)
        finishAfterTransition()
    }

    companion object {
        const val CHOOSE_TAG = 11110000
//        private var newThreadActivity: NewThreadActivity? = null
//        fun gotoChooseTagActivity(
//            activity: Activity,
//            tagId: String?,
//            vararg pairs: Pair<View?, String?>?
//        ) {
//            newThreadActivity = activity as NewThreadActivity
//            val options: ActivityOptionsCompat = ActivityOptionsCompat
//                .makeSceneTransitionAnimation(activity, pairs)
//            val intent = Intent(activity, ChooseTagActivity::class.java)
//            intent.putExtra("tagId", tagId)
//            activity.startActivityForResult(intent, CHOOSE_TAG, options.toBundle())
//        }
    }

    private val vm: ChooseTagVm by viewModels()
    override fun getLayoutId() = R.layout.activity_choose_tag

    override fun initViewModel() = vm
}

@HiltViewModel
class ChooseTagVm @Inject constructor(val app: Application) : FastViewModel(app) {
    var listForums: List<ForumDetail>? = null

    suspend fun initData() = withContext(Dispatchers.IO) {
        listForums = getList<ForumDetail>(KEY_FORUM_LIST)?.filter {
            return@filter it.id != "-1"
        }
    }

    fun selectTag(name: String,id:String) {
        viewModelScope.launch {
            delay(200)
            FlowEventBus.post(ACTION_TAG_NAME, name)
            FlowEventBus.post(ACTION_TAG_ID, id)
        }
    }

}