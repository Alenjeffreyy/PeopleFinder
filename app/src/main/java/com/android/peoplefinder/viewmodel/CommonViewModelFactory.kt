package com.android.peoplefinder.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.android.peoplefinder.activity.repository.CommonRepository
import com.android.peoplefinder.activity.repository.LocalDBRepository
import com.android.peoplefinder.network.ApiExceptionHandler

class CommonViewModelFactory(
    private val application: Application,
    private val commonRepository: CommonRepository,
    private val apiExceptionHandler: ApiExceptionHandler,
    private val localRepository: LocalDBRepository
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CommonViewModel::class.java)) {
            return CommonViewModel(
                application,
                commonRepository,
                apiExceptionHandler,
                localRepository
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}



