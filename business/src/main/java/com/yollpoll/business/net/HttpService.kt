package com.yollpoll.business.net

import com.yollpoll.business.model.bean.Article
import com.yollpoll.business.model.bean.ArticleItem
import com.yollpoll.business.model.bean.ForumList
import okhttp3.ResponseBody
import retrofit2.http.GET
import retrofit2.http.Query

interface HttpService {
    @GET(ROOT_URL)
    suspend fun getRealUrl(): List<String>

    @GET(FORUM_LIST)
    suspend fun getForumList(): ForumList

    @GET(COVER)
    suspend fun refreshCover(): ResponseBody

    @GET(GET_ARTICLE)
    suspend fun getThreadList(@Query("id") id: String, @Query("page") page: Int): Article//获取串列表

    @GET(TIME_LINE)
    suspend fun getTimeLine(@Query("page") page: Int): Article//时间线

    @GET(GET_CHILD_ARTICLE)
    suspend fun getArticleDetail(
        @Query("id") id: String,
        @Query("page") page: Int
    ): ArticleItem//获取串内容

}