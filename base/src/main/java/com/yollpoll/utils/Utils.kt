package com.yollpoll.utils

import android.app.Activity
import android.content.*
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import com.yollpoll.framework.extensions.dp2px
import java.io.File
import java.io.FileNotFoundException
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

fun getPathByUri(uri: Uri, context: Context): String? {
    return if (Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT) {
        getPhotoPathFromContentUri(context, uri)
    } else {
        getFileByUri(uri, context)
    }
}

/**
 * 通过Uri返回File文件
 * 注意：通过相机的是类似content://media/external/images/media/97596
 * 通过相册选择的：file:///storage/sdcard0/DCIM/Camera/IMG_20150423_161955.jpg
 * 通过查询获取实际的地址
 *
 * @param uri
 * @return
 */
fun getFileByUri(uri: Uri, activity: Context): String? {
    var path: String? = null
    if ("file" == uri.scheme) {
        path = uri.encodedPath
        if (path != null) {
            path = Uri.decode(path)
            val cr = activity.contentResolver
            val buff = StringBuffer()
            buff.append("(").append(MediaStore.Images.ImageColumns.DATA).append("=").append(
                "'$path'"
            ).append(")")
            val cur = cr.query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI, arrayOf(
                    MediaStore.Images.ImageColumns._ID, MediaStore.Images.ImageColumns.DATA
                ), buff.toString(), null, null
            )
            var index = 0
            var dataIdx = 0
            cur!!.moveToFirst()
            while (!cur.isAfterLast) {
                index = cur.getColumnIndex(MediaStore.Images.ImageColumns._ID)
                index = cur.getInt(index)
                dataIdx = cur.getColumnIndex(MediaStore.Images.ImageColumns.DATA)
                path = cur.getString(dataIdx)
                cur.moveToNext()
            }
            cur.close()
            if (index == 0) {
            } else {
                val u = Uri.parse("content://media/external/images/media/$index")
                println("temp uri is :$u")
            }
        }
        if (path != null) {
            return path
        }
    } else if ("content" == uri.scheme) {
        // 4.2.2以后
        val proj = arrayOf(MediaStore.Images.Media.DATA)
        val cursor = activity.contentResolver.query(uri, proj, null, null, null)
        if (cursor!!.moveToFirst()) {
            val columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
            path = cursor.getString(columnIndex)
        }
        cursor.close()
        return path
    } else {
    }
    return null
}

/**
 * 4.4以后通过uri获取图片地址
 *
 * @param context
 * @param uri
 * @return
 */
fun getPhotoPathFromContentUri(context: Context?, uri: Uri?): String? {
    var photoPath: String? = ""
    if (context == null || uri == null) {
        return photoPath
    }
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT && DocumentsContract.isDocumentUri(
            context,
            uri
        )
    ) {
        val docId = DocumentsContract.getDocumentId(uri)
        if (isExternalStorageDocument(uri)) {
            val split = docId.split(":").toTypedArray()
            if (split.size >= 2) {
                val type = split[0]
                if ("primary".equals(type, ignoreCase = true)) {
                    photoPath =
                        Environment.getExternalStorageDirectory().toString() + "/" + split[1]
                }
            }
        } else if (isDownloadsDocument(uri)) {
            val contentUri = ContentUris.withAppendedId(
                Uri.parse("content://downloads/public_downloads"),
                java.lang.Long.valueOf(docId)
            )
            photoPath = getDataColumn(context, contentUri, null, null)
        } else if (isMediaDocument(uri)) {
            val split = docId.split(":").toTypedArray()
            if (split.size >= 2) {
                val type = split[0]
                var contentUris: Uri? = null
                if ("image" == type) {
                    contentUris = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                } else if ("video" == type) {
                    contentUris = MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                } else if ("audio" == type) {
                    contentUris = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
                }
                val selection = MediaStore.Images.Media._ID + "=?"
                val selectionArgs = arrayOf(split[1])
                photoPath = getDataColumn(context, contentUris, selection, selectionArgs)
            }
        }
    } else if ("file".equals(uri.scheme, ignoreCase = true)) {
        photoPath = uri.path
    } else {
        photoPath = getDataColumn(context, uri, null, null)
    }
    return photoPath
}

private fun isExternalStorageDocument(uri: Uri): Boolean {
    return "com.android.externalstorage.documents" == uri.authority
}

private fun isDownloadsDocument(uri: Uri): Boolean {
    return "com.android.providers.downloads.documents" == uri.authority
}

private fun isMediaDocument(uri: Uri): Boolean {
    return "com.android.providers.media.documents" == uri.authority
}

private fun getDataColumn(
    context: Context,
    uri: Uri?,
    selection: String?,
    selectionArgs: Array<String>?
): String? {
    var cursor: Cursor? = null
    val column = MediaStore.Images.Media.DATA
    val projection = arrayOf(column)
    try {
        cursor =
            context.contentResolver.query(uri!!, projection, selection, selectionArgs, null)
        if (cursor != null && cursor.moveToFirst()) {
            val index = cursor.getColumnIndexOrThrow(column)
            return cursor.getString(index)
        }
    } finally {
        if (cursor != null && !cursor.isClosed) cursor.close()
    }
    return null
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

/**
 * 复制内容到剪切板
 *
 * @param copyStr
 * @return
 */
fun copyStr(context: Context, copyStr: String): Boolean {
    return try {
        //获取剪贴板管理器
        val cm: ClipboardManager? =
            context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager?
        // 创建普通字符型ClipData
        val mClipData: ClipData = ClipData.newPlainText("Label", copyStr)
        // 将ClipData内容放到系统剪贴板里。
        cm?.setPrimaryClip(mClipData)
        true
    } catch (e: Exception) {
        false
    }
}

/**
 * 设置某个View的margin
 *
 * @param view   需要设置的view
 * @param isDp   需要设置的数值是否为DP
 * @param left   左边距
 * @param right  右边距
 * @param top    上边距
 * @param bottom 下边距
 * @return
 */
fun View.setViewMargin(
    left: Float? = null,
    right: Float? = null,
    top: Float? = null,
    bottom: Float? = null,
    attr:AttributeSet?=null
): ViewGroup.LayoutParams {
    val context = this.context
    val params: ViewGroup.LayoutParams? = this.layoutParams
    var marginParams: ViewGroup.MarginLayoutParams? = null
    //获取view的margin设置参数
    marginParams = params?.let {
        return@let if (params is ViewGroup.MarginLayoutParams) {
            params
        } else {
            //不存在时创建一个新的参数
            ViewGroup.MarginLayoutParams(params)
        }
    } ?: let {
        ViewGroup.MarginLayoutParams(context,attr)
    }

    var leftMargin = marginParams.leftMargin
    var rightMargin = marginParams.rightMargin
    var topMargin = marginParams.topMargin
    var bottomMargin = marginParams.bottomMargin

    left?.let {
        leftMargin = context.dp2px(it).toInt()
    }
    right?.let {
        rightMargin = context.dp2px(it).toInt()
    }
    top?.let {
        topMargin = context.dp2px(it).toInt()
    }
    bottom?.let {
        bottomMargin = context.dp2px(it).toInt()
    }
    //设置margin
    marginParams.setMargins(leftMargin, topMargin, rightMargin, bottomMargin)
    this.layoutParams = marginParams
    return marginParams
}