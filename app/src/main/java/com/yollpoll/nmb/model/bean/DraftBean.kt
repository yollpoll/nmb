package com.yollpoll.nmb.model.bean

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.squareup.moshi.JsonClass

/**
 * Created by spq on 2022/12/6
 */
@JsonClass(generateAdapter = true)
@Entity(tableName = "draft")
data class DraftBean(
    var reply: String?,
    val fid: String,
    @ColumnInfo(name = "f_name")
    val fName: String,//板块名称
    val mask: String,
    val email: String?,
    @PrimaryKey(autoGenerate = true)
    val id: Int? = 0,
    val title: String?,
    val content: String,
    @ColumnInfo(name = "update_time") val updateTime: Long,
    val img: String?
)