package com.android.peoplefinder.client

import NetworkProvider
import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import com.bumptech.glide.Glide
import com.bumptech.glide.GlideBuilder
import com.bumptech.glide.load.DecodeFormat
import com.bumptech.glide.request.RequestOptions

class AppController : Application() {
    companion object {
        lateinit var appContext: Context
            private set
    }

    @SuppressLint("VisibleForTests")
    override fun onCreate() {
        super.onCreate()

        appContext = applicationContext
        NetworkProvider.init(this)
        Glide.init(this, GlideBuilder().setDefaultRequestOptions(
            RequestOptions().format(DecodeFormat.PREFER_ARGB_8888)))
    }
}





