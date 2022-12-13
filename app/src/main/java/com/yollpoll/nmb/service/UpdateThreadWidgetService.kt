package com.yollpoll.nmb.service

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import com.yollpoll.base.logI
import com.yollpoll.framework.extensions.getInt
import com.yollpoll.framework.extensions.getIntWithFLow
import com.yollpoll.framework.extensions.getString
import com.yollpoll.nmb.App
import com.yollpoll.nmb.KEY_FOCUS
import com.yollpoll.nmb.db.MainDB
import com.yollpoll.nmb.di.AppWidgetRepository
import com.yollpoll.nmb.model.repository.ArticleDetailRepository
import com.yollpoll.nmb.model.repository.HomeRepository
import com.yollpoll.nmb.view.widgets.updateAppWidget
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import javax.inject.Inject
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.suspendCoroutine

const val UPDATE_TIME :Long= 5_000_000

//更新小组件数据
@AndroidEntryPoint
class UpdateThreadWidgetService : Service() {
    @Inject
    @AppWidgetRepository
    lateinit var homeRepository: HomeRepository

    private var job: Job? = null
    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        "onStartCommand".logI()
        if (job != null && job!!.isActive) {
            job?.cancel()
        }
        job = GlobalScope.launch(Dispatchers.IO) {
            //更新串
            val list = homeRepository.getTimeLine(1, 1)
            if (list.size == 0) return@launch
            withContext(Dispatchers.Main) {
                var index = 0
                while (true) {
                    updateAppWidget(this@UpdateThreadWidgetService, list[index])
                    delay(5_000)
                    index = (index + 1) % list.size
                }
            }

        }
        return START_NOT_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        job?.cancel()
    }
}
