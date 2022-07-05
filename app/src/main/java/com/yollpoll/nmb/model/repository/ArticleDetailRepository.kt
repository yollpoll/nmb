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
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

const val PAGE_SIZE = 20

class ArticleDetailRepository @Inject constructor(@CommonRetrofitFactory val retrofitFactory: RetrofitFactory) :
    IRepository {
    private val service = retrofitFactory.createService(HttpService::class.java)

    suspend fun getArticleDetail(id: String, pos: Int) = service.getArticleDetail(id, pos)

//    //获取回复
//    fun getReplyPagerSource(id: String): NMBBasePagingSource<ArticleItem> {
//        return object : NMBBasePagingSource<ArticleItem>() {
//            override suspend fun load(pos: Int): List<ArticleItem> {
//                val data = service.getArticleDetail(id, pos)
//                val res = arrayListOf<ArticleItem>()
//                try {
//                    if (pos == 0) {
//                        val head = data.copy()
//                        head.master = "1"
//                        res.add(head)
//                    }
//                } catch (e: Exception) {
//                    e.message?.logI()
//                }
//
//                data.Replies?.let {
//                    it.forEach { reply ->
//                        if (reply.user_hash == data.user_hash) {
//                            reply.master = "1"
//                        } else {
//                            reply.master = "0"
//                        }
//                    }
//                    res.addAll(it)
//                }
//                return res
//            }
//        }
//    }

}