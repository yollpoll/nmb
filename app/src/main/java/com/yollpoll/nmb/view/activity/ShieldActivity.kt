package com.yollpoll.nmb.view.activity

import android.app.Application
import android.content.Context
import android.os.Bundle
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewModelScope
import androidx.paging.cachedIn
import androidx.recyclerview.widget.LinearLayoutManager
import com.yollpoll.annotation.annotation.OnMessage
import com.yollpoll.annotation.annotation.Route
import com.yollpoll.base.*
import com.yollpoll.framework.dispatch.DispatchRequest
import com.yollpoll.framework.fast.FastViewModel
import com.yollpoll.nmb.MR
import com.yollpoll.nmb.R
import com.yollpoll.nmb.adapter.ThreadAdapter
import com.yollpoll.nmb.databinding.ActivityShieldBinding
import com.yollpoll.nmb.model.bean.ArticleItem
import com.yollpoll.nmb.model.repository.ArticleDetailRepository
import com.yollpoll.nmb.model.repository.HomeRepository
import com.yollpoll.nmb.router.DispatchClient
import com.yollpoll.nmb.router.ROUTE_SETTING
import com.yollpoll.nmb.router.ROUTE_SHIELD_LIST
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Created by spq on 2022/12/1
 */

suspend fun gotoShieldActivity(context: Context) {
    DispatchClient.manager?.dispatch(context, DispatchRequest.UrlBuilder(ROUTE_SHIELD_LIST).build())
}

@Route(url = ROUTE_SHIELD_LIST)
@AndroidEntryPoint
class ShieldActivity : NMBActivity<ActivityShieldBinding, ShieldVm>() {
    val vm: ShieldVm by viewModels()
    val layoutManager = LinearLayoutManager(context)
    override fun getLayoutId() = R.layout.activity_shield
    override fun initViewModel() = vm
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initTitle(mDataBinding.headerTitle.toolbar, true)
        initData()
    }

    val adapter = ThreadAdapter(onUrlClick = {
        vm.onThreadUrlClick(it)
    }, onItemLongClick = {
        CommonDialog("取消屏蔽", "是否取消屏蔽", context) {
            vm.cancelShield(it.id)
        }.show()
        true
    }, onImageClick = { item, pos ->
        lifecycleScope.launch {
            gotoThreadImageActivity(context, 0, item.id)
        }
    }) { item ->
        lifecycleScope.launch {
            ThreadDetailActivity.gotoThreadDetailActivity(item.id, context)
        }
    }

    fun initData() {
        lifecycleScope.launch {
            vm.shieldPager.collectLatest {
                "submit".logI()
                adapter.submitData(it)
            }
        }
    }

    @OnMessage
    fun refresh() {
        adapter.refresh()
    }
}

@HiltViewModel
class ShieldVm @Inject constructor(
    val app: Application,
    val repository: HomeRepository,
) :
    FastViewModel(app) {
    var loaded = false

    val shieldPager = getNMBCommonPager {
        object : NMBBasePagingSource<ArticleItem>() {
            override suspend fun load(pos: Int): List<ArticleItem> {
                return if (loaded) {
                    emptyList()
                } else {
                    loaded = true
                    repository.getShieldArticleList()
                }
            }

        }
    }.flow.cachedIn(viewModelScope)

    //url点击
    fun onThreadUrlClick(url: String) {
        if (url.startsWith("/t/")) {
            url.split("/").let {
                if (it.size > 2) {
                    sendMessage(MR.HomeActivity_gotoThreadDetail, it[2])
                }
            }
        } else if (url.startsWith(">>No.")) {
        }
    }

    fun cancelShield(id: String) {
        viewModelScope.launch {
            repository.cancelShield(id)
            sendEmptyMessage(MR.ShieldActivity_refresh)
        }
    }
}