package com.android.peoplefinder.interfaces

import com.android.peoplefinder.network.NetworkResult

interface ApiListeners {
    fun onSuccess(networkResult: NetworkResult<Any>)
    fun onFailure(networkResult: NetworkResult<Any>)
    fun onLoading(networkResult: NetworkResult<Any>)
}