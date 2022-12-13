/*
 * Copyright (c) 2021. Lorem ipsum dolor sit amet, consectetur adipiscing elit.
 * Morbi non lorem porttitor neque feugiat blandit. Ut vitae ipsum eget quam lacinia accumsan.
 * Etiam sed turpis ac ipsum condimentum fringilla. Maecenas magna.
 * Proin dapibus sapien vel ante. Aliquam erat volutpat. Pellentesque sagittis ligula eget metus.
 * Vestibulum commodo. Ut rhoncus gravida arcu.
 */

package com.yollpoll.nmb.model.repository

import androidx.paging.*
import androidx.room.withTransaction
import com.yollpoll.base.NMBBasePagingSource
import com.yollpoll.base.PAGE_SIZE
import com.yollpoll.base.logE
import com.yollpoll.base.logI
import com.yollpoll.extensions.isNetConnected
import com.yollpoll.floweventbus.FlowEventBus
import com.yollpoll.framework.extensions.shortToast
import com.yollpoll.framework.extensions.toJson
import com.yollpoll.framework.extensions.toListJson
import com.yollpoll.framework.net.http.RetrofitFactory
import com.yollpoll.framework.paging.BasePagingSource
import com.yollpoll.framework.paging.START_INDEX
import com.yollpoll.framework.paging.getCommonPager
import com.yollpoll.nmb.App
import com.yollpoll.nmb.db.MainDB
import com.yollpoll.nmb.di.CommonRetrofitFactory
import com.yollpoll.nmb.model.bean.ArticleItem
import com.yollpoll.nmb.model.bean.ImgTuple
import com.yollpoll.nmb.net.HttpService
import com.yollpoll.nmb.net.getRequestBody
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import okhttp3.ResponseBody
import okio.IOException
import retrofit2.HttpException
import java.io.File
import javax.inject.Inject

class ArticleDetailRepository @Inject constructor(@CommonRetrofitFactory val retrofitFactory: RetrofitFactory) :
    IRepository {
    private val service = retrofitFactory.createService(HttpService::class.java)

    //获取串回复
    suspend fun getArticleReply(article: ArticleItem, pos: Int): List<ArticleItem> {
        if (!App.INSTANCE.isNetConnected()) {
            //没有网络直接取本地数据
            return MainDB.getInstance().getArticleDao().getReplies(article.id, pos).filter {
                return@filter it.id != "9999999"
            }
        }
        val replies = arrayListOf<ArticleItem>()
        //计算总页数
        val allPage = article.ReplyCount?.let {
            try {
                return@let 1 + (it.toInt()) / PAGE_SIZE
            } catch (e: Exception) {
                return@let 1
            }
        } ?: 1
        if (pos >= allPage) {
            //当前是最新一页就一定要去服务器拉取最新数据
            val data = (service.getArticleDetail(article.id, pos).Replies ?: emptyList()).filter {
                return@filter it.id != "9999999"
            }.map { reply ->
                reply.replyTo = article.id
                reply.page = pos
                reply
            }
            //添加缓存
            MainDB.getInstance().getArticleDao().insertAll(data)
            replies.addAll(data)
        } else {
            var data = MainDB.getInstance().getArticleDao().getReplies(article.id, pos).filter {
                return@filter it.id != "9999999"
            }
            if (data.isEmpty()) {
                //当前不是最后一页且本地没有数据，再去网络拉取
                data = (service.getArticleDetail(article.id, pos).Replies ?: emptyList()).filter {
                    return@filter it.id != "9999999"
                }.map { reply ->
                    reply.replyTo = article.id
                    reply.page = pos
                    reply
                }
                //缓存网络数据
                MainDB.getInstance().getArticleDao().insertAll(data)
            }
            replies.addAll(data)
        }
        return replies
    }

    //获取串内容
    suspend fun getArticle(id: String): ArticleItem {
        //本地数据库内
        return MainDB.getInstance().getArticleDao().getArticle(id)
            ?: service.getArticleDetail(id, 1).apply {
                //服务器数据
                //缓存到本地
                MainDB.getInstance().getArticleDao().insertAll(listOf(this))
            }
    }

    fun getImagesFlow(id: String): Flow<List<ImgTuple>> =
        MainDB.getInstance().getArticleDao().getImageFlow(id).map { list ->
            return@map list.filter {
                if (it.img.isNotEmpty()) {
                    return@filter true
                }
                return@filter false
            }
        }

    suspend fun getImagesList(id: String): List<ImgTuple> =
        MainDB.getInstance().getArticleDao().getImageList(id).filter { it ->
            if (it.img.isNotEmpty()) {
                return@filter true
            }
            return@filter false
        }


    suspend fun newThread(
        fid: String,//板块id
        name: String,
        title: String,
        email: String,
        content: String,
        water: String,
        file: File?
    ) {
        "newThread".logI()
        return service.newThread(
            getRequestBody(fid),
            getRequestBody(name),
            getRequestBody(title),
            getRequestBody(email),
            getRequestBody(content),
            getRequestBody(water),
            getRequestBody(file)
        )
    }

    //回复
    suspend fun reply(
        reply: String,
        name: String,
        title: String,
        email: String,
        content: String,
        water: String,
        file: File?
    ): ResponseBody {
        "newThread".logI()
        return service.replyThread(
            getRequestBody(reply),
            getRequestBody(content),
            getRequestBody(name),
            getRequestBody(title),
            getRequestBody(email),
            getRequestBody(water),
            getRequestBody(file)
        )
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

    //加载缓存整个串
    suspend fun startLoadReply(id: String) {
        val pageSet = hashSetOf<Int>()
        MainDB.getInstance().getArticleDao().getAllReplyPage(id).map {
            return@map it.page
        }.forEach {
            pageSet.add(it)
        }
        val article = getArticle(id)
        val allPage = 1 + ((article.ReplyCount?.toInt() ?: 0) / PAGE_SIZE)
        val loadPage = arrayListOf<Int>()//未加载的页数
        //1-allPage
        for (i: Int in 1..allPage) {
            if (!pageSet.contains(i)) {
                loadPage.add(i)
            }
        }
        run loop@{
            loadPage.forEach { index ->
                //加载并缓存
                val data = (service.getArticleDetail(id, index).Replies ?: emptyList()).filter {
                    return@filter it.id != "9999999"
                }.map { reply ->
                    reply.replyTo = article.id
                    reply.page = index
                    reply
                }
                //添加缓存
                MainDB.getInstance().getArticleDao().insertAll(data)
            }
        }
    }

    //加载最后一页数据
    suspend fun loadLastPage(id: String) {
        val article = MainDB.getInstance().getArticleDao().getArticle(id)
        val allPage = 1 + ((article?.page ?: 0) / PAGE_SIZE)
        service.getArticleDetail(id, allPage)
    }

}
