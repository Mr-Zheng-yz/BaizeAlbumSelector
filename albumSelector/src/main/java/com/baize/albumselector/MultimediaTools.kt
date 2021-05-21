package com.baize.albumselector

import android.content.Context
import android.os.Bundle

class MultimediaTools {

    companion object {

        fun getAlbum(
            context: Context,
            maxLimit: Int,
            selectorPath: ArrayList<String>,
            autoFinishActivity: Boolean,
            supportGif: Boolean,
            supportVideo: Boolean,
            defaultVideo: Boolean
        ) {
            val bundle = Bundle()
            bundle.putInt(MAX_LIMIT,maxLimit)
            bundle.putStringArrayList(SELECTOR_PATH, selectorPath)
            bundle.putBoolean(AUTO_FINISH_ACTIVITY,autoFinishActivity)
            bundle.putBoolean(SUPPORT_GIF,supportGif)
            bundle.putBoolean(SUPPORT_VIDEO,supportVideo)
            bundle.putBoolean(DEFAULT_VIDEO,defaultVideo)
            AlbumSelectorActivity.open(context,bundle)
        }

        //常量
        internal const val MAX_LIMIT = "maxLimit"
        internal const val SELECTOR_PATH = "selectorPath"
        internal const val AUTO_FINISH_ACTIVITY = "autoFinishActivity"
        internal const val SUPPORT_GIF = "supportGif"
        internal const val SUPPORT_VIDEO = "supportVideo"
        internal const val DEFAULT_VIDEO = "defaultVideo"
        internal const val SORT_ORDER = "SortOrder"
        internal const val IMAGE_FOLDER_PATH = "ImageFolderPath"
        internal const val CURRENT_POSITION = "current_position"
    }


}