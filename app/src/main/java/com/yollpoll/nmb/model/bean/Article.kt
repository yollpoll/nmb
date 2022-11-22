package com.yollpoll.nmb.model.bean

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import java.sql.Timestamp

class Article : ArrayList<ArticleItem>()

@Entity
@JsonClass(generateAdapter = true)
data class ArticleItem(
    var admin: String,
    var content: String,
    var email: String?,
    var ext: String,
    @PrimaryKey
    var id: String,
    var img: String,
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
    var timestamp: Long
) {
    @Ignore
    var Replies: List<ArticleItem>? = null
}

