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
            // Fetch data from repository or API service
            val response = when (requestCode) {
                Enums.REQ_DATA -> commonRepository.getUserData(hashMap) // Assume this returns a Response object
                else -> return NetworkResult.Error("Unknown requestCode", requestCode = requestCode)
            }

            Log.d("apiRequestSuspended", "Response: ${response.body().toString() ?: "Response is null"}")

            // If the response is valid, parse it
            response.body()?.let {
                try {
                    // Parse the response and insert it into the database


                    // Assuming the response is a JSON object with a "results" key
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
            // Log the error and return the error result
            Log.e("apiRequestSuspended", "Error occurred: ${e.message}")
            NetworkResult.Error(e.message ?: "Unknown error", requestCode = requestCode)
        }
    }






    private suspend fun insertResponseInDatabase(response: Response, requestCode: Int) {
        try {
            val usersToInsert = mutableListOf<User>()

            // Iterate through each result and build the User objects
            for (userData in response.results) {
                val user = User(
                    uuid = userData.login.username,  // Access the login username
                    firstName = userData.name.first,  // Access first name
                    lastName = userData.name.last,    // Access last name
                    gender = userData.gender,         // Gender
                    location = "${userData.location.street.number} ${userData.location.street.name}, ${userData.location.city}, ${userData.location.country}", // Location
                    email = userData.email,           // Email
                    phone = userData.phone,           // Phone
                    cell = userData.cell,             // Cell
                    picture = userData.picture.medium, // Profile picture URL
                    nationality = userData.nat        // Nationality
                )

                // Add the user to the list for insertion
                usersToInsert.add(user)
            }

            // Insert into the local repository (e.g., database) only if there are users
            if (usersToInsert.isNotEmpty()) {
                localRepository.insertUsers(usersToInsert)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e("insertResponseInDatabase", "Error: ${e.message}")
        }
    }



    fun getUsers(): Flow<PagingData<User>> {
        return Pager(PagingConfig(pageSize = 20)) {
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
            .cachedIn(scope)
    }
}
