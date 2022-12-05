package com.yollpoll.nmb.model.repository

import com.yollpoll.base.*
import com.yollpoll.framework.net.http.RetrofitFactory
import com.yollpoll.nmb.db.MainDB
import com.yollpoll.nmb.di.CommonRetrofitFactory
import com.yollpoll.nmb.model.bean.Article
import com.yollpoll.nmb.model.bean.ArticleItem
import com.yollpoll.nmb.model.bean.ShieldArticle
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
                return getThreadList(id, pos).apply {
                    MainDB.getInstance().getArticleDao().insertAll(this)
                }.filter {
                    return@filter !getShieldThread().contains(it.id)
                }
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
                return getTimeLine(pos, id).apply {
                    MainDB.getInstance().getArticleDao().insertAll(this)
                }.filter {
                    return@filter !getShieldThread().contains(it.id)
                }
            }
        }
    }

    //屏蔽板块列表
    suspend fun getShieldThread(): List<String> {
        return MainDB.getInstance().getArticleDao().getShieldArticle().map {
            it.articleId
        }
    }

    //插入屏蔽
    suspend fun addShieldThread(vararg id: String) {
        id.asList().map {
            ShieldArticle(it)
        }.let {
            MainDB.getInstance().getArticleDao().insertShield(it)
        }

    }

    //取消屏蔽
    suspend fun cancelShield(vararg id: String) {
        id.map {
            ShieldArticle(it)
        }.forEach {
            MainDB.getInstance().getArticleDao().deleteShieldByArticleId(it.articleId)
        }
    }

    //获取屏蔽的串
    suspend fun getShieldArticleList(): List<ArticleItem> {
//        "size:${MainDB.getInstance().getArticleDao().getShieldArticle().size}".logI()
        return MainDB.getInstance().getArticleDao().getShieldArticle().map {
//            "shield item: ${it.articleId}".logI()
            val article = MainDB.getInstance().getArticleDao().getShieldArticleList(it.articleId)
            return@map article
        }
    }

    suspend fun addArticle(articleItem: ArticleItem) {
        MainDB.getInstance().getArticleDao().insertAll(listOf(articleItem))
    }

    suspend fun getForumList() = service.getForumList()

    //获取封面真实地址
    suspend fun refreshCover() = service.refreshCover()

    //获取公告
    suspend fun getAnnouncement() = service.getAnnouncement()
}
