package com.yollpoll.nmb.model.bean

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.squareup.moshi.JsonClass

class ForumList : ArrayList<Forum>()


//板块
@JsonClass(generateAdapter = true)
data class Forum(
    val forums: List<ForumDetail>,
    val id: String,
    val name: String,
    val sort: String,
    val status: String
)

//详细版本
@JsonClass(generateAdapter = true)
@Entity(tableName = "forum")
data class ForumDetail(
    var createdAt: String?,
    var fgroup: String?,
    @PrimaryKey
    val id: String,
    var interval: String?,
    var msg: String,
    var name: String,
    var showName: String?,
    var sort: String?,
    var status: String?,
    var updateAt: String?,
    var show: Int = 1
)