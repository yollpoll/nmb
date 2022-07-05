package com.yollpoll.nmb.model.bean

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

class Article : ArrayList<ArticleItem>()

@JsonClass(generateAdapter = true)
data class ArticleItem(
    val admin: String,
    val content: String,
    val email: String,
    val ext: String,
    val id: String,
    val img: String,
    val name: String,
    val now: String,
    var ReplyCount: String,
    val replys: List<Reply>,
    val title: String,
    val user_hash: String
)

@JsonClass(generateAdapter = true)
data class Reply(
    val admin: String? = null,
    val content: String? = null,
    val email: String? = null,
    val ext: String? = null,
    val id: String? = null,
    val img: String? = null,
    val name: String? = null,
    val now: String? = null,
    val title: String? = null,
    val userid: String? = null
)