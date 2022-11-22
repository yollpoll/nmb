package com.yollpoll.nmb.db

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.yollpoll.nmb.model.bean.ArticleItem

@Dao
interface ArticleDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(users: List<ArticleItem>)

    @Query("SELECT * FROM ArticleItem WHERE replyTo LIKE :replyTo")
    fun pagingSource(replyTo: String): PagingSource<Int, ArticleItem>

    @Query("DELETE FROM ArticleItem")
    suspend fun clearAll()


}