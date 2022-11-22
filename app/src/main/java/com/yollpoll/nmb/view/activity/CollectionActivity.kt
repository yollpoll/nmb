package com.yollpoll.nmb.view.activity

import android.app.AlertDialog
import android.app.Application
import android.content.Context
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewModelScope
import androidx.paging.cachedIn
import androidx.recyclerview.widget.LinearLayoutManager
import com.yollpoll.annotation.annotation.OnMessage
import com.yollpoll.annotation.annotation.Route
import com.yollpoll.base.*
import com.yollpoll.framework.dispatch.DispatchRequest
import com.yollpoll.framework.extensions.shortToast
import com.yollpoll.framework.fast.FastViewModel
import com.yollpoll.framework.paging.BasePagingDataAdapter
import com.yollpoll.nmb.App
import com.yollpoll.nmb.MR
import com.yollpoll.nmb.R
import com.yollpoll.nmb.adapter.ThreadAdapter
import com.yollpoll.nmb.databinding.ActivityCollectionBinding
import com.yollpoll.nmb.model.bean.ArticleItem
import com.yollpoll.nmb.model.bean.ForumDetail
import com.yollpoll.nmb.model.repository.ArticleDetailRepository
import com.yollpoll.nmb.model.repository.UserRepository
import com.yollpoll.nmb.router.DispatchClient
import com.yollpoll.nmb.router.ROUTE_COLLECTION
import com.yollpoll.nmb.view.widgets.ImportCollectionDialog
import com.yollpoll.nmb.view.widgets.InputDialog
import com.yollpoll.utils.copyStr
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

suspend fun gotoCollectionActivity(context: Context) {
    DispatchClient.manager?.dispatch(context, DispatchRequest.UrlBuilder(ROUTE_COLLECTION).build())
}

@AndroidEntryPoint
@Route(url = ROUTE_COLLECTION)
class CollectionActivity : NMBActivity<ActivityCollectionBinding, CollectionVm>() {
    val vm: CollectionVm by viewModels()
    override fun getLayoutId() = R.layout.activity_collection
    override fun getMenuLayout() = R.menu.menu_collection
    override fun initViewModel() = vm

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_add -> {
                ImportCollectionDialog(this) {
                    it?.run {
                        vm.addCollection(it)
                    }
                }.show()
                return true
            }
            R.id.action_import -> {
                InputDialog(context, "导入订阅", "订阅号", null) {
                    it?.let {
                        vm.importCollection(it)
                    }
                }.show()
            }
        }
        return super.onOptionsItemSelected(item)

    }

    val rvManager = LinearLayoutManager(context)

    val adapter = ThreadAdapter(onUrlClick = {
        vm.onThreadUrlClick(it)
    }, onItemLongClick = {
        CommonDialog("取消订阅", "是否取消订阅", context) {
            vm.delCollection(it.id)
        }.show()
        true
    }, onImageClick = { item, pos ->
        lifecycleScope.launch {
            ImageActivity.gotoImageActivity(
                context,
                0,
                listOf(item.id),
                listOf(item.img + item.ext)
            )
        }
    }) { item ->
        lifecycleScope.launch {
            ThreadDetailActivity.gotoThreadDetailActivity(item.id, context)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initView()
        initData()
    }

    private fun initData() {
        lifecycleScope.launch {
            vm.collectionPager.collectLatest {
                adapter.submitData(it)
            }
        }
    }

    private fun initView() {
        initTitle(mDataBinding.layoutTitle.toolbar, true)
    }

    @OnMessage
    fun refresh() {
        adapter.refresh()
        rvManager.scrollToPosition(0)
    }
}

@HiltViewModel
class CollectionVm @Inject constructor(
    val app: Application,
    val repository: ArticleDetailRepository,
    val userRepository: UserRepository
) :
    FastViewModel(app) {
    val collectionPager = getNMBCommonPager {
        object : NMBBasePagingSource<ArticleItem>() {
            override suspend fun load(pos: Int): List<ArticleItem> {
                return repository.getCollection(pos, userRepository.getCollectionId())
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

    fun delCollection(id: String) {
        viewModelScope.launch {
            showLoading()
            val res = withContext(Dispatchers.IO) {
                return@withContext repository.delCollection(userRepository.getCollectionId(), id)
            }
            hideLoading()
            res.shortToast()
            sendEmptyMessage(MR.CollectionActivity_refresh)
        }
    }

    //添加订阅串
    fun addCollection(tid: String) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                val res = repository.collect(userRepository.getCollectionId(), tid)
                withContext(Dispatchers.Main) {
                    sendEmptyMessage(MR.CollectionActivity_refresh)
                    res.shortToast()
                }
            }
        }
    }

    fun importCollection(id: String) {
        //导入订阅
        viewModelScope.launch {
            showLoading()
            val collectionList = arrayListOf<ArticleItem>()
            var page = 1
            while (true) {
                val data = userRepository.getCollection(page, id)
                collectionList.addAll(data)
                if (data.isEmpty()) {
                    //没有数据
                    break
                }
                page++
            }
            collectionList.forEach {
                userRepository.collect(userRepository.getCollectionId(), it.id)
            }
            hideLoading()
            sendEmptyMessage(MR.CollectionActivity_refresh)
        }
    }
}