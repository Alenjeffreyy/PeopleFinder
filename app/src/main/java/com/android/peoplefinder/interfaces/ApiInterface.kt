package com.android.peoplefinder.interfaces

import com.android.peoplefinder.dataclass.Response
import retrofit2.http.GET
import retrofit2.http.QueryMap

interface ApiInterface {
    @GET("api")
    suspend fun getUserData(@QueryMap hashMap: HashMap<String, Int>): retrofit2.Response<Response>
}