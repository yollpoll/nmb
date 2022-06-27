package com.yollpoll.log

import android.content.Context
import androidx.work.*
import java.text.SimpleDateFormat
import java.util.*

/**
 * Created by spq on 2022/1/14
 */

const val DIVIDER_LINE = "****************************************************************"
const val DATA_FORMAT = "yyyy-MM-dd hh:mm:ss"

class LogWorker(val context: Context, workerParam: WorkerParameters) :
    Worker(context, workerParam) {
    override fun doWork(): Result {
        val name = inputData.getString("name")
        var content = inputData.getString("content")

        val sdf = SimpleDateFormat(DATA_FORMAT)
        val data = "[${sdf.format(Date())}]\n"
        content = data + content + "\n\n$DIVIDER_LINE\n\n"
        if (LogTools.saveLog(name, content, context)) {
            return Result.success()
        } else {
            return Result.failure()
        }
    }

}

object LogWorkerManager {
    fun enqueue(name: String, content: String, context: Context) {
        val saveLogRequest =
            OneTimeWorkRequestBuilder<LogWorker>().setInputData(
                Data.Builder().putString("name", name)
                    .putString("content", content).build()
            )
                .build()

        WorkManager.getInstance(context).enqueue(saveLogRequest)
    }
}