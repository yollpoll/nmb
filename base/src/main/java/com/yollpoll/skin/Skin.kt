package com.yollpoll.skin

import android.content.Context
import android.content.pm.PackageManager
import android.content.res.AssetManager
import android.content.res.Resources
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup

/**
 * Created by spq on 2022/11/15
 */


var skinTheme = SkinTheme.NULL

//主题类型
enum class SkinTheme(name: String) {
    NULL("NULL"), MATERIAL("MATERIAL"), OLD_SCHOOL("OLD_SCHOOL"), OTHER("OTHER")
}
//函数别名
typealias SkinHandler = (parent: View?, name: String, view: View, attrs: AttributeSet) -> View

val materialHandlers = hashMapOf(MaterialItemLine, MaterialItem)
val oldSchoolHandlers = hashMapOf<String, SkinHandler>()
val otherHandlers = hashMapOf<String, SkinHandler>()

val handlerPool = hashMapOf(
    SkinTheme.MATERIAL to materialHandlers,
    SkinTheme.OLD_SCHOOL to oldSchoolHandlers,
    SkinTheme.OTHER to otherHandlers,
)

fun getSkinResource(context: Context, path: String): Resources {
    val mPackageManager = context.packageManager
    val mPackageInfo = mPackageManager.getPackageArchiveInfo(path, PackageManager.GET_ACTIVITIES)
    val mAssertManager = AssetManager::class.java.newInstance()
    val addAssertPath = mAssertManager.javaClass.getMethod("addAssetPath", String.javaClass)
    //添加资源路径
    addAssertPath.invoke(mAssertManager, path)

    val mResource = context.resources
    //    val skinResource=Resources(mAssertManager,mResource.displayMetrics,mResource.configuration)
    return context.createConfigurationContext(mResource.configuration).resources
}

/**
 * 设置view的皮肤
 * @receiver View
 * @param name String
 * @param tag String
 * @param attrs AttributeSet
 */
fun View.setSkin(parent: View?, name: String, tag: String, attrs: AttributeSet): View {
    if (skinTheme == SkinTheme.NULL)
        return this
    return handlerPool[skinTheme]?.let {
        it[tag]?.invoke(parent, name, this, attrs)
    } ?: let {
        it
    }
}