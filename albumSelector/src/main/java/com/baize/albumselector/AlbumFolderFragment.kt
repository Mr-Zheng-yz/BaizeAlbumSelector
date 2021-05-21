package com.baize.albumselector

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.baize.albumselector.bean.MediaFolder
import com.baize.albumselector.extensin.dp
import com.baize.albumselector.view.LinearVerticalDecoration
import com.bumptech.glide.Glide

class AlbumFolderFragment : Fragment() {

    private val folderCoverView: View by lazy {
        requireView().findViewById<View>(R.id.album_folder_background)
    }
    private val folderRecyclerView: RecyclerView? by lazy {
        requireView().findViewById<RecyclerView>(R.id.folder_recycle_view)
    }
    private val mediaFolderAdapter: MediaFolderAdapter by lazy {
        MediaFolderAdapter()
    }
    private lateinit var albumViewModel : AlbumViewModel
    private lateinit var allImageFolders : ArrayList<MediaFolder>

    //选择回调
    var selectFolderListener: ((selectMediaFolder: MediaFolder) -> Unit)? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_folder_album, null, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        albumViewModel = ViewModelProvider(requireActivity(), ViewModelProvider.NewInstanceFactory()).get(AlbumViewModel::class.java)
//        allImageFolders = albumViewModel.allMediaFolders
//        allImageFolders = ((activity as? AlbumSelectorActivity)?.getAllImageFolders())
//            ?: mutableListOf<MediaFolder>()
        folderRecyclerView?.apply {
            layoutManager = LinearLayoutManager(context)
            addItemDecoration(
                LinearVerticalDecoration(
                    0.5F.dp.toInt(),
                    Color.parseColor("#b0555555"),
                    60F.dp.toInt()
                )
            )
            adapter = mediaFolderAdapter
        }
        folderCoverView.setOnClickListener { parentFragment?.childFragmentManager?.popBackStackImmediate() }
    }

    override fun onCreateAnimation(transit: Int, enter: Boolean, nextAnim: Int): Animation? {
        if (nextAnim == 0) return super.onCreateAnimation(transit, enter, nextAnim)
        val animation: Animation? = AnimationUtils.loadAnimation(activity, nextAnim)
        animation?.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationRepeat(animation: Animation?) {
            }

            override fun onAnimationEnd(animation: Animation?) {
                if (enter) {
                    folderCoverView?.visibility = View.VISIBLE
                }
            }

            override fun onAnimationStart(animation: Animation?) {
                if (!enter) {
                    folderCoverView?.visibility = View.GONE
                }
            }
        })
        return animation
    }

    inner class MediaFolderAdapter :
        RecyclerView.Adapter<MediaFolderAdapter.MediaFolderViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MediaFolderViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.adapter_album_folder_item, parent, false)
            return MediaFolderViewHolder(view)
        }

        override fun onBindViewHolder(holder: MediaFolderViewHolder, position: Int) {
            val bean = albumViewModel.allMediaFolders[position]
            Glide.with(holder.folderImage)
                .load(bean.cover?.path)
                .into(holder.folderImage)
            holder.folderName.text = bean.name ?: ""
            holder.folderFileCount.text = bean.showImagesCount()
            holder.itemView.setOnClickListener {
                selectFolderListener?.invoke(bean)
                parentFragment?.childFragmentManager?.popBackStackImmediate()
            }
        }

        override fun getItemCount() = albumViewModel.allMediaFolders.size

        //媒体文件ViewHolder
        inner class MediaFolderViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val folderImage: ImageView = view.findViewById(R.id.folder_image)
            val folderName: TextView = view.findViewById(R.id.folder_name)
            val folderFileCount: TextView = view.findViewById(R.id.folder_file_count)
        }
    }
}