package com.android.peoplefinder.dataclass

import android.icu.text.IDNA
import com.android.peoplefinder.activity.Db.User

data class Response (
    val results: List<com.android.peoplefinder.dataclass.getUser>,
    val info: Info
)
