package com.android.peoplefinder.activity

import NetworkProvider
import android.Manifest
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Resources
import android.graphics.Rect
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.os.Parcelable
import android.provider.Settings
import android.util.Log
import android.view.View
import com.bumptech.glide.Glide
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.widget.doAfterTextChanged
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.cachedIn
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.android.peoplefinder.activity.Db.AppDataBase
import com.android.peoplefinder.activity.repository.CommonRepository
import com.android.peoplefinder.activity.repository.LocalDBRepository
import com.android.peoplefinder.activity.view.BaseActivity
import com.android.peoplefinder.adapter.UserAdapter
import com.android.peoplefinder.databinding.ActivityMainBinding
import com.android.peoplefinder.helper.Constants.BASE_URL
import com.android.peoplefinder.helper.Constants.WEATHER_API_KEY
import com.android.peoplefinder.helper.Constants.WEATHER_API_URL
import com.android.peoplefinder.interfaces.ApiInterface
import com.android.peoplefinder.network.ApiExceptionHandler
import com.android.peoplefinder.viewmodel.CommonViewModel
import com.android.peoplefinder.viewmodel.CommonViewModelFactory
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.gson.Gson
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay

import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch


class MainActivity : BaseActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var viewModel: CommonViewModel
    private lateinit var userAdapter: UserAdapter
    private var searchJob: Job? = null
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var rvViewedState: Parcelable? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        savedInstanceState?.let {
            rvViewedState = it.getParcelable("rvViewedState")
        }
        setupViewModel()
        checkLocationPermission()
        setupSearchListener()
    }

    private fun setupViewModel() {
        val gson = Gson()
        val apiResponseDao = AppDataBase.getInstance(application).getLocalDBDao()
        val localRepository = LocalDBRepository(apiResponseDao)
        val factory = CommonViewModelFactory(
            commonRepository = CommonRepository(NetworkProvider.getApiService(this,BASE_URL), apiResponseDao),
            apiExceptionHandler = ApiExceptionHandler(gson),
            localRepository = localRepository,
            application = application
        )
        viewModel = ViewModelProvider(this, factory).get(CommonViewModel::class.java)



        userAdapter = UserAdapter { user ->

        }

        binding.rvUser.apply {
            layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
            adapter = userAdapter

        }
        rvViewedState?.let { state ->
            binding.rvUser.layoutManager?.onRestoreInstanceState(state)
        }
        val pager = Pager(
            config = PagingConfig(pageSize = 25, enablePlaceholders = false),
            pagingSourceFactory = { UserPagingSource(viewModel) }
        ).flow.cachedIn(lifecycleScope)

        lifecycleScope.launchWhenStarted {
            pager.collectLatest { pagingData ->
                userAdapter.submitData(pagingData)
            }
        }
    }

    private fun setupSearchListener() {
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

    private fun checkLocationPermission() {
        when {
            ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED -> {
                getCurrentLocation()
            }
            else -> {
                requestLocationPermission()
            }
        }
    }

    private fun getCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
            location?.let {
                val latitude = it.latitude
                val longitude = it.longitude
                fetchWeatherAndAirQuality(latitude, longitude)
            }
        }
    }
    private fun requestLocationPermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
            showLocationPermissionRationale()
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                REQUEST_LOCATION_PERMISSION
            )
        }
    }


    private fun fetchWeatherAndAirQuality(latitude: Double, longitude: Double) {
        val weatherApiService = NetworkProvider.getRetrofit(this, WEATHER_API_URL).create(ApiInterface::class.java)

        val params = hashMapOf(
            "lat" to latitude.toString(),
            "lon" to longitude.toString(),
            "appid" to WEATHER_API_KEY,
            "units" to "metric"
        )

        lifecycleScope.launch {
            try {
                val response = weatherApiService.getWeather(params)

                if (response.isSuccessful) {
                    val json = response.body()
                    val temp = json?.getAsJsonObject("main")?.get("temp")?.asDouble
                    val cityName = json?.get("name")?.asString
                    Log.d("CityName", "City Name: $cityName")
                    val description = json?.getAsJsonArray("weather")?.get(0)?.asJsonObject?.get("description")?.asString
                    val iconCode = json?.getAsJsonArray("weather")?.get(0)?.asJsonObject?.get("icon")?.asString

                    // Set values to toolbar views
                    binding.rltHeadTitle.tvTempCity.text = "${temp?.toInt()}° $cityName"  // Show the dynamic city name
                    binding.rltHeadTitle.tvCondition.text = description?.replaceFirstChar { it.uppercase() }

                    val iconUrl = "https://openweathermap.org/img/wn/${iconCode}@2x.png"
                    Glide.with(this@MainActivity)
                        .load(iconUrl)
                        .into(binding.rltHeadTitle.ivWeatherIcon)

                } else {
                    Toast.makeText(this@MainActivity, "Error: ${response.message()}", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(this@MainActivity, "Exception: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
        }
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
    private fun showLocationPermissionRationale() {
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Location Permission Needed")
            .setMessage("This app needs location access to show local weather and air quality.")
            .setPositiveButton("Allow") { dialog, _ ->
                dialog.dismiss()
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                    REQUEST_LOCATION_PERMISSION
                )
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .create()
            .show()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_LOCATION_PERMISSION) {
            if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                getCurrentLocation()
            } else {
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                    showLocationPermissionRationale()
                } else {
                    showSettingsRedirectDialog()
                }
            }
        }
    }
    private fun showSettingsRedirectDialog() {
        AlertDialog.Builder(this)
            .setTitle("Permission Denied")
            .setMessage("Location access is denied permanently. You can enable it from app settings.")
            .setPositiveButton("Open Settings") { dialog, _ ->
                dialog.dismiss()
                openAppSettings()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }
    private fun openAppSettings() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", packageName, null)
        }
        startActivity(intent)
    }


    fun Int.dpToPx(): Int = (this * Resources.getSystem().displayMetrics.density).toInt()

    companion object {
        private const val REQUEST_LOCATION_PERMISSION = 1
    }
}
