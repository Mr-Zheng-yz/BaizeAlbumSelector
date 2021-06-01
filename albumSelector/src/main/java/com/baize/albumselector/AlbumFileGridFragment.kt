package com.baize.albumselector

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.graphics.Color
import android.os.Bundle
import android.provider.MediaStore
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.baize.albumselector.bean.MediaFolder
import com.baize.albumselector.bean.MediaItem
import com.baize.albumselector.extensin.*
import com.baize.albumselector.utils.FileUtil
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import kotlin.math.roundToInt

class AlbumFileGridFragment : Fragment(), OnImagesLoadedListener {
    companion object {
        fun getInstance(bundle: Bundle?): AlbumFileGridFragment {
            val fragment = AlbumFileGridFragment()
            fragment.arguments = bundle
            return fragment
        }
    }

    private val empty_layout: TextView by lazy {
        requireView().findViewById(R.id.empty_layout)
    }
    private val loadingBar: ProgressBar by lazy {
        requireView().findViewById<ProgressBar>(R.id.loading_bar)
    }
    private val fileSelectNumber: TextView by lazy {
        requireView().findViewById<TextView>(R.id.file_select_number)
    }
    private val tipArrow: ImageView by lazy {
        requireView().findViewById<ImageView>(R.id.common_toolbar_title_arrow)
    }
    private val customBack: ImageView by lazy {
        requireView().findViewById<ImageView>(R.id.album_custom_back)
    }
    private val toolbarContentView: View by lazy {
        requireView().findViewById<View>(R.id.common_toolbar_content_view)
    }
    private val toolbarTitle: TextView by lazy {
        requireView().findViewById<TextView>(R.id.common_toolbar_title)
    }
    private val albumRecyclerView: RecyclerView by lazy {
        requireView().findViewById(R.id.comment_recycle_view)
    }
    private val fileSelectFinishView: LinearLayout by lazy {
        requireView().findViewById<LinearLayout>(R.id.select_file_finish_view)
    }
    private var orderByCreateLayout: View? = null
    private var orderByModifyLayout: View? = null
    private var gridWith = 0
    private val albumAdapter by lazy { AlbumAdapter() }
    private lateinit var albumViewModel: AlbumViewModel

    private var onLocalImageFileClickListener: OnLocalImageFileClickListener? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        onLocalImageFileClickListener = if (OnLocalImageFileClickListener::class.java.isAssignableFrom(activity!!::class.java)) activity as OnLocalImageFileClickListener else null
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_file_grid, null, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        albumViewModel = ViewModelProvider(requireActivity(),ViewModelProvider.NewInstanceFactory()).get(AlbumViewModel::class.java)
        albumViewModel.selectedMediaFiles.observeForever { selectFiles ->
            Log.i("yanze", "收到通知... ${selectFiles.size}")
            albumAdapter.notifyDataSetChanged()
            fileSelectNumber.text = ("(${selectFiles.size}/${albumViewModel.albumSelectConfig.maxSelectLimit})")
        }
        Log.i("yanze","viewModel: == Activity#VieModel? ${albumViewModel == (activity as? AlbumSelectorActivity)?.albumViewModel}")
        albumRecyclerView.apply {
            layoutManager = GridLayoutManager(context, 4, GridLayoutManager.VERTICAL, false)
            adapter = albumAdapter
        }
        gridWith = ((Resources.getSystem().displayMetrics.widthPixels - 5.dp) * 0.25f).roundToInt()
        fileSelectNumber.text = ("(${albumViewModel.selectFileSize}/${albumViewModel.albumSelectConfig.maxSelectLimit})")
        orderByCreateLayout = requireView().findViewById(R.id.order_by_create_layout)
        orderByModifyLayout = requireView().findViewById(R.id.order_by_modify_layout)
        orderByCreateLayout?.setOnClickListener {
            if (TextUtils.equals(albumViewModel.currentSortOrder, MediaStore.Images.Media.DATE_ADDED + " DESC")) {
                return@setOnClickListener
            }
            albumViewModel.currentSortOrder = MediaStore.Images.Media.DATE_ADDED + " DESC"
            showLoading()
            queryImages(albumViewModel.currentImageFolder?.path)
        }
        orderByModifyLayout?.setOnClickListener {
            if (TextUtils.equals(albumViewModel.currentSortOrder, MediaStore.Images.Media.DATE_MODIFIED + " DESC")) {
                return@setOnClickListener
            }
            albumViewModel.currentSortOrder = MediaStore.Images.Media.DATE_MODIFIED + " DESC"
            showLoading()
            queryImages(albumViewModel.currentImageFolder?.path)
        }
        fileSelectFinishView.setOnClickListener {
            if (albumViewModel.albumSelectConfig.autoFinishActivity) {
                if (albumViewModel.selectFileSize > 0) {
                    val intent = Intent()
                    intent.putStringArrayListExtra(MultimediaTools.SELECTOR_PATH, albumViewModel.getSelectedMediaFiles())
                    activity?.setResult(Activity.RESULT_OK, intent)
                }
                requireActivity().finish()
            } else {
                MultimediaTools.selectCompleteListener?.invoke(albumViewModel.getSelectedMediaFiles())
            }
        }
        toolbarContentView.setOnClickListener {
            if (albumViewModel.allMediaFolders.size < 2) {
                return@setOnClickListener
            }
            if (childFragmentManager.backStackEntryCount > 0) {
                tipArrow.setImageResource(R.drawable.album_down_arrow)
                childFragmentManager.popBackStackImmediate()
            } else {
                tipArrow.setImageResource(R.drawable.album_down_arrow)
                val bundle = Bundle()
                val albumFolderFragment = AlbumFolderFragment()
                albumFolderFragment.selectFolderListener = ::localMediaFolderChange
                albumFolderFragment.arguments = bundle
                childFragmentManager.beginTransaction()
                    .setCustomAnimations(
                        R.anim.anim_sild_down_in,
                        R.anim.anim_sild_up_exit,
                        R.anim.anim_sild_down_in,
                        R.anim.anim_sild_up_exit
                    )
                    .add(R.id.fragment_folder_page, albumFolderFragment)
                    .addToBackStack(null)
                    .commitAllowingStateLoss()
            }
        }
        customBack.setOnClickListener {
            activity?.finish()
        }
        requireActivity().checkAndRequestPermission(
            Manifest.permission.READ_EXTERNAL_STORAGE,
            111
        ) {
            queryImages()
        }
    }

    private fun localMediaFolderChange(selectMediaFolder: MediaFolder) {
        if (albumViewModel.currentImageFolder == selectMediaFolder) {
            return
        }
        albumViewModel.currentImageFolder = selectMediaFolder
        toolbarTitle.text = albumViewModel.currentImageFolder?.name ?: ""
        albumAdapter.datas = selectMediaFolder.images
        albumAdapter.notifyDataSetChanged()
        if (albumAdapter.datas.size == 0) {
            showEmpty()
        } else {
            showContent()
        }
    }

    private fun queryImages(folderPath: String? = null) {
        val imageDataSource = AlbumDataSource(requireContext(), loadedListener = this, lifecycle = lifecycle)
        imageDataSource.query(
            folderPath = folderPath,
            sortOrder = albumViewModel.currentSortOrder,
            supportGif = albumViewModel.albumSelectConfig.supportGif,
            supportVideo = albumViewModel.albumSelectConfig.supportVideo,
            defaultVideo = albumViewModel.albumSelectConfig.isDefaultVideo
        )
    }

    override fun onImagesLoaded(imageFolders: MutableList<MediaFolder>) {
        albumViewModel.allMediaFolders = imageFolders
        if (imageFolders.isNotEmpty()) {
            albumViewModel.currentImageFolder = imageFolders[0]
            setAdapterData(albumViewModel.currentImageFolder)
            if (albumViewModel.allMediaFolders.size > 1) {
                tipArrow.setImageResource(R.drawable.album_down_arrow)
                tipArrow.visibility = View.VISIBLE
            }
            showContent()
        }else{
            showEmpty()
        }
        toolbarTitle.text = albumViewModel.currentImageFolder?.name ?: ""
    }

    private fun setAdapterData(imageFiles: MediaFolder?) {
        if (imageFiles?.images?.isEmpty() == true) {
            return
        }
        albumAdapter.datas.clear()
        albumAdapter.datas.addAll(imageFiles?.images!!)
        albumAdapter.notifyDataSetChanged()
    }

    private fun showEmpty() {
        empty_layout.visibility = View.VISIBLE
        loadingBar.visibility = View.GONE
        albumRecyclerView.visibility = View.GONE
    }

    private fun showContent() {
        albumRecyclerView.visibility = View.VISIBLE
        loadingBar.visibility = View.GONE
        empty_layout.visibility = View.GONE
    }

    private fun showLoading() {
        loadingBar.visibility = View.VISIBLE
        empty_layout.visibility = View.GONE
        albumRecyclerView.visibility = View.GONE
    }

    val selectCache = arrayListOf<Int>()

    /**
     * 适配器
     */
    inner class AlbumAdapter : RecyclerView.Adapter<AlbumAdapter.AlbumViewHolder>() {
        var datas = ArrayList<MediaItem>()

        override fun onCreateViewHolder(
            parent: ViewGroup,
            viewType: Int
        ): AlbumAdapter.AlbumViewHolder {
            var view =
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.adapter_album_file_item, parent, false)
            return AlbumViewHolder(view)
        }

        override fun onBindViewHolder(holder: AlbumAdapter.AlbumViewHolder, position: Int) {
            holder.bindView(datas[position], position)
        }

        override fun getItemCount() = datas.size

        inner class AlbumViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val fileItemView: ConstraintLayout = view.findViewById(R.id.file_image_view)
            val fileSelectView: View = view.findViewById(R.id.file_select_view)
            val indexTextView: TextView = view.findViewById(R.id.file_select_order)
            val fileImageView: ImageView = view.findViewById(R.id.file_image)
            val fileDebugInfo: TextView = view.findViewById(R.id.file_debug_info)
            val frameBard: FrameLayout = view.findViewById(R.id.file_frame_bard)
            fun bindView(data: MediaItem, position: Int) {
                val layoutParams = fileItemView.layoutParams
                layoutParams.width = gridWith
                layoutParams.height = gridWith
                fileItemView.layoutParams = layoutParams
                if (BuildConfig.DEBUG) {
                    fileDebugInfo?.visibility = View.VISIBLE
                    fileDebugInfo?.text =
                        "大小: ${FileUtil.convertStorage(FileUtil.getFileSize(data.path))}" +
                                "\n宽高比: ${
                                    FileUtil.formatNumberJudgeInteger(
                                        data.dimensionRatio(),
                                        "0.000"
                                    )
                                } " +
                                "\n添加时间: ${data.getAddDateText()}" +
                                "\n修改时间: ${data.getModifyDateText()}"
                }
                val index = albumViewModel.getSelectedMediaFiles().indexOf(data.path)
                if (index >= 0) {
                    frameBard?.setBackgroundColor(Color.parseColor("#99000000"))
                    indexTextView?.setBackgroundResource(R.drawable.album_fill_circle)
                    indexTextView?.text = "${(index + 1)}"
                } else {
                    frameBard?.setBackgroundColor(Color.parseColor("#33000000"))
                    indexTextView?.setBackgroundResource(R.drawable.album_empty_circle)
                    indexTextView?.text = ""
                }
                //加载图片
                loadImage(fileImageView, data.path)
                fileSelectView?.setOnClickListener {
//                    val tempSelectedFiles = albumViewModel.getSelectedMediaFiles()
                    val checked = albumViewModel.getSelectedMediaFiles().contains(data.path)
                    if (checked) {
                        albumViewModel.getSelectedMediaFiles().remove(data.path)
//                        tempSelectedFiles.remove(data.path)
//                        selectPhotonFiles.remove(data.path)
                        if (selectCache.lastOrNull() == position) {
                            selectCache.removeLast()
                        } else {
                            selectCache.remove(position)
                            selectCache.forEach { notifyItemChanged(it) }
                        }
                        notifyItemChanged(position)
                    } else {
                        if (albumViewModel.albumSelectConfig.maxSelectLimit == 1) {
                            if (!albumViewModel.albumSelectConfig.checkFileAvailable(data)) return@setOnClickListener
                            val selectPhotonFilePath = if (albumViewModel.getSelectedMediaFiles().isNotEmpty()) {
                                albumViewModel.getSelectedMediaFiles()[0]
//                                tempSelectedFiles[0]
                            } else {
                                ""
                            }
                            albumViewModel.getSelectedMediaFiles().clear()
                            if (!TextUtils.isEmpty(selectPhotonFilePath)) {
                                notifyItemChanged(position)
                            }
                        } else {
                            if (albumViewModel.getSelectedMediaFiles().size >= albumViewModel.albumSelectConfig.maxSelectLimit) {
                                Toast.makeText(
                                    context,
                                    "最多只能选${albumViewModel.albumSelectConfig.maxSelectLimit}张图片",
                                    Toast.LENGTH_SHORT
                                ).show()
                                return@setOnClickListener
                            }
                            if (!albumViewModel.albumSelectConfig.checkFileAvailable(data)) return@setOnClickListener
//                            if (!CheckFileAvailable.checkFileAvailable(data)) return@setOnClickListener
                        }
                        selectCache.add(position)
                        albumViewModel.getSelectedMediaFiles().add(albumViewModel.selectFileSize,data.path)
                        notifyItemChanged(position)
                    }
                    fileSelectNumber.text = ("(${albumViewModel.getSelectedMediaFiles().size}/${albumViewModel.albumSelectConfig.maxSelectLimit})")
                }
                fileImageView.setOnClickListener {
                    albumViewModel.currentPosition = position
                    albumViewModel.currentImageFolder?.let {
                        onLocalImageFileClickListener?.onLocalImageFileClick(
                            position,
                            albumViewModel.albumSelectConfig.maxSelectLimit,
                            albumViewModel.albumSelectConfig.autoFinishActivity,
                            it,
                            albumViewModel.albumSelectConfig.supportGif,
                            albumViewModel.albumSelectConfig.supportVideo,
                            albumViewModel.currentSortOrder,
                            isDefaultVideo = albumViewModel.albumSelectConfig.isDefaultVideo,
                            )
                    }
                }
            }

            fun loadImage(iv: ImageView, path: String) {
                val requestOptions = RequestOptions()
                Glide.with(iv)
                    .load(path)
                    .placeholder(R.drawable.placeholder_rectangle)
                    .into(iv)
            }
        }

    }

}

interface OnLocalImageFileClickListener {
    fun onLocalImageFileClick(
        position: Int,
        limitNumber: Int,
        autoFinishActivity: Boolean,
        currentImageFolder: MediaFolder,
        supportGif: Boolean,
        supportVideo: Boolean,
        currentSortOrder: String,
        isDefaultVideo: Boolean
    )
}