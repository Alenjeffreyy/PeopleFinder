package com.android.peoplefinder.viewmodel

import android.content.Context
import android.widget.Toast
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.android.peoplefinder.R
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

import retrofit2.Response

import androidx.lifecycle.ViewModelProvider
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import kotlinx.coroutines.launch
import org.json.JSONObject


class CommonViewModel(
    application: Application,
    private val commonRepository: CommonRepository,
    private val apiExceptionHandler: ApiExceptionHandler,
    private val localRepository: LocalDBRepository
) : AndroidViewModel(application) {

    private val scope = viewModelScope
    val liveDataResponse = MutableLiveData<NetworkResult<Any>>()
    private var responseCode: Int? = null

    fun apiRequest(
        requestCode: Int,
        hashMap: HashMap<String, Any>,
    ) {
        liveDataResponse.value = NetworkResult.Loading(true, requestCode = requestCode)
        scope.launch {
            try {
                val response = when (requestCode) {
                    Enums.REQ_DATA -> commonRepository.getUserData(hashMap)
                    else -> return@launch
                }

                val handler = apiExceptionHandler.exceptionHandler(response as Response<Any>, getApplication())
                responseCode = requestCode

                if (handler.isSuccess) {
                    response.body()?.let { body ->
                        liveDataResponse.value = NetworkResult.Success(body, requestCode)
                        insertResponseInDatabase(Gson().toJson(body), requestCode)
                    }
                } else {
                    liveDataResponse.value = NetworkResult.Error(
                        handler.errorResonse,
                        data = response.body(),
                        requestCode = requestCode
                    )
                }
            } catch (e: Exception) {
                liveDataResponse.value = NetworkResult.Error(
                    e.message ?: "Unknown error",
                    requestCode = requestCode
                )
            } finally {
                liveDataResponse.value = NetworkResult.Loading(false, requestCode = requestCode)
            }
        }
    }

    private suspend fun insertResponseInDatabase(response: String, requestCode: Int) {
        try {
            val jsonResponse = JSONObject(response)
            val resultsArray = jsonResponse.getJSONArray("results")

            val usersToInsert = mutableListOf<User>()

            for (i in 0 until resultsArray.length()) {
                val userJson = resultsArray.getJSONObject(i)


                val user = User(
                    uuid = userJson.getJSONObject("login").getString("username"),
                    firstName = userJson.getJSONObject("name").getString("first"),
                    lastName = userJson.getJSONObject("name").getString("last"),
                    gender = userJson.getString("gender"),
                    location = "${userJson.getJSONObject("location").getString("street")} ${userJson.getJSONObject("location").getString("city")}, ${userJson.getJSONObject("location").getString("country")}",
                    email = userJson.getString("email"),
                    phone = userJson.getString("phone"),
                    cell = userJson.getString("cell"),
                    picture = userJson.getJSONObject("picture").getString("medium"),
                    nationality = userJson.getString("nat")
                )


                usersToInsert.add(user)
            }


            if (usersToInsert.isNotEmpty()) {
                localRepository.insertUsers(usersToInsert)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
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
