package com.android.peoplefinder.interfaces

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.QueryMap

interface ApiInterface {
    @GET("api")
    suspend fun getUserData(@QueryMap hashMap: HashMap<String,Any>): Response<Any>
}