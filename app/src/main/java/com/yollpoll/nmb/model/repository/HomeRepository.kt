package com.yollpoll.nmb.model.repository

import android.util.Log
import com.yollpoll.base.TAG
import com.yollpoll.base.logE
import com.yollpoll.framework.extensions.toListJson
import com.yollpoll.framework.net.http.RetrofitFactory
import com.yollpoll.framework.paging.BasePagingSource
import com.yollpoll.nmb.di.CommonRetrofitFactory
import com.yollpoll.nmb.di.HomeRepositoryAnnotation
import com.yollpoll.nmb.model.bean.ArticleItem
import com.yollpoll.nmb.net.HttpService
import javax.inject.Inject


class HomeRepository @Inject constructor(@CommonRetrofitFactory val retrofitFactory: RetrofitFactory) :
    IRepository {
    private val service = retrofitFactory.createService(HttpService::class.java)

    /**
     * 获取串列表
     */
    suspend fun getThreadList(id: String, page: Int) = service.getThreadList(id, page)

    /**
     * 时间线
     */
    suspend fun getTimeLine(page: Int) = service.getTimeLine(page = page)


    /**
     * 获取串列表的pagingSource
     */
    fun getThreadsPagingSource(id: String): BasePagingSource<ArticleItem> {
        return object : BasePagingSource<ArticleItem>() {
            override suspend fun load(pos: Int): List<ArticleItem> {
                Log.d(TAG, "load: thread id $id")
                return getThreadList(id, pos)
            }
        }
//        return ThreadSource(this, id)
    }

    /**
     * 获取时间线
     */
    fun getTimeLinePagingSource(): BasePagingSource<ArticleItem> {
        return object : BasePagingSource<ArticleItem>() {
            override suspend fun load(pos: Int): List<ArticleItem> {
                val data=getTimeLine(pos)
                "asasasas".logE()
                "loadTimeLine: ${data.toListJson()}".logE()
                return data
            }
        }
//        return TimeLineSource(this)
    }

    class ThreadSource(val repository: HomeRepository, val id: String) :
        BasePagingSource<ArticleItem>() {
        override suspend fun load(pos: Int): List<ArticleItem> {
            return repository.getThreadList(id, pos)
        }
    }

    class TimeLineSource(val repository: HomeRepository) : BasePagingSource<ArticleItem>() {
        override suspend fun load(pos: Int): List<ArticleItem> {
            return repository.getTimeLine(pos)
        }

    }

    suspend fun getForumList() = service.getForumList()

}
