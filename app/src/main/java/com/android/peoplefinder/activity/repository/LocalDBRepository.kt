package com.android.peoplefinder.activity.repository

import androidx.paging.PagingSource
import com.android.peoplefinder.activity.Db.ApiResponse
import com.android.peoplefinder.activity.Db.ApiResponseDao
import com.android.peoplefinder.activity.Db.User


class LocalDBRepository(private val dao: ApiResponseDao) {

    suspend fun insertApiResponse(apiResponse: ApiResponse) {
        dao.insertApiResponse(apiResponse)
    }
    suspend fun insertUsers(user: List<User>) {
        dao.insertUsers(user)
    }
    fun getUsers(): PagingSource<Int, User> {
        return dao.getUsers()
    }

    suspend fun getApiResponse(requestCode: Int): ApiResponse? = dao.getResponseByKey(requestCode)
}