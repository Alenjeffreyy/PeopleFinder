package com.android.peoplefinder.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.android.peoplefinder.activity.Db.ApiResponse
import com.android.peoplefinder.activity.Db.User
import com.android.peoplefinder.activity.repository.CommonRepository
import com.android.peoplefinder.activity.repository.LocalDBRepository

import com.android.peoplefinder.helper.Enums
import com.android.peoplefinder.network.ApiExceptionHandler
import com.android.peoplefinder.network.NetworkResult
import com.google.gson.Gson

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch



import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.paging.Pager
import androidx.paging.PagingConfig
import com.android.peoplefinder.activity.UserPagingSource
import com.android.peoplefinder.dataclass.Response
import org.json.JSONObject


class CommonViewModel(
    application: Application,
    private val commonRepository: CommonRepository,
    private val apiExceptionHandler: ApiExceptionHandler,
    private val localRepository: LocalDBRepository
) : AndroidViewModel(application) {

    private val scope = viewModelScope
    suspend fun apiRequestSuspended(
        requestCode: Int,
        hashMap: HashMap<String, Int>
    ): NetworkResult<Any> {
        return try {
            val response = when (requestCode) {
                Enums.REQ_DATA -> commonRepository.getUserData(hashMap) // Assume this returns a Response object
                else -> return NetworkResult.Error("Unknown requestCode", requestCode = requestCode)
            }

            Log.d("apiRequestSuspended", "Response: ${response.body().toString() ?: "Response is null"}")

            response.body()?.let {
                try {
                    val parsedResponse = it as? com.android.peoplefinder.dataclass.Response
                    parsedResponse?.let { response ->
                        val userList = response.results
                        insertResponseInDatabase(response, requestCode)
                        Log.d("apiRequestSuspended", "Parsed User List: $userList")
                        NetworkResult.Success(userList, requestCode)
                    } ?: NetworkResult.Error("Parsed response is null", requestCode = requestCode)

                } catch (parseException: Exception) {
                    Log.e("apiRequestSuspended", "Error parsing response: ${parseException.message}")
                    NetworkResult.Error("Error parsing response", requestCode = requestCode)
                }
            } ?: NetworkResult.Error("Response is null", requestCode = requestCode)
        } catch (e: Exception) {
            Log.e("apiRequestSuspended", "Error occurred: ${e.message}")
            NetworkResult.Error(e.message ?: "Unknown error", requestCode = requestCode)
        }
    }






    private suspend fun insertResponseInDatabase(response: Response, requestCode: Int) {
        try {
            val usersToInsert = mutableListOf<User>()

            for (userData in response.results) {
                val user = User(
                    uuid = userData.login.username,
                    firstName = userData.name.first,
                    lastName = userData.name.last,
                    gender = userData.gender,
                    location = "${userData.location.street.number} ${userData.location.street.name}, ${userData.location.city}, ${userData.location.country}", // Location
                    email = userData.email,
                    phone = userData.phone,
                    cell = userData.cell,
                    pictureMedium = userData.picture.medium,
                    pictureLarge = userData.picture.large,
                    pictureThumbnail = userData.picture.thumbnail,
                    nationality = userData.nat
                )
                usersToInsert.add(user)
            }

            if (usersToInsert.isNotEmpty()) {
                localRepository.insertUsers(usersToInsert)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e("insertResponseInDatabase", "Error: ${e.message}")
        }
    }



    fun getUsers(): Flow<PagingData<User>> {
        return Pager(PagingConfig(pageSize = 25)) {
            UserPagingSource(this)
        }.flow.cachedIn(viewModelScope)
    }

    val pagedUsers: Flow<PagingData<User>> = Pager(PagingConfig(pageSize = 20)) {
        localRepository.getUsers()
    }.flow.cachedIn(viewModelScope)


    suspend fun getApiResponse(requestCode: Int): ApiResponse? {
        return localRepository.getApiResponse(requestCode)
    }

    fun searchUsers(query: String): Flow<PagingData<User>> {
        return commonRepository.getSearchResults(query)
            .cachedIn(viewModelScope)
    }
}
