package com.yollpoll.nmb.service

import android.app.Service
import android.content.Intent
import android.os.IBinder
import com.yollpoll.arch.message.MessageManager
import com.yollpoll.base.logE
import com.yollpoll.base.logI
import com.yollpoll.extensions.isNetConnected
import com.yollpoll.floweventbus.FlowEventBus
import com.yollpoll.framework.extensions.shortToast
import com.yollpoll.nmb.ACTION_UPDATE_THREAD_DETAIL
import com.yollpoll.nmb.ACTION_UPDATE_THREAD_DETAIL_CANCEL
import com.yollpoll.nmb.App
import com.yollpoll.nmb.db.MainDB
import com.yollpoll.nmb.model.repository.ArticleDetailRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectLatest
import javax.inject.Inject

/**
 * Created by spq on 2022/12/7
 */
@AndroidEntryPoint
class ThreadReplyService : Service() {
    @Inject
    lateinit var repository: ArticleDetailRepository
    val map = hashMapOf<String, Job>()
    lateinit var task: Job
    override fun onBind(intent: Intent?): IBinder? {
        "ThreadReply On Bind".logI()
        GlobalScope.launch {
            FlowEventBus.getFlow<String>(ACTION_UPDATE_THREAD_DETAIL_CANCEL).collect { id ->
                "ThreadReply cancel on event:$id".logI()
                if (map.containsKey(id)) {
                    map[id]?.cancel()
                    map.remove(id)
                }
            }
        }
        task = GlobalScope.launch(Dispatchers.IO) {
            FlowEventBus.getFlow<String>(ACTION_UPDATE_THREAD_DETAIL).collect { id ->
                "ThreadReply on event:$id".logI()
                if (map.containsKey(id)) {
                    return@collect
                }
                if (App.INSTANCE.isNetConnected()) {
                    //网络判断
                    val loadJob = this.launch {
                        repository.startLoadReply(id)
                    }
                    map[id] = loadJob
                }
            }

        }

        return null
    }

    override fun onUnbind(intent: Intent?): Boolean {
        "ThreadReply on Unbind".logI()
        map.forEach {
            if (!it.value.isCancelled) {
                it.value.cancel()
            }
        }
        task.cancel()
        return super.onUnbind(intent)
    }
}