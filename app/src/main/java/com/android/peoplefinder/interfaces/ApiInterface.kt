package com.android.peoplefinder.interfaces

import com.android.peoplefinder.dataclass.Response
import com.google.gson.JsonObject
import retrofit2.http.GET
import retrofit2.http.Query
import retrofit2.http.QueryMap

interface ApiInterface {
    @GET("api")
    suspend fun getUserData(@QueryMap hashMap: HashMap<String, Int>): retrofit2.Response<Response>

    @GET("weather")
    suspend fun getWeather(@QueryMap options: Map<String, @JvmSuppressWildcards Any>): retrofit2.Response<JsonObject>



}