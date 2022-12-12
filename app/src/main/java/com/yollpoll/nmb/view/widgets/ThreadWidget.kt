package com.yollpoll.nmb.view.widgets

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.text.Html
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.view.View
import android.widget.RemoteViews
import androidx.core.text.HtmlCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.SimpleTarget
import com.bumptech.glide.request.transition.Transition
import com.yollpoll.nmb.R
import com.yollpoll.nmb.model.bean.ArticleItem
import com.yollpoll.nmb.net.imgThumbUrl
import com.yollpoll.nmb.view.activity.ThreadDetailActivity
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.regex.Pattern


/**
 * 桌面小组件
 */
class ThreadWidget : AppWidgetProvider() {

    override fun onUpdate(
        context: Context?,
        appWidgetManager: AppWidgetManager?,
        appWidgetIds: IntArray?
    ) {
        super.onUpdate(context, appWidgetManager, appWidgetIds)
    }

    //广播
    override fun onReceive(context: Context?, intent: Intent?) {
        super.onReceive(context, intent)
    }

    override fun onDisabled(context: Context?) {
        super.onDisabled(context)
    }

    override fun onEnabled(context: Context?) {
        super.onEnabled(context)
    }

    //改变大小
    override fun onAppWidgetOptionsChanged(
        context: Context?,
        appWidgetManager: AppWidgetManager?,
        appWidgetId: Int,
        newOptions: Bundle?
    ) {
        super.onAppWidgetOptionsChanged(context, appWidgetManager, appWidgetId, newOptions)
    }
}

fun updateAppWidget(context: Context, articleItem: ArticleItem) {
    //初始化RemoteViews
    val componentName = ComponentName(context, ThreadWidget::class.java)
    val remoteViews = RemoteViews(context.packageName, R.layout.widget_thread)

    //点击事件跳转页面
    val intent = Intent(context, ThreadDetailActivity::class.java)
    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    intent.putExtra("id", articleItem.id)
    val processInfoIntent =
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            //31，Android11以上系统
            PendingIntent.getActivity(
                context,
                System.currentTimeMillis().toInt(),
                intent,
                PendingIntent.FLAG_IMMUTABLE
            )
        } else {
            PendingIntent.getActivity(
                context,
                System.currentTimeMillis().toInt(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT
            )
        }
    remoteViews.setOnClickPendingIntent(R.id.ll_root, processInfoIntent)
    if (articleItem.img.isEmpty()) {
        remoteViews.setViewVisibility(R.id.iv_content, View.GONE)
    } else {
        remoteViews.setViewVisibility(R.id.iv_content, View.VISIBLE)
        //图片加载
        Glide.with(context)
            .asBitmap()
            .apply(getCommonGlideOptions(context))
            .load(imgThumbUrl + articleItem.img + articleItem.ext)
            .into(object : SimpleTarget<Bitmap>() {
                override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                    remoteViews.setImageViewBitmap(R.id.iv_content, resource)
                }
            })
    }
    if (articleItem.admin == "1") {
        remoteViews.setTextColor(R.id.tv_user, context.resources.getColor(R.color.color_red))
    }
    val htmlContent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        Html.fromHtml(articleItem.content,
            HtmlCompat.FROM_HTML_MODE_COMPACT,
            {
                null
            }
        ) { opening, tag, output, xmlReader ->
        }
    } else {
        Html.fromHtml(articleItem.content)
    }

    //herf
    val spannableString = SpannableString(htmlContent)
    //串引用
    val pattern = Pattern.compile(">>No.\\d*")
    val matcher = pattern.matcher(htmlContent)
    while (matcher.find()) {
        spannableString.setSpan(
            ForegroundColorSpan(Color.parseColor("#7cb342")),
            matcher.start(),
            matcher.end(),
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )
    }
    remoteViews.setTextViewText(R.id.tv_content, spannableString)
    remoteViews.setTextViewText(R.id.tv_title, articleItem.title.ifEmpty { "无标题" })
    remoteViews.setTextViewText(R.id.tv_time, articleItem.now)
    remoteViews.setTextViewText(R.id.tv_user, articleItem.user_hash)
    val manager = AppWidgetManager.getInstance(context)
    GlobalScope.launch {
        delay(4000)
        manager.updateAppWidget(componentName, remoteViews)
    }
}