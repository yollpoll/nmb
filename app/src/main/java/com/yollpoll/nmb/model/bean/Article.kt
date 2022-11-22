package com.yollpoll.nmb.model.bean

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

class Article : ArrayList<ArticleItem>()

@JsonClass(generateAdapter = true)
@Entity
data class ArticleItem(
    @PrimaryKey(autoGenerate = true)
    var _id: Int,
    var admin: String,
    var content: String,
    var email: String?,
    var ext: String,
    var id: String,
    var img: String,
    var name: String,
    var now: String,
    var ReplyCount: String?,
    var title: String,
    var user_hash: String,
    var master: String?,//是否是发帖人
    var page: Int = 1,
) {
    @Ignore
    var Replies: List<ArticleItem>? = null
}

