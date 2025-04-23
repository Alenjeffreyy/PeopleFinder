package com.android.peoplefinder.activity.Db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class User(
    @PrimaryKey(autoGenerate = true)
    val slNo: Long = 0,
    val uuid: String,
    @ColumnInfo(name = "first_name")
    val firstName: String,
    @ColumnInfo(name = "last_name")
    val lastName: String,
    val gender: String,
    val location: String,
    val email: String,
    val phone: String,
    val cell: String,
    val picture: String,
    val nationality: String

)