package com.yollpoll.nmb.model.repository

import android.provider.Settings
import androidx.paging.*
import androidx.room.withTransaction
import com.yollpoll.base.logI
import com.yollpoll.framework.extensions.getString
import com.yollpoll.framework.extensions.toJsonBean
import com.yollpoll.framework.net.http.RetrofitFactory
import com.yollpoll.framework.utils.getString
import com.yollpoll.framework.utils.putString
import com.yollpoll.nmb.App
import com.yollpoll.nmb.KEY_COLLECTION_ID
import com.yollpoll.nmb.db.MainDB
import com.yollpoll.nmb.db.MySpeechDao
import com.yollpoll.nmb.di.CommonRetrofitFactory
import com.yollpoll.nmb.model.bean.ArticleItem
import com.yollpoll.nmb.model.bean.HistoryBean
import com.yollpoll.nmb.model.bean.MySpeechBean
import com.yollpoll.nmb.net.HttpService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.Response
import retrofit2.HttpException
import java.io.IOException
import java.nio.charset.Charset
import javax.inject.Inject

class UserRepository @Inject constructor(@CommonRetrofitFactory val retrofitFactory: RetrofitFactory) {
    private val service = retrofitFactory.createService(HttpService::class.java)

    //请求一次以后服务端会清空之前的数据
    suspend fun getSpeakingHistory(): MutableList<MySpeechBean> {
        val body = service.getSpeakingHistory()
        val json = String(body.bytes())
        var bean: MySpeechBean? = null
        try {
            bean = json.toJsonBean()
        } catch (e: Exception) {
        }
        return if (null != bean) {
            arrayListOf(bean)
        } else {
            arrayListOf()
        }
    }

    //保存到数据库
    suspend fun saveSpeechToLocal(bean: List<MySpeechBean>) {
        MainDB.getInstance().getSpeechDao().insertAll(bean)
    }

    //从数据库加载
    suspend fun loadSpeechFromLocal(): List<MySpeechBean> {
        return if (App.INSTANCE.cookie?.cookie != null) {
            return MainDB.getInstance().getSpeechDao().query()
        } else {
            arrayListOf()
        }
    }

    //清空数据
    suspend fun clearLocalSpeech() {
        MainDB.getInstance().getSpeechDao().clearAll()
    }

    suspend fun collect(uuid: String, tid: String): String {
        return service.addCollection(uuid, tid)
    }

    suspend fun delCollection(uuid: String, tid: String): String {
        return service.delCollection(uuid, tid)
    }

    suspend fun getCollection(page: Int, uuid: String): List<ArticleItem> {
        return service.getCollection(page, uuid)
    }

    suspend fun getHistory(): List<HistoryBean> {
        return MainDB.getInstance().getHistoryDao().query()
    }

    suspend fun delHistory(bean: HistoryBean) {
        MainDB.getInstance().getHistoryDao().delete(bean)
    }

    suspend fun clearHistory() {
        MainDB.getInstance().getHistoryDao().clearAll()
    }

    suspend fun addHistory(vararg list: HistoryBean) {
        MainDB.getInstance().getHistoryDao().insertAll(list.asList())
    }

    //更新订阅ID
    fun updateCollectionId(id: String) {
        putString(KEY_COLLECTION_ID, id)
    }

    //获取订阅ID
    fun getCollectionId(): String {
        return getString(
            KEY_COLLECTION_ID,
            Settings.System.getString(App.INSTANCE.contentResolver, Settings.Secure.ANDROID_ID)
        )!!
    }

}