package com.yollpoll.nmb.model.bean

class Article : ArrayList<ArticleItem>()

data class ArticleItem(
    val admin: String,
    val content: String,
    val email: String,
    val ext: String,
    val id: String,
    val img: String,
    val name: String,
    val now: String,
    val replyCount: String,
    val replys: List<Reply>,
    val title: String,
    val userid: String
)

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