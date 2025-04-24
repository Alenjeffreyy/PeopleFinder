package com.android.peoplefinder.client

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import okhttp3.Interceptor
import okhttp3.Protocol
import okhttp3.Response
import okhttp3.ResponseBody
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import org.json.JSONObject
import java.io.IOException

class NetworkInterceptor(private val context: Context) : Interceptor {

    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        return if (!isOnline(context)) {
            val responseBody = getNetworkFailResponse()
            Response.Builder()
                .code(600)
                .protocol(Protocol.HTTP_1_1)
                .message("No internet")
                .body(responseBody)
                .request(chain.request())
                .build()
        } else {
            chain.proceed(chain.request())
        }
    }

    private fun isOnline(context: Context): Boolean {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = cm.activeNetwork ?: return false
        val capabilities = cm.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    private fun getNetworkFailResponse(): ResponseBody {
        val jsonObject = JSONObject().apply {
            put("code", 600)
            put("status", "Cancel")
            put("message", "No network connection")
        }
        val mediaType = "application/json".toMediaType()
        return jsonObject.toString().toResponseBody(mediaType)
    }
}
