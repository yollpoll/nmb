/*
 * Copyright (c) 2021. Lorem ipsum dolor sit amet, consectetur adipiscing elit.
 * Morbi non lorem porttitor neque feugiat blandit. Ut vitae ipsum eget quam lacinia accumsan.
 * Etiam sed turpis ac ipsum condimentum fringilla. Maecenas magna.
 * Proin dapibus sapien vel ante. Aliquam erat volutpat. Pellentesque sagittis ligula eget metus.
 * Vestibulum commodo. Ut rhoncus gravida arcu.
 */

package com.yollpoll.nmb.model.repository

import com.yollpoll.base.NMBBasePagingSource
import com.yollpoll.base.logI
import com.yollpoll.framework.extensions.toJson
import com.yollpoll.framework.extensions.toListJson
import com.yollpoll.framework.net.http.RetrofitFactory
import com.yollpoll.framework.paging.BasePagingSource
import com.yollpoll.framework.paging.START_INDEX
import com.yollpoll.nmb.di.CommonRetrofitFactory
import com.yollpoll.nmb.model.bean.ArticleItem
import com.yollpoll.nmb.net.HttpService
import com.yollpoll.nmb.net.getRequestBody
import kotlinx.coroutines.flow.flow
import okhttp3.ResponseBody
import java.io.File
import javax.inject.Inject

const val PAGE_SIZE = 20

class ArticleDetailRepository @Inject constructor(@CommonRetrofitFactory val retrofitFactory: RetrofitFactory) :
    IRepository {
    private val service = retrofitFactory.createService(HttpService::class.java)

    suspend fun getArticleDetail(id: String, pos: Int) = service.getArticleDetail(id, pos)

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
    ) {
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

    suspend fun getCollection(page: Int, uuid: String) :List<ArticleItem>{
        return service.getCollection(page,uuid)
    }

}