package com.android.peoplefinder.activity

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.android.peoplefinder.activity.Db.User
import com.android.peoplefinder.activity.repository.CommonRepository
import com.android.peoplefinder.activity.view.BaseActivity
import com.android.peoplefinder.dataclass.getUser
import com.android.peoplefinder.helper.Enums
import com.android.peoplefinder.interfaces.ApiInterface
import com.android.peoplefinder.network.NetworkResult
import com.android.peoplefinder.viewmodel.CommonViewModel

class UserPagingSource(private val viewModel: CommonViewModel) : PagingSource<Int, User>() {

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, User> {
        val page = params.key ?: 1
        return try {
            val result = viewModel.apiRequestSuspended(
                Enums.REQ_DATA,
                hashMapOf("page" to page, "results" to params.loadSize)
            )

            when (result) {
                is NetworkResult.Success -> {
                    val getUserList = result.data as? List<getUser> ?: emptyList()
                    val userList = getUserList.map { mapToUser(it) }

                    LoadResult.Page(
                        data = userList,
                        prevKey = if (page == 1) null else page - 1,
                        nextKey = if (userList.isEmpty()) null else page + 1
                    )
                }

                is NetworkResult.Error -> {
                    LoadResult.Error(Exception(result.message ?: "Unknown API error"))
                }

                else -> {
                    LoadResult.Error(Exception("Unexpected result type"))
                }
            }

        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }

    fun mapToUser(apiUser: getUser): User {
        return User(
            uuid = apiUser.login.username,
            firstName = apiUser.name.first,
            lastName = apiUser.name.last,
            gender = apiUser.gender,
            location = "${apiUser.location.street.number} ${apiUser.location.street.name}, ${apiUser.location.city}, ${apiUser.location.country}",
            email = apiUser.email,
            phone = apiUser.phone,
            cell = apiUser.cell,
            picture = apiUser.picture.medium,
            nationality = apiUser.nat
        )
    }

    override fun getRefreshKey(state: PagingState<Int, User>): Int? {
        return state.anchorPosition?.let { position ->
            val anchorPage = state.closestPageToPosition(position)
            anchorPage?.prevKey?.plus(1) ?: anchorPage?.nextKey?.minus(1)
        }
    }
}




