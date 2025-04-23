package com.android.peoplefinder.activity

import NetworkProvider
import android.content.res.Resources
import android.graphics.Rect
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doAfterTextChanged
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import androidx.recyclerview.widget.StaggeredGridLayoutManager.GAP_HANDLING_MOVE_ITEMS_BETWEEN_SPANS
import androidx.recyclerview.widget.StaggeredGridLayoutManager.VERTICAL
import com.android.peoplefinder.activity.Db.AppDataBase
import com.android.peoplefinder.activity.Db.User
import com.android.peoplefinder.activity.repository.CommonRepository
import com.android.peoplefinder.activity.repository.LocalDBRepository
import com.android.peoplefinder.activity.view.BaseActivity
import com.android.peoplefinder.adapter.LoadingStateAdapter
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

import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch



class MainActivity : BaseActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var viewModel: CommonViewModel
    private lateinit var userAdapter: UserAdapter

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
        getUserDetails()
        userAdapter = UserAdapter { user ->
            // handle click
        }

        binding.rvUser.apply {
            layoutManager = GridLayoutManager(context, 2) // 2 columns
            adapter = userAdapter
            addItemDecoration(MarginItemDecoration(12.dpToPx())) // 12dp spacing
        }

        // Item decoration class


        // Extension to convert dp to pixels



        lifecycleScope.launch {
            viewModel.pagedUsers.collectLatest { pagingData ->
                userAdapter.submitData(pagingData)
            }
        }


        binding.edtSearch.doAfterTextChanged { text ->
            text?.let {
                if (it.isNotEmpty()) {
                    lifecycleScope.launch {
                        viewModel.searchUsers(it.toString()).collectLatest { pagingData ->
                            userAdapter.submitData(pagingData)
                        }
                    }
                } else {

                }
            }
        }
    }

    private fun getUserDetails(){
        val hashMap = HashMap<String, Any>().apply {
            put("results", 25)

        }
        viewModel.apiRequest(Enums.REQ_DATA, hashMap)
    }

    private fun updateUserData(data: Any?) {
        val jsonData = if (data is String) data else Gson().toJson(data)
        try {
            // Convert the response data into a JsonObject
            val jsonObject = Gson().fromJson(jsonData, JsonObject::class.java)
            val responseJson = jsonObject.getAsJsonArray("results")

            // Parse the JSON into a List of User objects
            val type = object : TypeToken<List<User>>() {}.type
            val users: List<User> = Gson().fromJson(responseJson, type)

            // Get the DAO instance
            val localRepository = LocalDBRepository(AppDataBase.getInstance(application).getLocalDBDao())


            CoroutineScope(Dispatchers.IO).launch {
                // Insert all users at once
                localRepository.insertUsers(users)
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
