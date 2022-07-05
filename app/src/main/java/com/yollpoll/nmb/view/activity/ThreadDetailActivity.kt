package com.yollpoll.nmb.view.activity

import android.app.Application
import android.content.Context
import android.os.Bundle
import androidx.activity.viewModels
import androidx.databinding.Bindable
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewModelScope
import androidx.paging.*
import androidx.recyclerview.widget.LinearLayoutManager
import com.yollpoll.annotation.annotation.OnMessage
import com.yollpoll.annotation.annotation.Route
import com.yollpoll.arch.annotation.Extra
import com.yollpoll.base.NMBActivity
import com.yollpoll.base.NMBBasePagingSource
import com.yollpoll.base.logE
import com.yollpoll.base.logI
import com.yollpoll.framework.dispatch.DispatchRequest
import com.yollpoll.framework.extensions.shortToast
import com.yollpoll.framework.extensions.toJson
import com.yollpoll.framework.extensions.toJsonBean
import com.yollpoll.framework.fast.FastActivity
import com.yollpoll.framework.fast.FastViewModel
import com.yollpoll.framework.paging.getCommonPager
import com.yollpoll.nmb.BR
import com.yollpoll.nmb.MR
import com.yollpoll.nmb.R
import com.yollpoll.nmb.adapter.ThreadAdapter
import com.yollpoll.nmb.databinding.ActivityThreadDetailBinding
import com.yollpoll.nmb.model.bean.ArticleItem
import com.yollpoll.nmb.model.repository.ArticleDetailRepository
import com.yollpoll.nmb.model.repository.HomeRepository
import com.yollpoll.nmb.router.DispatchClient
import com.yollpoll.nmb.router.ROUTE_THREAD_DETAIL
import com.yollpoll.nmb.view.widgets.LinkArticleDialog
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
@Route(url = ROUTE_THREAD_DETAIL)
class ThreadDetailActivity : NMBActivity<ActivityThreadDetailBinding, ThreadDetailVM>() {
    @Extra
    lateinit var id: String

    companion object {
        suspend fun gotoThreadDetailActivity(
            id: String,
            context: Context
        ) {
            val req = DispatchRequest.RequestBuilder().host("nmb").module("thread_detail").params(
                hashMapOf("id" to id)
            ).build()
            DispatchClient.manager?.dispatch(context, req)
        }
    }

    private val vm by viewModels<ThreadDetailVM>()
    override fun getLayoutId() = R.layout.activity_thread_detail
    override fun initViewModel() = vm
    private val mManager = LinearLayoutManager(this)
    private val mAdapter = ThreadAdapter(false, {
        vm.onUrlClick(it)
    })

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initView()
        initData()
    }

    private fun initView() {
        initTitle(mDataBinding.layoutTitle.toolbar, true) {
            this.finish()
        }
        //初始化列表
        mDataBinding.rvContent.layoutManager = mManager
        mDataBinding.rvContent.adapter = mAdapter
    }

    private fun initData() {
        lifecycleScope.launch {
            launch {
                vm.getPager(id).collectLatest { data ->
                    mAdapter.submitData(data)
                }
            }
        }

    }

    @OnMessage
    fun gotoSelf(id: String) {
        lifecycleScope.launch {
            ThreadDetailActivity.gotoThreadDetailActivity(id, context)
        }
    }

    @OnMessage
    fun showLinkArticle(articleItem: ArticleItem) {
        LinkArticleDialog(context, articleItem) {
            vm.onUrlClick(it)
        }.show()
    }

}

@HiltViewModel
class ThreadDetailVM @Inject constructor(
    val app: Application,
    val repository: ArticleDetailRepository
) : FastViewModel(app) {
    @Bindable
    var title: String = "无标题"

    //缓存用来引用
    val cache = hashMapOf<String, ArticleItem>()
    fun getPager(id: String): Flow<PagingData<ArticleItem>> {
        return getCommonPager {
            object : NMBBasePagingSource<ArticleItem>() {
                override suspend fun load(pos: Int): List<ArticleItem> {
                    val data = repository.getArticleDetail(id, pos)
                    val res = arrayListOf<ArticleItem>()
                    try {
                        if (pos == 0) {
                            val head = data.copy()
                            title = head.title
                            notifyPropertyChanged(BR.title)
                            head.master = "1"
                            cache[head.id] = head
                            res.add(head)
                        }
                    } catch (e: Exception) {
                        e.message?.logI()
                    }

                    data.Replies?.let {
                        it.forEach { reply ->
                            if (reply.user_hash == data.user_hash) {
                                reply.master = "1"
                            } else {
                                reply.master = "0"
                            }
                            //缓存
                            cache[reply.id] = reply
                        }
                        res.addAll(it)
                    }
                    return res
                }
            }
        }.flow.cachedIn(viewModelScope)
    }

    //点击了文本中的连接
    fun onUrlClick(url: String) {
        if (url.startsWith("/t/")) {
            url.split("/").let {
                if (it.size > 2) {
                    sendMessage(MR.ThreadDetailActivity_gotoSelf, it[2])
                }
            }
        } else if (url.startsWith(">>No.")) {
            //引用串
            val id = url.split(".")[1]
            cache[id]?.let {
                sendMessage(MR.ThreadDetailActivity_showLinkArticle, it)
            }
        }
    }

}