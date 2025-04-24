package com.android.peoplefinder.adapter

import android.annotation.SuppressLint
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.RecyclerView
import com.android.peoplefinder.R
import com.android.peoplefinder.activity.Db.User
import com.android.peoplefinder.databinding.ItemUserBinding
import com.android.peoplefinder.helper.UserDiffCallback
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition


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

            val imageUrl = when (absoluteAdapterPosition % 3) {
                0 -> user.pictureLarge
                1 -> user.pictureMedium
                else -> user.pictureThumbnail
            }
            val orientation = binding.root.context.resources.configuration.orientation

            Glide.with(binding.root.context)
                .asBitmap()
                .load(imageUrl)
                .placeholder(R.drawable.ic_place_holder)
                .into(object : CustomTarget<Bitmap>() {
                    override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                        val width = resource.width
                        val height = resource.height
                        Log.d("ImageResolution", "Loaded image resolution: ${width}x$height")

                        val layoutParams = binding.imgView.layoutParams
                        when {
                            width == 72 && height == 72 -> {
                                layoutParams.width = if (orientation == Configuration.ORIENTATION_LANDSCAPE) 950 else 500
                                layoutParams.height = if (orientation == Configuration.ORIENTATION_LANDSCAPE) 850 else 400
                            }
                            width == 128 && height == 128 -> {
                                layoutParams.width = if (orientation == Configuration.ORIENTATION_LANDSCAPE) 1050 else 650
                                layoutParams.height = if (orientation == Configuration.ORIENTATION_LANDSCAPE) 1050 else 650
                            }
                            else -> {
                                layoutParams.width = if (orientation == Configuration.ORIENTATION_LANDSCAPE) 800 else 500
                                layoutParams.height = if (orientation == Configuration.ORIENTATION_LANDSCAPE) 650 else 250
                            }
                        }

                        binding.imgView.layoutParams = layoutParams
                        binding.imgView.setImageBitmap(resource)
                    }

                    override fun onLoadCleared(placeholder: Drawable?) {
                        binding.imgView.setImageDrawable(placeholder)
                    }
                })

            binding.root.setOnClickListener { onItemClick(user) }
        }

    }
}


