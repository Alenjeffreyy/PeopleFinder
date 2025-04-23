package com.android.peoplefinder.adapter

import android.content.Context
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.bumptech.glide.Glide
import com.android.peoplefinder.activity.Db.User
import com.android.peoplefinder.databinding.ItemUserBinding
import com.android.peoplefinder.helper.UserDiffCallback
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

        fun bind(user: User) {
            // Reset layout params
            binding.profileImage.layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.WRAP_CONTENT
            )

            Glide.with(binding.root)
                .load(user.picture)
                .listener(object : RequestListener<Drawable> {
                    override fun onResourceReady(
                        resource: Drawable,
                        model: Any,
                        target: Target<Drawable>,
                        dataSource: DataSource,
                        isFirstResource: Boolean
                    ): Boolean {
                        val width = binding.root.width
                        if (width > 0 && resource.intrinsicWidth > 0 && resource.intrinsicHeight > 0) {
                            val aspectRatio = resource.intrinsicWidth.toFloat() / resource.intrinsicHeight.toFloat()
                            val height = (width / aspectRatio).toInt()

                            // Apply to both CardView and ImageView
                            binding.cvImage.layoutParams.height = height
                            binding.profileImage.layoutParams.height = height
                            binding.cvImage.requestLayout()
                        }
                        return false
                    }

                    override fun onLoadFailed(
                        e: GlideException?,
                        model: Any?,
                        target: Target<Drawable>?,
                        isFirstResource: Boolean
                    ): Boolean = false
                })
                .into(binding.profileImage)

            binding.root.setOnClickListener { onItemClick(user) }
        }
    }
}

