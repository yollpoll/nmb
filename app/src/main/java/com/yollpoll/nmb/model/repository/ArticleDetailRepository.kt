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
import kotlinx.coroutines.flow.flow
import okhttp3.ResponseBody
import okio.IOException
import retrofit2.HttpException
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

    @ExperimentalPagingApi
    fun getArticlePager(id: String): Pager<Int, ArticleItem> {
        return Pager(
            config = PagingConfig(50),
            remoteMediator = ArticleRemoteMediator(id, MainDB.getInstance(), service)
        ) {
            MainDB.getInstance().getArticleDao().pagingSource(id)
        }
    }

}

@ExperimentalPagingApi
class ArticleRemoteMediator(
    private val id: String,
    private val database: MainDB,
    private val networkService: HttpService
) : RemoteMediator<Int, ArticleItem>() {
    private val dao = database.getArticleDao()
    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Int, ArticleItem>
    ): MediatorResult {
        return try {
            // The network load method takes an optional after=<user.id>
            // parameter. For every page after the first, pass the last user
            // ID to let it continue from where it left off. For REFRESH,
            // pass null to load the first page.
            val page = when (loadType) {
                LoadType.REFRESH -> 1
                // In this example, you never need to prepend, since REFRESH
                // will always load the first page in the list. Immediately
                // return, reporting end of pagination.
                LoadType.PREPEND -> {
                    val lastItem = state.lastItemOrNull()
                        ?: return MediatorResult.Success(
                            endOfPaginationReached = true
                        )
                    lastItem.page - 1
                }


                LoadType.APPEND -> {
                    val lastItem = state.lastItemOrNull()
                        ?: return MediatorResult.Success(
                            endOfPaginationReached = true
                        )

                    // You must explicitly check if the last item is null when
                    // appending, since passing null to networkService is only
                    // valid for initial load. If lastItem is null it means no
                    // items were loaded after the initial REFRESH and there are
                    // no more items to load.

                    lastItem.page + 1
                }
            }
            // Suspending network load via Retrofit. This doesn't need to be
            // wrapped in a withContext(Dispatcher.IO) { ... } block since
            // Retrofit's Coroutine CallAdapter dispatches on a worker
            // thread.
            val response = networkService.getArticleDetail(id, page)
            val reply: List<ArticleItem> = response.Replies ?: let {
                emptyList()
            }
            reply.forEach {
                it.replyTo = response.id
                it.page = page
            }
            database.withTransaction {
                // Insert new users into database, which invalidates the
                // current PagingData, allowing Paging to present the updates
                // in the DB.
                dao.insertAll(reply)
            }

            MediatorResult.Success(
                endOfPaginationReached = false
            )
        } catch (e: IOException) {
            MediatorResult.Error(e)
        } catch (e: HttpException) {
            MediatorResult.Error(e)
        }
    }
}