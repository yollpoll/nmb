package com.yollpoll.nmb.net

import com.yollpoll.nmb.model.bean.*
import okhttp3.Call
import okhttp3.RequestBody
import okhttp3.Response
import okhttp3.ResponseBody
import retrofit2.http.*

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
    suspend fun getTimeLine(@Query("id") id: Int, @Query("page") page: Int): Article//时间线

    @GET(ANNOUNCEMENT)
    suspend fun getAnnouncement(): Announcement//公告

    @GET(GET_CHILD_ARTICLE)
    suspend fun getArticleDetail(
        @Query("id") id: String,
        @Query("page") page: Int
    ): ArticleItem//获取串内容

    /**
     * 带图片
     *
     * @param fid
     * @param name
     * @param title
     * @param email
     * @param content
     * @param water
     * @param file
     * @return
     */
    @Multipart
    @POST(NEW_THREAD)
    suspend fun newThread(
        @Part("fid") fid: RequestBody?,
        @Part("name") name: RequestBody?,
        @Part("title") title: RequestBody?,
        @Part("email") email: RequestBody?,
        @Part("content") content: RequestBody?,
        @Part("water") water: RequestBody?,
        @Part("image\"; filename=\"a.jpg\"") file: RequestBody?
    )


    /**
     * 回复(帶圖片)
     *
     * @param resto
     * @param name
     * @param title
     * @param email
     * @param content
     * @param water
     * @return
     */
    @Multipart
    @POST(REPLY_THREAD)
    suspend fun replyThread(
        @Part("resto") resto: RequestBody?,
        @Part("content") content: RequestBody?,
        @Part("name") name: RequestBody?,
        @Part("title") title: RequestBody?,
        @Part("email") email: RequestBody?,
        @Part("water") water: RequestBody?,
        @Part("image\"; filename=\"a.jpg\"") file: RequestBody?
    )

    @GET("$COLLECTION/page/{page}/uuid/{uuid}")
    suspend fun getCollection(
        @Path("page") page: Int,
        @Path("uuid") uuid: String?
    ): List<ArticleItem>

    @GET("$ADD_COLLECTION/uuid/{uuid}/tid/{tid}")
    suspend fun addCollection(@Path("uuid") uuid: String?, @Path("tid") tid: String?): String

    @GET("$DEL_COLLECTION/uuid/{uuid}/tid/{tid}")
    suspend fun delCollection(@Path("uuid") uuid: String?, @Path("tid") tid: String?): String

    @GET(SPEAKING_HISTORY)
    suspend fun getSpeakingHistory():ResponseBody
}