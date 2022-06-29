package com.yollpoll.nmb.model.bean

class ForumList : ArrayList<Forum>()


//板块
data class Forum(
    val forums: List<ForumDetail>,
    val id: String,
    val name: String,
    val sort: String,
    val status: String
)

//详细版本
data class ForumDetail(
    var createdAt: String? = null,
    var fgroup: String?,
    val id: String,
    var interval: String?,
    var msg: String?,
    var name: String?,
    var showName: String?,
    var sort: String?,
    var status: String?,
    var updateAt: String?
)