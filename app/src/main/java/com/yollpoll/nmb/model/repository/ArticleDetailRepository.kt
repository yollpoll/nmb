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
import com.yollpoll.base.logI
import com.yollpoll.framework.extensions.shortToast
import com.yollpoll.framework.extensions.toJson
import com.yollpoll.framework.extensions.toListJson
import com.yollpoll.framework.net.http.RetrofitFactory
import com.yollpoll.framework.paging.BasePagingSource
import com.yollpoll.framework.paging.START_INDEX
import com.yollpoll.framework.paging.getCommonPager
import com.yollpoll.nmb.db.MainDB
import com.yollpoll.nmb.di.CommonRetrofitFactory
import com.yollpoll.nmb.model.bean.ArticleItem
import com.yollpoll.nmb.net.HttpService
import com.yollpoll.nmb.net.getRequestBody
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import okhttp3.ResponseBody
import okio.IOException
import retrofit2.HttpException
import java.io.File
import javax.inject.Inject

const val PAGE_SIZE = 20

class ArticleDetailRepository @Inject constructor(@CommonRetrofitFactory val retrofitFactory: RetrofitFactory) :
    IRepository {
    private val service = retrofitFactory.createService(HttpService::class.java)

    //获取串回复
    suspend fun getArticleReply(article: ArticleItem, pos: Int): List<ArticleItem> {
        var replies = MainDB.getInstance().getArticleDao().getReplies(article.id, pos)
        if (replies.isEmpty()) {
            "load from net".logI()
            //本地数据库内无数据,从网络加载
            replies = service.getArticleDetail(article.id, pos).Replies ?: emptyList()
            replies.forEach { reply ->
                reply.replyTo = article.id
                reply.page = pos
            }
            MainDB.getInstance().getArticleDao().insertAll(replies)
        }else{
            "load from db".logI()
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


    suspend fun newThread(
        fid: String,//板块id
        name: String,//板块名字
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

}
