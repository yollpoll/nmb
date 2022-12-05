package com.yollpoll.nmb.db

import androidx.room.*
import com.yollpoll.nmb.model.bean.ArticleItem
import com.yollpoll.nmb.model.bean.ForumDetail

/**
 * Created by spq on 2022/12/2
 */
@Dao
interface ForumDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(forumList: List<ForumDetail>): List<Long>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertIfUnExist(forumList: List<ForumDetail>)

    @Update
    suspend fun update(vararg forumDetail: ForumDetail)

    @Query("SELECT * FROM forum ORDER BY sort")
    suspend fun queryAll(): List<ForumDetail>

    @Query("SELECT * FROM forum WHERE show=1 ORDER BY sort")
    suspend fun queryShow(): List<ForumDetail>

}