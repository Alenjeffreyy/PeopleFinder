package com.android.peoplefinder.activity.view

import android.content.Context
import android.content.res.Configuration
import android.content.res.Resources
import android.os.Bundle
import android.os.SystemClock
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.android.peoplefinder.helper.SessionManager
import com.android.peoplefinder.interfaces.ApiInterface
import com.android.peoplefinder.interfaces.ApiListeners
import com.android.peoplefinder.network.NetworkResult
import com.android.peoplefinder.viewmodel.CommonViewModel


 open class BaseActivity : AppCompatActivity(), ApiListeners {

    protected lateinit var sessionManager: SessionManager
    protected lateinit var apiInterface: ApiInterface
    protected var commonViewModel: CommonViewModel? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)



        val configuration: Configuration = resources.configuration
        configuration.fontScale = 0.85f
        resources.updateConfiguration(configuration, resources.displayMetrics)
    }


    /**
     * Solving multiple clicks problem
     */
    class SafeClickListener(
        private var defaultInterval: Int = 1000,
        private val onSafeCLick: (View) -> Unit
    ) : View.OnClickListener {
        private var lastTimeClicked: Long = 0
        override fun onClick(v: View) {
            if (SystemClock.elapsedRealtime() - lastTimeClicked < defaultInterval) {
                return
            }
            lastTimeClicked = SystemClock.elapsedRealtime()
            onSafeCLick(v)
        }
    }

    fun View.setSafeOnClickListener(onSafeClick: (View) -> Unit) {
        val safeClickListener = SafeClickListener {
            onSafeClick(it)
        }
        setOnClickListener(safeClickListener)
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        newConfig.fontScale = 1.0f
        resources.updateConfiguration(newConfig, resources.displayMetrics)
    }

     override fun onSuccess(networkResult: NetworkResult<Any>) {
         TODO("Not yet implemented")
     }

     override fun onFailure(networkResult: NetworkResult<Any>) {
         TODO("Not yet implemented")
     }

     override fun onLoading(networkResult: NetworkResult<Any>) {
         TODO("Not yet implemented")
     }
 }