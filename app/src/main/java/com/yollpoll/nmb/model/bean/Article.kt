package com.yollpoll.nmb.model.bean

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

class Article : ArrayList<ArticleItem>()

@JsonClass(generateAdapter = true)
data class ArticleItem(
    val admin: String,
    val content: String,
    val email: String?,
    val ext: String,
    @PrimaryKey
    val id: String,
    val img: String,
    val name: String,
    val now: String,
    var ReplyCount: String?,
    val Replies: List<ArticleItem>?,
    val title: String,
    val user_hash: String,
    var master: String?,//是否是发帖人
    var page:Int=1
)