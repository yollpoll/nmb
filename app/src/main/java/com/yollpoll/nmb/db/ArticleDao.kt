package com.yollpoll.nmb.db

import androidx.paging.PagingSource
import androidx.room.*
import com.yollpoll.nmb.model.bean.ArticleItem
import com.yollpoll.nmb.model.bean.ImgTuple
import com.yollpoll.nmb.model.bean.PageItem
import com.yollpoll.nmb.model.bean.ShieldArticle
import kotlinx.coroutines.flow.Flow

@Dao
interface ArticleDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(article: List<ArticleItem>)

    @Query("SELECT * FROM ArticleItem WHERE replyTo LIKE :replyTo AND page LIKE :page")
    fun pagingSource(replyTo: String, page: Int): PagingSource<Int, ArticleItem>

    @Query("DELETE FROM ArticleItem")
    suspend fun clearAll()

    //查询回复
    @Query("SELECT * FROM ArticleItem WHERE replyTo LIKE :replyTo AND page LIKE :page")
    suspend fun getReplies(replyTo: String, page: Int): List<ArticleItem>

    //根据id查询
    @Query("SELECT * FROM ArticleItem WHERE id LIKE :id")
    suspend fun getArticle(id: String): ArticleItem?

    //查询图片
    @Query("SELECT img,ext,id FROM ArticleItem WHERE replyTo LIKE :replyTo OR id LIKE :replyTo ORDER BY id")
    fun getImageFlow(replyTo: String): Flow<List<ImgTuple>>

    @Query("SELECT img,ext,id FROM ArticleItem WHERE replyTo LIKE :replyTo OR id LIKE :replyTo ORDER BY id")
    suspend fun getImageList(replyTo: String): List<ImgTuple>

    //屏蔽列表
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertShield(list: List<ShieldArticle>)

    //查询屏蔽列表
    @Query("SELECT * FROM ShieldArticle")
    suspend fun getShieldArticle(): List<ShieldArticle>

    @Query("DELETE  FROM ShieldArticle WHERE articleId LIKE :articleId")
    suspend fun deleteShieldByArticleId(articleId: String)

    @Query("SELECT * FROM ArticleItem WHERE id LIKE :id")
    suspend fun getShieldArticleList(id: String): ArticleItem

    @Query("SELECT page FROM ArticleItem WHERE replyTo LIKE :id ORDER BY id")
    suspend fun getAllReplyPage(id: String): List<PageItem>
}