package com.android.peoplefinder.activity

import NetworkProvider
import android.content.res.Resources
import android.graphics.Rect
import android.os.Bundle
import android.view.View
import androidx.core.widget.doAfterTextChanged
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.cachedIn
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.android.peoplefinder.activity.Db.AppDataBase
import com.android.peoplefinder.activity.Db.User
import com.android.peoplefinder.activity.repository.CommonRepository
import com.android.peoplefinder.activity.repository.LocalDBRepository
import com.android.peoplefinder.activity.view.BaseActivity
import com.android.peoplefinder.adapter.UserAdapter
import com.android.peoplefinder.databinding.ActivityMainBinding
import com.android.peoplefinder.helper.Enums
import com.android.peoplefinder.interfaces.ApiInterface
import com.android.peoplefinder.network.ApiExceptionHandler
import com.android.peoplefinder.network.NetworkResult
import com.android.peoplefinder.viewmodel.CommonViewModel
import com.android.peoplefinder.viewmodel.CommonViewModelFactory
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay

import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class MainActivity : BaseActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var viewModel: CommonViewModel
    private lateinit var userAdapter: UserAdapter
    private var searchJob: Job? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val gson = Gson()

        val apiResponseDao = AppDataBase.getInstance(application).getLocalDBDao()

        val localRepository = LocalDBRepository(apiResponseDao)
        val factory = CommonViewModelFactory(
            commonRepository = CommonRepository(NetworkProvider.getApiService(), apiResponseDao),
            apiExceptionHandler = ApiExceptionHandler(gson),
            localRepository = localRepository,
            application = application
        )


         viewModel = ViewModelProvider(this, factory).get(CommonViewModel::class.java)

        userAdapter = UserAdapter { user ->
            // handle click
        }

        binding.rvUser.apply {

            layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL) // 2 columns
            adapter = userAdapter
            addItemDecoration(MarginItemDecoration(12.dpToPx())) // 12dp spacing
        }

        val pager = Pager(
            config = PagingConfig(
                pageSize = 25,
                enablePlaceholders = false
            ),
            pagingSourceFactory = { UserPagingSource(viewModel) }
        ).flow.cachedIn(lifecycleScope)

        lifecycleScope.launchWhenStarted {
            pager.collectLatest { pagingData ->
                userAdapter.submitData(pagingData)
            }
        }





        binding.edtSearch.doAfterTextChanged { text ->
            searchJob?.cancel()
            searchJob = lifecycleScope.launch {
                delay(300)

                val query = text?.toString().orEmpty()

                if (query.isNotEmpty()) {
                    viewModel.searchUsers(query).collectLatest { pagingData ->
                        userAdapter.submitData(pagingData)
                    }
                } else {
                    viewModel.getUsers().collectLatest { pagingData ->
                        userAdapter.submitData(pagingData)
                    }
                }
            }
        }


    }


    private fun updateUserData(data: Any?) {
        val jsonData = if (data is String) data else Gson().toJson(data)
        try {
            // Convert the response data into a JsonObject
            val jsonObject = Gson().fromJson(jsonData, JsonObject::class.java)
            val responseJson = jsonObject.getAsJsonArray("results")

            // Parse the JSON into a List of User objects
            val type = object : TypeToken<List<User>>() {}.type


            // Get the DAO instance
            val localRepository = LocalDBRepository(AppDataBase.getInstance(application).getLocalDBDao())


            CoroutineScope(Dispatchers.IO).launch {
                // Insert all users at once
                localRepository.insertApiResponse(Gson().fromJson(responseJson, type))
            }

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onSuccess(networkResult: NetworkResult<Any>) {
        when (networkResult.requestCode) {

            Enums.REQ_DATA -> {

                updateUserData(networkResult.data)
            }

        }

    }

    override fun onFailure(networkResult: NetworkResult<Any>) {

    }

    override fun onLoading(networkResult: NetworkResult<Any>) {

    }
    class MarginItemDecoration(private val space: Int) : RecyclerView.ItemDecoration() {
        override fun getItemOffsets(
            outRect: Rect,
            view: View,
            parent: RecyclerView,
            state: RecyclerView.State
        ) {
            outRect.left = space / 2
            outRect.right = space / 2
            outRect.bottom = space
            outRect.top = space / 2
        }
    }
    fun Int.dpToPx(): Int = (this * Resources.getSystem().displayMetrics.density).toInt()
}
