package com.yollpoll.utils

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.Target
import com.yollpoll.framework.extensions.startActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

/**
 * 分享MediaStore文件
 * @param path mediaStore中返回的路径
 */
fun share(title: String, path: String, context: Activity) {
    val intent = Intent()
    intent.action = Intent.ACTION_SEND
    val uri = Uri.parse(path)
    intent.type = "image/*"
    intent.putExtra(Intent.EXTRA_STREAM, uri)
    context.startActivity(Intent.createChooser(intent, title))
}

/**
 * 分享
 *
 * @param activityTitle
 * @param msgTitle
 * @param msgText
 * @param imgPath
 */
fun share(
    activityTitle: String?, msgTitle: String?, msgText: String?,
    imgPath: String?, context: Activity
) {
    val intent = Intent(Intent.ACTION_SEND)
    if (imgPath == null || imgPath == "") {
        intent.type = "text/plain" // 纯文本
    } else {
        val f = File(imgPath)
        if (f.exists() && f.isFile()) {
            intent.type = "image/png"
            val u: Uri = Uri.fromFile(f)
            intent.putExtra(Intent.EXTRA_STREAM, u)
        }
    }
    intent.putExtra(Intent.EXTRA_SUBJECT, msgTitle)
    intent.putExtra(Intent.EXTRA_TEXT, msgText)
    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
    context.startActivity(Intent.createChooser(intent, activityTitle))
}

/**
 * 保存图片到mediastore
 */
suspend fun saveImageToMediaStore(url: String, name: String, context: Context): String =
    withContext(Dispatchers.IO) {
        val bitmap = Glide.with(context).asBitmap().load(url)
            .submit(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL).get()
        return@withContext MediaStore.Images.Media.insertImage(
            context.contentResolver, bitmap, name,
            getCurrentDate()
        )
    }

/**
 * 保存图片到SD卡目录
 * 这个方法应当写在子线程
 *
 * @param bitmap
 * @param path   地址，应该用constant里面的常量
 */
//context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)?.absolutePath
fun saveImageToSd(bitmap: Bitmap?, imageName: String, path: String): String? {
    if (null == bitmap) return ""
    //替换/
    val cacheDir = File(path)
    if (!cacheDir.exists()) {
        try {
            cacheDir.mkdirs()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }
    val file = File(cacheDir.absolutePath, imageName)

    var fos: FileOutputStream? = null
    try {
        fos = FileOutputStream(file)
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos)
        fos.flush()
    } catch (e: FileNotFoundException) {
        e.printStackTrace()
    } catch (e: IOException) {
        e.printStackTrace()
    } finally {
        if (fos != null) {
            try {
                fos.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }
    return file.absolutePath
}

/**
 * 更新图片库
 */
fun updatePhoto(context: Context, path: String?, fileName: String?) {
    // 其次把文件插入到系统图库
    try {
        MediaStore.Images.Media.insertImage(
            context.contentResolver,
            path, fileName, null
        )
    } catch (e: FileNotFoundException) {
        e.printStackTrace()
    }
    context.sendBroadcast(
        Intent(
            Intent.ACTION_MEDIA_SCANNER_SCAN_FILE,
            Uri.fromFile(File(path))
        )
    )
}

//获取当前日期
fun getCurrentDate(): String {
    val sdf = SimpleDateFormat("yyyy-MM-dd")
    return sdf.format(Date().time)
}