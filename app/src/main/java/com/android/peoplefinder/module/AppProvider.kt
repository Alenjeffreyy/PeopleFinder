package com.android.peoplefinder.module

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import androidx.room.Room
import com.android.peoplefinder.activity.Db.ApiResponseDao
import com.android.peoplefinder.activity.Db.AppDataBase

object AppProvider {
    private var application: Application? = null
    private var sharedPreferences: SharedPreferences? = null
    private var appDatabase: AppDataBase? = null

    fun init(application: Application) {
        this.application = application
    }

    fun getApplicationContext(): Context {
        return application?.applicationContext ?: throw IllegalStateException("AppProvider not initialized")
    }

    fun getSharedPreferences(): SharedPreferences {
        return sharedPreferences ?: getApplicationContext()
            .getSharedPreferences("sharedpref", Context.MODE_PRIVATE)
            .also { sharedPreferences = it }
    }

    fun getAppDatabase(): AppDataBase {
        return appDatabase ?: Room.databaseBuilder(
            getApplicationContext(),
            AppDataBase::class.java,
            "LocalDB"
        )
            .fallbackToDestructiveMigration()
            .build()
            .also { appDatabase = it }
    }

    fun getApiResponseDao(): ApiResponseDao {
        return getAppDatabase().getLocalDBDao()
    }
}