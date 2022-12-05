package com.yollpoll.nmb.model.repository

import com.yollpoll.base.logI
import com.yollpoll.framework.extensions.shortToast
import com.yollpoll.framework.extensions.toListJson
import com.yollpoll.framework.net.http.RetrofitFactory
import com.yollpoll.nmb.db.MainDB
import com.yollpoll.nmb.di.CommonRetrofitFactory
import com.yollpoll.nmb.model.bean.ForumDetail
import com.yollpoll.nmb.net.HttpService
import javax.inject.Inject

/**
 * Created by spq on 2022/12/2
 */
class ForumRepository @Inject constructor(@CommonRetrofitFactory val retrofitFactory: RetrofitFactory) :
    IRepository {
    private val service = retrofitFactory.createService(HttpService::class.java)
    private val forumDao = MainDB.getInstance().getForumDao()

    //获取所有的板块列表
    suspend fun getAllFromNet(): List<ForumDetail> {
        val list = service.getForumList().flatMap {
            it.forums
        }.map {
            it.show = 1
            return@map it
        }
        val res = arrayListOf<ForumDetail>(
//            ForumDetail(
//                null,
//                null,
//                "-1",
//                null,
//                "",
//                "时间线1",
//                "时间线1",
//                "-2",
//                null,
//                null,
//                show = 1
//            ),
            //添加创作类时间线
            ForumDetail(
                null,
                null,
                "-2",
                null,
                "",
                "时间线2",
                "时间线2",
                "-1",
                null,
                null,
                show = 1
            )
        )
        res.addAll(list)
        res.forEachIndexed { i, item ->
            item.sort = i.toString()
        }
        return res
    }

    //从数据库查询所有
    suspend fun getAllFromDB(): List<ForumDetail> {
        return forumDao.queryAll()
    }

    //首页获取展示的forum
    suspend fun getShowForum(): List<ForumDetail> {
        return forumDao.queryShow().let {
            if (it.isEmpty()) {
                val res = getAllFromNet()
                cacheForumList(res)
                return@let res
            } else {
                return@let it.sortedBy { forum ->
                    forum.sort?.toInt()
                }
            }
        }
    }

    //插入缓存
    suspend fun cacheForumList(list: List<ForumDetail>) {
        val insert = forumDao.insertAll(list)
    }

    //更新数据
    suspend fun updateForumList(vararg forumDetail: ForumDetail) {
        forumDao.update(*forumDetail)
    }
}