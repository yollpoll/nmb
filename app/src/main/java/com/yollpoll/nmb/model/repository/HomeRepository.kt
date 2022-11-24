package com.yollpoll.nmb.model.repository

import com.yollpoll.base.*
import com.yollpoll.framework.net.http.RetrofitFactory
import com.yollpoll.nmb.di.CommonRetrofitFactory
import com.yollpoll.nmb.model.bean.Article
import com.yollpoll.nmb.model.bean.ArticleItem
import com.yollpoll.nmb.net.HttpService
import javax.inject.Inject


class HomeRepository @Inject constructor(@CommonRetrofitFactory val retrofitFactory: RetrofitFactory) :
    IRepository {
    private val service = retrofitFactory.createService(HttpService::class.java)

    /**
     * 获取串列表
     */
    suspend fun getThreadList(id: String, page: Int): Article {
        try {
            return service.getThreadList(id, page)
        } catch (e: Exception) {

        }
        return Article()
    }

    /**
     * 时间线
     */
    suspend fun getTimeLine(page: Int, id: Int): Article {
        try {
            return service.getTimeLine(page = page, id = id)
        } catch (e: Exception) {

        }
        return Article()
    }


    /**
     * 获取串列表的pagingSource
     */
    fun getThreadsPagingSource(id: String): NMBBasePagingSource<ArticleItem> {
        return object : NMBBasePagingSource<ArticleItem>(selectedPage = {
            START_INDEX//返回第一页
        }) {
            override suspend fun load(pos: Int): List<ArticleItem> {
                return getThreadList(id, pos)
            }
        }
    }

    /**
     * 获取时间线
     */
    fun getTimeLinePagingSource(id: Int): NMBBasePagingSource<ArticleItem> {
        return object : NMBBasePagingSource<ArticleItem>(selectedPage = {
            //加载失败的时候刷新返回第一页
            START_INDEX//返回第一页
        }) {
            override suspend fun load(pos: Int): List<ArticleItem> {
                return getTimeLine(pos, id)
            }
        }
    }


    suspend fun getForumList() = service.getForumList()

    //获取封面真实地址
    suspend fun refreshCover() = service.refreshCover()

    //获取公告
    suspend fun getAnnouncement() = service.getAnnouncement()

}
