package com.android.peoplefinder.helper

import androidx.recyclerview.widget.DiffUtil
import com.android.peoplefinder.activity.Db.User


class UserDiffCallback : DiffUtil.ItemCallback<User>() {
    override fun areItemsTheSame(oldItem: User, newItem: User): Boolean {
        return oldItem.uuid == newItem.uuid
    }

    override fun areContentsTheSame(oldItem: User, newItem: User): Boolean {
        return oldItem == newItem
    }
}
