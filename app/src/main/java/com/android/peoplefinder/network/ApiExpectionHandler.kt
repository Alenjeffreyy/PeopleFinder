package com.android.peoplefinder.network

import android.content.Context
import android.widget.Toast
import com.android.peoplefinder.R
import com.google.gson.Gson
import retrofit2.HttpException
import retrofit2.Response


class ApiExceptionHandler(
    private val gson: Gson
) {
    suspend fun <T> exceptionHandler(
        response: retrofit2.Response<T>?,
        context: Context
    ): ApiResponseHandler {
        return try {
            when {
                response == null -> {
                    errorResponse(context.getString(R.string.internal_server_error))
                }

                response.isSuccessful && response.body() != null -> {
                    println("JSON RESPONSE: " + gson.toJson(response.body()))
                    ApiResponseHandler().apply {
                        isSuccess = true
                        errorResonse = ""
                    }
                }

                else -> {
                    errorHandling(response, context)
                }
            }
        } catch (e: HttpException) {
            errorResponse(e.message ?: "Unknown HTTP error")
        } catch (e: Throwable) {
            errorResponse(e.message ?: "Unknown error")
        }
    }

    private fun <T> errorHandling(
        response: retrofit2.Response<T>,
        context: Context
    ): ApiResponseHandler {
        return when {
            response.errorBody() == null -> {
                errorResponse(context.getString(R.string.internal_server_error))
            }

            response.code() == 401 || response.code() == 500 -> {
                Toast.makeText(context, context.getString(R.string.please_try_again), Toast.LENGTH_SHORT).show()
                errorResponse(context.getString(R.string.please_try_again))
            }

            response.code() == 404 || response.code() == 503 -> {
                errorResponse("${response.code()} ${response.message()}")
            }

            else -> {
                errorResponse(context.getString(R.string.internal_server_error))
            }
        }
    }

    private fun errorResponse(errorMessage: String): ApiResponseHandler {
        return ApiResponseHandler().apply {
            isSuccess = false
            errorResonse = errorMessage
        }
    }
}
