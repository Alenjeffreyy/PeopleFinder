package com.android.peoplefinder.activity.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.android.peoplefinder.activity.Db.ApiResponse
import com.android.peoplefinder.activity.Db.ApiResponseDao
import com.android.peoplefinder.activity.Db.User

import com.android.peoplefinder.interfaces.ApiInterface
import kotlinx.coroutines.flow.Flow
import retrofit2.Response

class CommonRepository(
    private val apiService: ApiInterface,
    private val dao: ApiResponseDao
) {
    suspend fun getUserData(request: HashMap<String, Any>): Response<Any> {
        return apiService.getUserData(request) as Response<Any>
    }

    fun getSearchResults(query: String): Flow<PagingData<User>> {
        return Pager(
            config = PagingConfig(pageSize = 20),
            pagingSourceFactory = { dao.searchUsers("%$query%") }
        ).flow
    }
}