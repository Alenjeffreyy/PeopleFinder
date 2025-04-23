package com.android.peoplefinder.activity.Db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "ApiResponse")
data class ApiResponse(
    @PrimaryKey(autoGenerate = false)
    var key: Int = 0,

    @ColumnInfo("response")
    var response: String = ""
)

