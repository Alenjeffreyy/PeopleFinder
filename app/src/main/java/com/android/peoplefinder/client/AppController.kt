package com.android.peoplefinder.client



import android.annotation.SuppressLint
import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.content.res.Configuration
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import com.android.peoplefinder.module.AppProvider
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
        AppProvider.init(this)
        NetworkProvider.init(this)
        Glide.init(this, GlideBuilder().setDefaultRequestOptions(
            RequestOptions().format(DecodeFormat.PREFER_ARGB_8888)))
    }
}





