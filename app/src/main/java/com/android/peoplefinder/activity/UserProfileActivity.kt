package com.android.peoplefinder.activity

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.android.peoplefinder.R
import com.android.peoplefinder.activity.Db.User
import com.android.peoplefinder.activity.view.BaseActivity
import com.android.peoplefinder.databinding.ActivityMainBinding
import com.android.peoplefinder.databinding.ActivityUserProfileBinding
import com.android.peoplefinder.helper.Constants.WEATHER_API_KEY
import com.android.peoplefinder.helper.Constants.WEATHER_API_URL
import com.android.peoplefinder.interfaces.ApiInterface
import com.bumptech.glide.Glide
import kotlinx.coroutines.launch

class UserProfileActivity : BaseActivity() {

    private lateinit var binding: ActivityUserProfileBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUserProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initViews()

    }
    private fun initViews() {
        binding.headerTitle.tvTitle.visibility = View.GONE
        binding.headerTitle.weatherLayout.visibility = View.GONE
        binding.headerTitle.rltBackClose.visibility = View.VISIBLE
        binding.headerTitle.ivBack.visibility = View.VISIBLE
        binding.headerTitle.ivBack.setOnClickListener {
            finish()
        }
        val user = intent.getSerializableExtra("user") as? User

        user.let {
            if (it != null) {
                updateUI(it)
            }
        }

    }
    private fun updateUI(user: User){
        binding.apply {
            Glide.with(this@UserProfileActivity)
                .load(user.pictureLarge)
                .into(ivProfile)

            tvName.text = "${user.firstName} ${user.lastName}"
            tvEmail.text = user.email
            tvLocation.text = user.location
            tvPhone.text = user.phone
            tvNationality.text = user.nationality
            fetchWeatherAndAirQuality(user.latitude.toDouble(), user.longitude.toDouble())
        }

    }

    private fun fetchWeatherAndAirQuality(latitude: Double, longitude: Double) {
        val weatherApiService = NetworkProvider.getRetrofit(this, WEATHER_API_URL).create(
            ApiInterface::class.java)

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
                    val description = json?.getAsJsonArray("weather")?.get(0)?.asJsonObject?.get("description")?.asString
                    val temp = json?.getAsJsonObject("main")?.get("temp")?.asDouble
                    val cityName = json?.get("name")?.asString
                    val iconCode = json?.getAsJsonArray("weather")?.get(0)?.asJsonObject?.get("icon")?.asString

                    binding.tvTemperature.text = "Temp: ${temp?.toInt()}°C"
                    binding.tvLocation.text = cityName

                    binding.tvAirQuality.text = description?.replaceFirstChar { it.uppercase() }


                    val iconUrl = "https://openweathermap.org/img/wn/${iconCode}@2x.png"
                    Glide.with(this@UserProfileActivity)
                        .load(iconUrl)
                        .into(binding.icWeatherIcon)


                } else {
                    Toast.makeText(this@UserProfileActivity, "Error: ${response.message()}", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(this@UserProfileActivity, "Exception: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
        }

    }

}