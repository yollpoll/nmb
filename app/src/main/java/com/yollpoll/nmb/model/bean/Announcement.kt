package com.yollpoll.nmb.model.bean

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
class Announcement(val content: String, val date: Long, val enable: Boolean) {
}