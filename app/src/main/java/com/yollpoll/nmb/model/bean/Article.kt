package com.yollpoll.nmb.model.bean

import androidx.room.*
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import java.sql.Timestamp


//class Article : ArrayList<ArticleItem>()
typealias Article = ArrayList<ArticleItem>

@Entity
@JsonClass(generateAdapter = true)
data class ArticleItem(
    var admin: String,
    var content: String,
    var email: String?,
    @ColumnInfo(name = "ext") var ext: String,
    @PrimaryKey var id: String,
    @ColumnInfo(name = "img") var img: String,
    var name: String,
    var now: String,
    var ReplyCount: String?,
    var title: String,
    var user_hash: String,
    var master: String?,//是否是发帖人
    var page: Int = 1,
    var sage: Int,//吃我世嘉
    var Hide: Int,
    var replyTo: String?,//当前回复的串的id
) {
    @Ignore
    var Replies: List<ArticleItem>? = null

    @Ignore
    var tagColor: Int? = null//标记

    @Ignore
    var index: Int? = null//第x条回复


}

@JsonClass(generateAdapter = true)
data class ImgTuple(
    @ColumnInfo(name = "img") val img: String,
    @ColumnInfo(name = "ext") val ext: String,
    @ColumnInfo(name = "id") val id: String,
)

data class PageItem(@ColumnInfo(name = "page") val page: Int)

@Entity
data class ShieldArticle(@PrimaryKey val articleId: String)//屏蔽列表
