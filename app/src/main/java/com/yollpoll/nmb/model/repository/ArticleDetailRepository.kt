/*
 * Copyright (c) 2021. Lorem ipsum dolor sit amet, consectetur adipiscing elit.
 * Morbi non lorem porttitor neque feugiat blandit. Ut vitae ipsum eget quam lacinia accumsan.
 * Etiam sed turpis ac ipsum condimentum fringilla. Maecenas magna.
 * Proin dapibus sapien vel ante. Aliquam erat volutpat. Pellentesque sagittis ligula eget metus.
 * Vestibulum commodo. Ut rhoncus gravida arcu.
 */

package com.yollpoll.nmb.model.repository

import com.yollpoll.framework.net.http.RetrofitFactory
import com.yollpoll.framework.paging.BasePagingSource
import com.yollpoll.framework.paging.START_INDEX
import com.yollpoll.nmb.model.bean.ArticleItem
import com.yollpoll.nmb.model.bean.Reply
import com.yollpoll.nmb.net.HttpService
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

const val PAGE_SIZE = 20

class ArticleDetailRepository @Inject constructor(val retrofitFactory: RetrofitFactory) :
    IRepository {
    private val service = retrofitFactory.createService(HttpService::class.java)

    //获取回复
    fun getReply(id: String): BasePagingSource<Reply> {
        return object : BasePagingSource<Reply>() {
            override suspend fun load(pos: Int): List<Reply> {
                service.getArticleDetail(id, pos).let {
                    return if ((it.replyCount.toInt()) < pos * PAGE_SIZE) {
                        arrayListOf()
                    } else {
                        it.replys
                    }
                }
            }
        }
    }

    //获取详情
    fun getArticleDetail(id: String) = flow<ArticleItem?> {
        try {
            emit(service.getArticleDetail(id, START_INDEX))
        } catch (e: Exception) {
            emit(null)
        }
    }
}