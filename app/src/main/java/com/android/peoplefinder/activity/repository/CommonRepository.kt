package com.android.peoplefinder.activity.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.android.peoplefinder.activity.Db.ApiResponseDao
import com.android.peoplefinder.activity.Db.User
import com.android.peoplefinder.dataclass.Response

import com.android.peoplefinder.interfaces.ApiInterface
import com.google.gson.JsonObject
import kotlinx.coroutines.flow.Flow

class CommonRepository(
    private val apiService: ApiInterface,
    private val dao: ApiResponseDao
) {
    suspend fun getUserData(request: HashMap<String, Int>): retrofit2.Response<Response> {
        return apiService.getUserData(request)
    }


    fun getSearchResults(query: String): Flow<PagingData<User>> {
        return Pager(
            config = PagingConfig(pageSize = 20),
            pagingSourceFactory = { dao.searchUsers("%$query%") }
        ).flow
    }
}