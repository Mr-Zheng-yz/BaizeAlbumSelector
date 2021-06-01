package com.baize.baizealbumselector

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class ImageAdapter : RecyclerView.Adapter<ImageAdapter.ImageViewHolder>(){
    var datas:ArrayList<String> = arrayListOf()

    inner class ImageViewHolder(var itemView: View) : RecyclerView.ViewHolder(itemView) {
        var ivImage:ImageView = itemView.findViewById(R.id.iv_image)
        var tvPath:TextView = itemView.findViewById(R.id.tv_path)
        fun bindData(url: String) {
            Glide.with(ivImage)
                .load(url)
                .into(ivImage)
            tvPath.text = "路径:${url}"
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.image_item, parent, false)
        return ImageViewHolder(view)
    }

    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
        holder.bindData(datas[position])
    }

    override fun getItemCount() = datas.size

}