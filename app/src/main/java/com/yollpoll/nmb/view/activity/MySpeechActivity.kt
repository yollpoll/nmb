package com.yollpoll.nmb.view.activity

import android.app.Application
import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.lifecycle.asLiveData
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewModelScope
import androidx.paging.cachedIn
import androidx.paging.filter
import androidx.paging.flatMap
import androidx.recyclerview.widget.LinearLayoutManager
import com.yollpoll.annotation.annotation.Route
import com.yollpoll.base.*
import com.yollpoll.framework.dispatch.DispatchRequest
import com.yollpoll.framework.extensions.toJson
import com.yollpoll.framework.fast.FastViewModel
import com.yollpoll.nmb.BR
import com.yollpoll.nmb.R
import com.yollpoll.nmb.databinding.ActivityMySpeechBinding
import com.yollpoll.nmb.databinding.ItemSpeechBinding
import com.yollpoll.nmb.db.MainDB
import com.yollpoll.nmb.model.bean.MySpeechBean
import com.yollpoll.nmb.model.repository.UserRepository
import com.yollpoll.nmb.router.DispatchClient
import com.yollpoll.nmb.router.ROUTE_MY_SPEECH
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

suspend fun gotoMySpeechActivity(context: Context) {
    DispatchClient.manager?.dispatch(context, DispatchRequest.UrlBuilder(ROUTE_MY_SPEECH).build())
}

@AndroidEntryPoint
@Route(url = ROUTE_MY_SPEECH)
class MySpeechActivity : NMBActivity<ActivityMySpeechBinding, MySpeechVm>() {
    private val vm: MySpeechVm by viewModels()
    override fun getLayoutId() = R.layout.activity_my_speech
    override fun initViewModel() = vm
    val adapter = BaseAdapter<MySpeechBean>(
        R.layout.item_speech,
        BR.bean,
        onBindViewHolder = { item, _, vh ->
            val binding:ItemSpeechBinding= vh.binding as ItemSpeechBinding
            "onBind".logI()
            item?.resto?.apply {
                binding.tvReply.visibility= View.VISIBLE
                val replyTo=">>No.${item.resto}"
                binding.tvReply.text=replyTo
            }?:apply {
                binding.tvReply.visibility= View.GONE
            }
            binding.llRoot.setOnClickListener{
                lifecycleScope.launch {
                    if(item?.resto==null){
                        ThreadDetailActivity.gotoThreadDetailActivity(item?.id.toString(),context)
                    }else{
                        ThreadDetailActivity.gotoThreadDetailActivity(item.resto.toString(),context)
                    }
                }
            }
        },
        contentSame = { item1, item2 ->
            return@BaseAdapter item1.id == item2.id
        }
    )
    val rvManager = LinearLayoutManager(context)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initView()
        initData()
    }

    private fun initView() {
        initTitle(mDataBinding.layoutTitle.toolbar, true)
    }

    private fun initData() {
        vm.loadMySpeech()
        lifecycleScope.launchWhenResumed {
            vm.speechLiveData.observe(this@MySpeechActivity) {
                adapter.submitData(it)
            }
        }
    }
}

@HiltViewModel
class MySpeechVm @Inject constructor(val app: Application, val repository: UserRepository) :
    FastViewModel(app) {
    private val speechFLow = MutableSharedFlow<List<MySpeechBean>>()
    val speechLiveData = speechFLow.asLiveData()

    fun loadMySpeech() {
        viewModelScope.launch {
            val list = repository.getSpeakingHistory()
            val localList=repository.loadSpeechFromLocal()
            list.addAll(localList)
            speechFLow.emit(list)
            repository.clearLocalSpeech()
            repository.saveSpeechToLocal(list)
        }
    }
}