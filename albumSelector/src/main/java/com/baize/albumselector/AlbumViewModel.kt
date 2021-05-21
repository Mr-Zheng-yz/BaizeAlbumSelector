package com.baize.albumselector

import android.provider.MediaStore
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.baize.albumselector.bean.MediaFolder
import com.baize.albumselector.bean.MediaItem
import com.baize.albumselector.bean.SelectAlbumConfig

class AlbumViewModel : ViewModel() {
    var allMediaFolders = mutableListOf<MediaFolder>()
    var currentPosition: Int = 0
    var currentImageFolder: MediaFolder? = null
    var currentSortOrder: String = MediaStore.Images.Media.DATE_ADDED + " DESC"
    val selectedMediaFiles: MutableLiveData<ArrayList<String>> by lazy {
        MutableLiveData()
    }

    //限制配置
    var albumSelectConfig: SelectAlbumConfig = SelectAlbumConfig.Builder().build()

    //已选择文件数
    var selectFileSize = 0
        get() {
            return selectedMediaFiles.value?.size ?: 0
        }

    init {
        selectedMediaFiles.value = arrayListOf()
    }

    fun setSelectedMediaFiles(selectedFiles: ArrayList<String>) {
        selectedMediaFiles.value = selectedFiles
    }

    fun getSelectedMediaFiles(): ArrayList<String> {
        if (selectedMediaFiles.value == null) {
            selectedMediaFiles.value = arrayListOf()
        }
        return selectedMediaFiles.value!!
    }

    fun getCurrentMediaItemList(): ArrayList<MediaItem> {
        return currentImageFolder?.images ?: arrayListOf()
    }

    fun getMediaItem(position: Int): MediaItem? {
        return if (position in 0 until (currentImageFolder?.images?.size ?: 0)) {
            return currentImageFolder?.images?.get(position)
        } else null
    }

    fun getCurrentMediaItem(): MediaItem? {
        return getMediaItem(currentPosition)
    }

}