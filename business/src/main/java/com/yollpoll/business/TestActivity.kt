package com.yollpoll.business

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import okhttp3.OkHttpClient

class TestActivity:AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val tv=TextView(this)
        tv.text="hello world"
        setContentView(tv)

        val builder= OkHttpClient.Builder()
        builder.build()
    }
}