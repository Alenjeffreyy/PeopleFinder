package com.android.peoplefinder.viewmodel


import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.PagingSource
import androidx.paging.cachedIn
import com.android.peoplefinder.activity.Db.ApiResponse
import com.android.peoplefinder.activity.Db.User
import com.android.peoplefinder.activity.UserPagingSource
import com.android.peoplefinder.activity.repository.CommonRepository
import com.android.peoplefinder.activity.repository.LocalDBRepository
import com.android.peoplefinder.dataclass.Response
import com.android.peoplefinder.dataclass.getUser
import com.android.peoplefinder.helper.Enums
import com.android.peoplefinder.network.ApiExceptionHandler
import com.android.peoplefinder.network.NetworkResult
import kotlinx.coroutines.flow.Flow


class CommonViewModel(
    application: Application,
    private val commonRepository: CommonRepository,
    private val apiExceptionHandler: ApiExceptionHandler,
    private val localRepository: LocalDBRepository
) : AndroidViewModel(application) {

    suspend fun apiRequestSuspended(
        requestCode: Int,
        hashMap: HashMap<String, Int>
    ): NetworkResult<Any> {
        return try {
            val response = when (requestCode) {
                Enums.REQ_DATA -> commonRepository.getUserData(hashMap)
                else -> return NetworkResult.Error("Unknown requestCode", requestCode = requestCode)
            }

            Log.d("apiRequestSuspended", "Response: ${response.body().toString()}")

            response.body()?.let {
                try {
                    val parsedResponse = it as? Response
                    parsedResponse?.let { response ->
                        val userList = response.results
                        insertResponseInDatabase(response)
                        Log.d("apiRequestSuspended", "Parsed User List: $userList")
                        NetworkResult.Success(userList, requestCode)
                    } ?: NetworkResult.Error("Parsed response is null", requestCode = requestCode)

                } catch (parseException: Exception) {
                    Log.e("apiRequestSuspended", "Error parsing response: ${parseException.message}")
                    NetworkResult.Error("Error parsing response", requestCode = requestCode)
                }
            } ?: NetworkResult.Error("Response is null", requestCode = requestCode)
        } catch (e: Exception) {
            Log.e("apiRequestSuspended", "Error occurred from the server end: ${e.message}")
            NetworkResult.Error(e.message ?: "Unknown error", requestCode = requestCode)
        }
    }


    private suspend fun insertResponseInDatabase(response: Response) {
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
                    latitude = userData.location.coordinates.latitude,
                    longitude = userData.location.coordinates.longitude,
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

    fun mapToUser(apiUser: getUser): User {
        return User(
            slNo = 0,
            uuid = apiUser.login.username,
            firstName = apiUser.name.first,
            lastName = apiUser.name.last,
            gender = apiUser.gender,
            latitude = apiUser.location.coordinates.latitude,
            longitude = apiUser.location.coordinates.longitude,
            location = "${apiUser.location.street.number} ${apiUser.location.street.name}, ${apiUser.location.city}, ${apiUser.location.country}",
            email = apiUser.email,
            phone = apiUser.phone,
            cell = apiUser.cell,
            pictureMedium = apiUser.picture.medium,
            pictureLarge = apiUser.picture.large,
            pictureThumbnail = apiUser.picture.thumbnail,
            nationality = apiUser.nat
        )
    }

    val pagedUsers: Flow<PagingData<User>> = Pager(PagingConfig(pageSize = 20)) {
        localRepository.getUsers()
    }.flow.cachedIn(viewModelScope)

    fun searchUsers(query: String): Flow<PagingData<User>> {
        return commonRepository.getSearchResults(query)
            .cachedIn(viewModelScope)
    }
    suspend fun isUserDataAvailable(): Boolean {
        return commonRepository.getUserCount() > 0
    }

    fun getLocalPagingSource(): PagingSource<Int, User> {
        return commonRepository.getLocalPagingSource()
    }

}
