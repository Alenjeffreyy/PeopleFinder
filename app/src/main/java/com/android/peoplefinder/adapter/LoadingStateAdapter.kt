package com.android.peoplefinder.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.paging.LoadState
import androidx.paging.LoadStateAdapter
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.android.peoplefinder.R

class LoadingStateAdapter : LoadStateAdapter<LoadingStateAdapter.LoadingViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, loadState: LoadState): LoadingViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_loading, parent, false)
        return LoadingViewHolder(view)
    }

    override fun onBindViewHolder(holder: LoadingViewHolder, loadState: LoadState) {
        holder.bind(loadState)
    }

    class LoadingViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(loadState: LoadState) {
            itemView.isVisible = loadState is LoadState.Loading
        }
    }

    override fun onViewAttachedToWindow(holder: LoadingViewHolder) {
        super.onViewAttachedToWindow(holder)
        val layoutParams = holder.itemView.layoutParams
        if (layoutParams is StaggeredGridLayoutManager.LayoutParams) {
            layoutParams.isFullSpan = true
        }
    }
}
