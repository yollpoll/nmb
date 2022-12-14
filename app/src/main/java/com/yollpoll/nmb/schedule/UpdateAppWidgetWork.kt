package com.yollpoll.nmb.schedule

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.*
import com.yollpoll.nmb.App
import com.yollpoll.nmb.di.AppWidgetRepository
import com.yollpoll.nmb.model.repository.HomeRepository
import com.yollpoll.nmb.view.widgets.updateAppWidget
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import kotlin.concurrent.timerTask
import kotlin.coroutines.suspendCoroutine

const val INTERVAL_TIME: Long = 20

@HiltWorker
class UpdateAppWidgetWork @AssistedInject constructor(
    @Assisted appContext: Context, @Assisted workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {
    @Inject
    @AppWidgetRepository
    lateinit var homeRepository: HomeRepository
    var job: Job? = null

    override suspend fun doWork(): Result = withContext<Result>(Dispatchers.IO) {
        //更新串
        val list = homeRepository.getTimeLine(1, 1)
        if (list.size == 0) return@withContext Result.success()
        val interval = INTERVAL_TIME / list.size
        for (i in 0 until list.size) {
            updateAppWidget(applicationContext, list[i])
//            delay((interval * 60_000).toLong())
            delay(60_000)
        }
        return@withContext Result.success()
    }

//    override fun onStopped() {
//        super.onStopped()
//        job?.cancel()
//    }
}

fun startWork() {
    val workRequest = PeriodicWorkRequestBuilder<UpdateAppWidgetWork>(
        INTERVAL_TIME, TimeUnit.MINUTES
    ).addTag("updateAppWidget")
//        .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
        .build()
    WorkManager.getInstance(App.INSTANCE)
        .enqueueUniquePeriodicWork("updateAppWidget", ExistingPeriodicWorkPolicy.KEEP, workRequest)
}

fun stopWork() {
    WorkManager.getInstance(App.INSTANCE).cancelAllWorkByTag("updateAppWidget")
}