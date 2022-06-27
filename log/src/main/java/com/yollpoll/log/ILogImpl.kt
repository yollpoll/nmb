package com.yollpoll.log

import android.content.Context
import com.yollpoll.ilog.Ilog
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

/**
 * Created by spq on 2022/1/14
 */
class ILogImpl @Inject constructor(@ApplicationContext val context: Context) : Ilog {
    override fun saveLog(fileName: String, content: String) {
        LogWorkerManager.enqueue(fileName, content, context)
    }
}