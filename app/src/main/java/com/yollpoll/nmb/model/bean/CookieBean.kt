package com.yollpoll.nmb.model.bean

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@Entity
@JsonClass(generateAdapter = true)
data class CookieBean(
    var cookie: String,
    @PrimaryKey(autoGenerate = false) var name: String,
    var used: Int = 0//0未使用，1使用中
)