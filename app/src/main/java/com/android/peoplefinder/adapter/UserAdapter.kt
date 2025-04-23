package com.android.peoplefinder.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.android.peoplefinder.R
import com.bumptech.glide.Glide
import com.android.peoplefinder.activity.Db.User
import com.android.peoplefinder.databinding.ItemUserBinding
import com.android.peoplefinder.helper.UserDiffCallback
import com.bumptech.glide.Glide.init
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target


class UserAdapter(
    private val onItemClick: (User) -> Unit
) : PagingDataAdapter<User, UserAdapter.UserViewHolder>(UserDiffCallback()) {




    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val binding = ItemUserBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return UserViewHolder(binding)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        getItem(position)?.let { user ->
            holder.bind(user)
        }
    }

    inner class UserViewHolder(private val binding: ItemUserBinding) :
        RecyclerView.ViewHolder(binding.root) {

        @SuppressLint("SetTextI18n")
        fun bind(user: User) {

            binding.tvName.text = "${user.firstName} ${user.lastName}"
            Glide.with(binding.root)
                .load(user.picture)
                .placeholder(R.drawable.ic_place_holder)
                .centerCrop()
                .into(binding.imgView)

            binding.root.setOnClickListener { onItemClick(user) }
        }
    }
}


