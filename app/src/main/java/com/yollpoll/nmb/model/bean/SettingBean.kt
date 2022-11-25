package com.yollpoll.nmb.model.bean

import com.yollpoll.skin.SkinTheme

/**
 * Created by spq on 2022/11/21
 */
data class SettingBean(
    val uiTheme: SkinTheme,
    val darkMod: DarkMod,
    val collectionId: String,
    val noImage: Boolean,
    val noCookie: Boolean,
    val thumbBigImg: Boolean,
)

enum class DarkMod(name: String) {
    DARK("黑暗"), LIGHT("光明"), AUTO("自动")
}