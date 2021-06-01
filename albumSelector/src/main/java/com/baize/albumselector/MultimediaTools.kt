package com.baize.albumselector

import android.app.Activity
import android.content.Context
import android.os.Bundle
import com.baize.albumselector.bean.SelectAlbumConfig

object MultimediaTools {

    internal var selectCompleteListener : ((ArrayList<String>) -> Unit)? = null
    var REQUEST_CODE : Int = 0x0101

    fun getAlbum(
        context: Activity,
        maxLimit: Int,
        selectorPath: ArrayList<String>,
        autoFinishActivity: Boolean,
        supportGif: Boolean,
        supportVideo: Boolean,
        defaultVideo: Boolean,
        requestCode:Int = REQUEST_CODE
    ) {
        val bundle = Bundle()
        bundle.putInt(MAX_LIMIT, maxLimit)
        bundle.putStringArrayList(SELECTOR_PATH, selectorPath)
        bundle.putBoolean(AUTO_FINISH_ACTIVITY, autoFinishActivity)
        bundle.putBoolean(SUPPORT_GIF, supportGif)
        bundle.putBoolean(SUPPORT_VIDEO, supportVideo)
        bundle.putBoolean(DEFAULT_VIDEO, defaultVideo)
        AlbumSelectorActivity.open(requestCode,context, bundle)
    }

    fun getAlbum(
        context: Activity,
        albumConfig: SelectAlbumConfig,
        selectorPath: ArrayList<String>? = null,
        requestCode:Int = REQUEST_CODE
    ) {
        val bundle = Bundle()
        selectorPath?.let {
            bundle.putStringArrayList(SELECTOR_PATH, selectorPath)
        }
        bundle.putSerializable(ALBUM_CONFIG,albumConfig)
        AlbumSelectorActivity.open(requestCode,context, bundle)
    }

    fun setSelectCompleteListener(selectListener:((ArrayList<String>) -> Unit)?) {
        this.selectCompleteListener = selectListener
    }

    //注意及时移除监听，避免内存泄漏
    fun release() {
        this.selectCompleteListener = null
    }

    //常量
    const val SELECTOR_PATH = "selectorPath"
    internal const val ALBUM_CONFIG = "album_config"
    internal const val MAX_LIMIT = "maxLimit"
    internal const val AUTO_FINISH_ACTIVITY = "autoFinishActivity"
    internal const val SUPPORT_GIF = "supportGif"
    internal const val SUPPORT_VIDEO = "supportVideo"
    internal const val DEFAULT_VIDEO = "defaultVideo"
    internal const val SORT_ORDER = "SortOrder"
    internal const val IMAGE_FOLDER_PATH = "ImageFolderPath"
    internal const val CURRENT_POSITION = "current_position"

}