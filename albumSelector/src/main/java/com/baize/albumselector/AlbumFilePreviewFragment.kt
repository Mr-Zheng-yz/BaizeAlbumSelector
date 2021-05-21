package com.baize.albumselector

import android.app.Activity
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager
import com.baize.albumselector.bean.MediaFolder
import com.baize.albumselector.bean.MediaItem
import com.baize.albumselector.extensin.getArgumentsArrayStringList
import com.baize.albumselector.extensin.getArgumentsBoolean
import com.baize.albumselector.extensin.getArgumentsInt
import com.baize.albumselector.extensin.getArgumentsString
import com.baize.albumselector.utils.CheckFileAvailable
import com.baize.albumselector.view.CustomVideoView
import com.bumptech.glide.Glide

class AlbumFilePreviewFragment : Fragment(), OnImagesLoadedListener,
    ViewPager.OnPageChangeListener {
    companion object {
        fun getInstance(): AlbumFilePreviewFragment {
            return AlbumFilePreviewFragment()
        }
    }

    private val fileSelectView: LinearLayout by lazy {
        requireView().findViewById<LinearLayout>(R.id.file_select_view)
    }
    private val fileSelectNumber: TextView by lazy {
        requireView().findViewById<TextView>(R.id.file_select_number)
    }
    private val albumFolderInfo: TextView? by lazy {
        requireView().findViewById<TextView>(R.id.album_folder_info)
    }
    private val albumSelectInfo: TextView? by lazy {
        requireView().findViewById<TextView>(R.id.album_select_info)
    }
    private val albumSelectInfoImage: ImageView by lazy {
        requireView().findViewById<ImageView>(R.id.album_select_info_image)
    }
    private val mViewpager: ViewPager by lazy {
        requireView().findViewById(R.id.image_viewpager)
    }
    private val customBack: ImageView by lazy {
        requireView().findViewById(R.id.album_custom_back)
    }
    private lateinit var albumViewModel : AlbumViewModel
    private var mPhotoViewAdapter: ImageOrVideoPreviewAdapter? = null
    //播放控制
    private var currentPlayVideoIndex = -1

//    private var currentMediaItemList: ArrayList<MediaItem> = arrayListOf()
//    private var limitNumber = 1
//    private var selectPhotonFiles = arrayListOf<String>()
//    private var viewPagerLastItemPosition = 0
//    private var autoFinishActivity = true
//    private var supportGif = false
//    private var supportVideo = falses
//    private var defaultVideo = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_file_album_pre_view_pager, null, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        albumViewModel = ViewModelProvider(requireActivity(),
            ViewModelProvider.NewInstanceFactory()).get(AlbumViewModel::class.java)

        mViewpager.addOnPageChangeListener(this)
//        albumViewModel.albumSelectConfig.maxSelectLimit = getArgumentsInt(MultimediaTools.MAX_LIMIT, 9)
//        selectPhotonFiles = getArgumentsArrayStringList(MultimediaTools.SELECTOR_PATH)
//        viewPagerLastItemPosition = getArgumentsInt(MultimediaTools.CURRENT_POSITION, 0)
//        val imageFolderPath = getArgumentsString(MultimediaTools.IMAGE_FOLDER_PATH, "")
//        autoFinishActivity = getArgumentsBoolean(MultimediaTools.AUTO_FINISH_ACTIVITY, true)
//        supportGif = getArgumentsBoolean(MultimediaTools.SUPPORT_GIF, false)
//        supportVideo = getArgumentsBoolean(MultimediaTools.SUPPORT_VIDEO, false)
//        defaultVideo = getArgumentsBoolean(MultimediaTools.DEFAULT_VIDEO, false)
        fileSelectNumber.text = ("(${albumViewModel.getSelectedMediaFiles().size}/$albumViewModel.albumSelectConfig.maxSelectLimit)")
        customBack.setOnClickListener {
            activity?.onBackPressed()
        }
        albumSelectInfoImage.setOnClickListener {
            onSelectViewClick()
        }
        albumSelectInfo?.setOnClickListener {
            onSelectViewClick()
        }
        queryImages(albumViewModel.currentImageFolder?.path ?: "", albumViewModel.currentSortOrder)
    }

    private fun queryImages(folderPath: String, sortOrder: String) {
        if (albumViewModel.allMediaFolders.size == 0) {
//            showCoverLoading()
            val imageDataSource = AlbumDataSource(requireContext(), this, lifecycle = lifecycle)
            imageDataSource.query(folderPath, sortOrder,
                albumViewModel.albumSelectConfig.supportGif, albumViewModel.albumSelectConfig.supportVideo, albumViewModel.albumSelectConfig.isDefaultVideo)
        } else {
            albumViewModel.allMediaFolders.find {
                TextUtils.equals(it.path, folderPath)
            }?.let { mediaFolder ->
                val imageFolders = mutableListOf<MediaFolder>()
                imageFolders.add(mediaFolder)
                onImagesLoaded(imageFolders)
            }
        }
    }

    private fun onSelectViewClick() {
        if (albumViewModel.currentPosition >= albumViewModel.getCurrentMediaItemList().size) {
            return
        }
        val currentFilePath = albumViewModel.getCurrentMediaItem()?.path ?: ""
        val checked = albumViewModel.getSelectedMediaFiles().contains(currentFilePath)
        val tempSelectFiles = albumViewModel.selectedMediaFiles.value ?: arrayListOf()
        if (checked) {
//            albumViewModel.getSelectedMediaFiles().remove(currentFilePath)
            tempSelectFiles.remove(currentFilePath)
//            selectPhotonFiles.remove(currentFilePath)
            //TODO 通知九宫格页面更新刷新
//            EventBusUtils.sendEvent(AlbumSelectFileChangeEvent(selectPhotonFiles))
        } else {
            if (albumViewModel.albumSelectConfig.maxSelectLimit == 1) {
                if (!CheckFileAvailable.checkFileAvailable(albumViewModel.getCurrentMediaItem())) return
//                selectPhotonFiles.clear()
                tempSelectFiles.clear()
//                albumViewModel.selectedMediaFiles.value?.clear()
            } else {
                if (albumViewModel.getSelectedMediaFiles().size >= albumViewModel.albumSelectConfig.maxSelectLimit) {
                    Toast.makeText(activity, "最多只能选${albumViewModel.albumSelectConfig.maxSelectLimit}张图片", Toast.LENGTH_SHORT).show()
                    return
                }
                if (!CheckFileAvailable.checkFileAvailable(albumViewModel.getCurrentMediaItem())) return
            }
//            albumViewModel.selectedMediaFiles.value?.add(albumViewModel.getSelectedMediaFiles().size,currentFilePath)
            tempSelectFiles.add(tempSelectFiles.size,currentFilePath)
//            selectPhotonFiles.add(selectPhotonFiles.size, currentFilePath)
//            currentFilePath?.let { tempSelectFiles.add(tempSelectFiles.size,it) }
        }
        albumViewModel.selectedMediaFiles.value = (tempSelectFiles)
        updateSelectFileState(currentFilePath ?: "")
    }

    private fun updateSelectFileState(currentFilePath: String) {
        if (albumViewModel.getSelectedMediaFiles().contains(currentFilePath)) {
            albumSelectInfoImage.visibility = View.INVISIBLE
            albumSelectInfo?.visibility = View.VISIBLE
            val index = albumViewModel.getSelectedMediaFiles().indexOf(currentFilePath) + 1
            albumSelectInfo?.text = "$index"
        } else {
            albumSelectInfoImage.visibility = View.VISIBLE
            albumSelectInfo?.visibility = View.INVISIBLE
            albumSelectInfo?.text = ""
        }
    }

    private fun updateVideoPlayState(enforcePause: Boolean = false) {
        for (i in 0 until mViewpager.childCount) {
            (mViewpager.getChildAt(i) as? CustomVideoView)?.let { videoView ->
                if ("$currentPlayVideoIndex" == videoView.tag as? String && !enforcePause) {  //播放当前视频
                    videoView.requestFocus()
                    videoView.seekTo(0)
                    videoView.start()
                } else {
                    videoView.pause()
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        updateVideoPlayState()
    }

    override fun onPause() {
        super.onPause()
        updateVideoPlayState(true)
    }

    override fun onImagesLoaded(imageFolders: MutableList<MediaFolder>) {
//        showContent()
//        currentMediaItemList.clear()
        if (imageFolders.size == 0) {
            activity?.supportFragmentManager?.popBackStackImmediate()
        } else {
//            currentMediaItemList.addAll(imageFolders[0].images)
            if (albumViewModel.currentImageFolder == null) {
                albumViewModel.currentImageFolder = imageFolders[0]
            }
            mPhotoViewAdapter = ImageOrVideoPreviewAdapter(requireActivity(), albumViewModel.getCurrentMediaItemList())
            mViewpager.adapter = mPhotoViewAdapter
            mViewpager.currentItem = albumViewModel.currentPosition
            if (albumViewModel.currentPosition == mViewpager.currentItem && albumViewModel.currentPosition == 0) {
                onPageSelected(0)
            }
        }
    }

    override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
    }

    override fun onPageSelected(position: Int) {
        currentPlayVideoIndex = position
        updateVideoPlayState()
        albumViewModel.currentPosition = position
        val index = position + 1
        albumFolderInfo?.text = "$index/${albumViewModel.getCurrentMediaItemList().size}"
        val currentFilePath = albumViewModel.getMediaItem(position)?.path ?: ""
        updateSelectFileState(currentFilePath)
    }

    override fun onPageScrollStateChanged(state: Int) {
    }

}

/**
 * 预览 adapter
 */
class ImageOrVideoPreviewAdapter(
    private val activity: Activity,
    private val mediaFiles: ArrayList<MediaItem>
) : PagerAdapter() {

    private val layoutInflater: LayoutInflater by lazy {
        LayoutInflater.from(activity)
    }

    private var firstLoadVideo = true

    override fun getCount() = mediaFiles.size

    override fun isViewFromObject(view: View, `object`: Any) = (view == `object`)

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        if (`object` is CustomVideoView) {
            (`object`).stopPlayback()
        }
        container.removeView(`object` as View)
    }

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val previewBean = mediaFiles[position]
        val itemView: View
        if (previewBean.isVideo()) {
            itemView = CustomVideoView(activity)
            itemView.setOnCompletionListener {
                itemView.seekTo(0)
                itemView.start()
            }
            itemView.setVideoPath(previewBean.path)
            itemView.tag = "$position"
//            itemView.setMediaController(mMediaController)
            if (firstLoadVideo) {
                itemView.requestFocus()
                itemView.seekTo(0)
                itemView.start()
            }
        } else {
            itemView = ImageView(activity)
            val layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            itemView.layoutParams = layoutParams
            Glide.with(itemView)
                .load(previewBean.path)
                .into(itemView)
        }
        firstLoadVideo = false
        container.addView(itemView)
        return itemView
    }

}