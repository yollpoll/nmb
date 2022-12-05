package com.yollpoll.nmb.view.activity

import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.app.Application
import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.viewModels
import androidx.lifecycle.*
import androidx.recyclerview.widget.*
import androidx.recyclerview.widget.ItemTouchHelper.*
import com.yollpoll.annotation.annotation.Route
import com.yollpoll.base.*
import com.yollpoll.framework.dispatch.DispatchRequest
import com.yollpoll.framework.extensions.dp2px
import com.yollpoll.framework.extensions.toListJson
import com.yollpoll.framework.fast.FastViewModel
import com.yollpoll.nmb.ACTION_REFRESH_FORUM
import com.yollpoll.nmb.BR
import com.yollpoll.nmb.R
import com.yollpoll.nmb.databinding.ActivityForumSettingBinding
import com.yollpoll.nmb.databinding.ItemSettingForumBinding
import com.yollpoll.nmb.model.bean.ForumDetail
import com.yollpoll.nmb.model.repository.ForumRepository
import com.yollpoll.nmb.router.DispatchClient
import com.yollpoll.nmb.router.ROUTE_FORUM_SETTING
import com.yollpoll.utils.setViewMargin
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject
import kotlin.collections.ArrayList


/**
 * Created by spq on 2022/12/2
 */

suspend fun gotoForumSettingActivity(context: Context) {
    DispatchClient.manager?.dispatch(
        context,
        DispatchRequest.UrlBuilder(ROUTE_FORUM_SETTING).build()
    )
}

//板块设置页面
@AndroidEntryPoint
@Route(url = ROUTE_FORUM_SETTING)
class ForumSettingActivity : NMBActivity<ActivityForumSettingBinding, ForumSettingVm>() {
    private val vm: ForumSettingVm by viewModels()
    override fun getLayoutId() = R.layout.activity_forum_setting
    override fun initViewModel() = vm
    val layoutManager = LinearLayoutManager(this)
    val adapter = BaseAdapter<ForumDetail>(
        layoutId = R.layout.item_setting_forum,
        variableId = BR.bean,
        itemSame = { old, new ->
            old.id == new.id
        }, contentSame = { old, new ->
            (old.show == new.show) && (old.name == new.name) && (old.showName == new.showName)
        }, getChangePayload = { old, new ->
            Bundle().apply {
                putInt("show", new.show)
            }
        }, onBindViewHolder = { item, pos, vh, _ ->
            val bind = vh.binding as ItemSettingForumBinding
            bind.btnForum.setOnClickListener {
                vm.toggleShow(vh.bindingAdapterPosition)
                bind.executePendingBindings()
            }
            if (item?.show == 1) {
                bind.btnForum.background =
                    context.resources.getDrawable(R.drawable.ripple_forum, context.theme)
            } else {
                bind.btnForum.background =
                    context.resources.getDrawable(R.drawable.ripple_forum_hide, context.theme)
            }
            bind.executePendingBindings()

        })

    private val itemTouchCallback = object : ItemTouchHelper.Callback() {
        override fun getMovementFlags(
            recyclerView: RecyclerView,
            viewHolder: RecyclerView.ViewHolder
        ): Int {
            return if (recyclerView.layoutManager is GridLayoutManager ||
                recyclerView.layoutManager is StaggeredGridLayoutManager
            ) {
                //此处不需要进行滑动操作，可设置为除4和8之外的整数，这里设为0
                //不支持滑动
                makeMovementFlags(
                    UP or DOWN or
                            LEFT or RIGHT, 0
                )
            } else {
                //如果是LinearLayoutManager则只能向上向下滑动，
                //此处第二个参数设置支持向右滑动
                makeMovementFlags(UP or DOWN, RIGHT)
            }
        }

        override fun onMove(
            recyclerView: RecyclerView,
            viewHolder: RecyclerView.ViewHolder,
            target: RecyclerView.ViewHolder
        ): Boolean {
            val srcPosition = viewHolder.bindingAdapterPosition
            val targetPosition = target.bindingAdapterPosition
            val dataSize = vm.forum.value?.size ?: 0
            if (dataSize > 1 && srcPosition < dataSize && targetPosition < dataSize) {
                vm.swapSort(srcPosition, targetPosition)
                adapter.notifyItemMoved(srcPosition, targetPosition)
                return true
            }
            return false
        }

        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
        }

        override fun onSelectedChanged(viewHolder: RecyclerView.ViewHolder?, actionState: Int) {
            super.onSelectedChanged(viewHolder, actionState)
            when (actionState) {
                ACTION_STATE_DRAG -> {
                }
                ACTION_STATE_SWIPE -> {

                }
            }
        }

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initView()
        initData()
        CommonDialog("使用方法", "长按拖动排序,点击显示/隐藏", context).show()
    }

    private fun initView() {
        initTitle(mDataBinding.layoutTitle.toolbar, true)
        val itemTouchHelper = ItemTouchHelper(itemTouchCallback)
        itemTouchHelper.attachToRecyclerView(mDataBinding.rvForum)
    }

    private fun initData() {
        lifecycleScope.launch {
            vm.forum.observe(this@ForumSettingActivity) {
                adapter.submitData(it)
            }
        }
    }

}

@HiltViewModel
class ForumSettingVm @Inject constructor(app: Application, val repository: ForumRepository) :
    FastViewModel(app) {
    private val forumLD = MutableLiveData<List<ForumDetail>>()
    val forum: LiveData<List<ForumDetail>> = forumLD

    init {
        viewModelScope.launch {
            forumLD.value = repository.getAllFromDB().sortedBy {
                it.sort?.toInt()
            }
        }
    }

    fun swapSort(src: Int, target: Int) {
        viewModelScope.launch {
            Collections.swap(forum.value, src, target)
            val sort = (forum.value?.get(src)?.sort)
            (forum.value?.get(src))?.sort = (forum.value?.get(target)?.sort)
            (forum.value?.get(target)?.sort) = sort
            repository.updateForumList(*forum.value!!.toTypedArray())
            refreshForum()
        }
    }

    fun toggleShow(pos: Int) {
        if (forum.value == null)
            return
        viewModelScope.launch {
            val curList = arrayListOf<ForumDetail>()
            ArrayList(forum.value!!).forEachIndexed { index, it ->
                if (pos == index) {
                    val show = if (it.show == 1) 0 else 1
                    val item = it.copy(show = show)
                    curList.add(item)
                } else {
                    val item = it.copy()
                    curList.add(item)
                }
            }
            repository.updateForumList(curList[pos])
            forumLD.postValue(curList)
            refreshForum()
        }
    }

    private fun refreshForum() {
        sendEmptyMessage(ACTION_REFRESH_FORUM)
    }
}