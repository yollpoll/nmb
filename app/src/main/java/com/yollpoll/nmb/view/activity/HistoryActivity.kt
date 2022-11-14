package com.yollpoll.nmb.view.activity

import android.app.AlertDialog
import android.app.Application
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.text.Html
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.activity.viewModels
import androidx.core.text.HtmlCompat
import androidx.lifecycle.asLiveData
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewModelScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.yollpoll.annotation.annotation.Route
import com.yollpoll.base.BaseAdapter
import com.yollpoll.base.CommonDialog
import com.yollpoll.base.NMBActivity
import com.yollpoll.base.logI
import com.yollpoll.framework.dispatch.DispatchRequest
import com.yollpoll.framework.extensions.shortToast
import com.yollpoll.framework.fast.FastViewModel
import com.yollpoll.nmb.BR
import com.yollpoll.nmb.R
import com.yollpoll.nmb.databinding.ActivityHistoryBinding
import com.yollpoll.nmb.databinding.ItemHistoryBinding
import com.yollpoll.nmb.databinding.ItemSpeechBinding
import com.yollpoll.nmb.model.bean.HistoryBean
import com.yollpoll.nmb.model.repository.UserRepository
import com.yollpoll.nmb.router.DispatchClient
import com.yollpoll.nmb.router.ROUTE_AUTHOR
import com.yollpoll.nmb.router.ROUTE_HISTORY
import com.yollpoll.nmb.view.widgets.init
import com.yollpoll.utils.TransFormContent
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Created by spq on 2022/11/11
 */
@Route(url = ROUTE_HISTORY)
@AndroidEntryPoint
class HistoryActivity : NMBActivity<ActivityHistoryBinding, HistoryVm>() {
    private val vm: HistoryVm by viewModels()

    override fun getLayoutId() = R.layout.activity_history

    override fun initViewModel() = vm

    val mManager = LinearLayoutManager(this)
    val adapter = BaseAdapter<HistoryBean>(
        R.layout.item_history,
        BR.bean,
        onBindViewHolder = { item, pos, vh ->
            val binding: ItemHistoryBinding = vh.binding as ItemHistoryBinding
            item?.resto?.apply {
                binding.tvReply.visibility = View.VISIBLE
                val replyTo = ">>No.${item.resto}"
                binding.tvReply.text = replyTo
            } ?: apply {
                binding.tvReply.visibility = View.GONE
            }
            binding.llRoot.setOnClickListener {
                lifecycleScope.launch {
                    if (item?.resto == null) {
                        ThreadDetailActivity.gotoThreadDetailActivity(item?.id.toString(), context)
                    } else {
                        ThreadDetailActivity.gotoThreadDetailActivity(
                            item.resto.toString(),
                            context
                        )
                    }
                }
            }
            binding.llRoot.setOnLongClickListener {
                CommonDialog("删除记录", "确认删除浏览记录吗", it.context) {
                    vm.delHistory(pos)
                }.show()
                true
            }
            val htmlContent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                Html.fromHtml(item?.content,
                    HtmlCompat.FROM_HTML_MODE_COMPACT,
                    {
//                            "image: ${it}".logE()
                        null
                    }
                ) { opening, tag, output, xmlReader ->
//                        "tag: ${tag} outPut ${output}".logE()
                }
            } else {
                Html.fromHtml(item?.content)
            }
            //文本修改为自定义编辑的模式
            TransFormContent.trans(
                htmlContent,
                binding.tvContent,
            )
        },
        contentSame = { item1, item2 ->
            return@BaseAdapter item1.id == item2.id
        }
    )

    override fun getMenuLayout() = R.menu.menu_history

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if(item.itemId==R.id.action_clear_history){
            vm.clearHistory()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initView()
        initData()
    }

    private fun initView() {
        mDataBinding.swipeRefresh.isRefreshing = true
        initTitle(mDataBinding.layoutTitle.toolbar, true)
        mDataBinding.swipeRefresh.init(context) {
            vm.loadHistory()
            if (!mDataBinding.swipeRefresh.isRefreshing) {
                mDataBinding.swipeRefresh.isRefreshing = true
            }
        }
    }

    private fun initData() {
        lifecycleScope.launchWhenResumed {
            vm.speechLiveData.observe(this@HistoryActivity) {
                mDataBinding.swipeRefresh.isRefreshing = false
                adapter.submitData(it)
            }
        }
    }

}

@HiltViewModel
class HistoryVm @Inject constructor(app: Application, val repository: UserRepository) :
    FastViewModel(app) {
    val title = "浏览记录"
    private val historyFlow = MutableSharedFlow<List<HistoryBean>>()
    val speechLiveData = historyFlow.asLiveData()

    init {
        loadHistory()
    }

    fun loadHistory() {
        viewModelScope.launch {
            val list = repository.getHistory()
            historyFlow.emit(list)
        }
    }

    fun delHistory(pos: Int) {
        viewModelScope.launch {
            speechLiveData.value!![pos].let {
                repository.delHistory(it)
                loadHistory()
            }
        }
    }
    fun clearHistory(){
        viewModelScope.launch {
            repository.clearHistory()
            loadHistory()
        }
    }
}

suspend fun gotoHistory(context: Context) {
    val req = DispatchRequest.RequestBuilder().host("nmb").module("history").build()
    DispatchClient.manager?.dispatch(context, req)
}