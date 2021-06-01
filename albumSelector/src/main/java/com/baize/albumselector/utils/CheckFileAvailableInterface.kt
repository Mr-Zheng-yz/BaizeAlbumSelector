package com.baize.albumselector.utils

import com.baize.albumselector.bean.MediaItem
import java.io.Serializable

interface CheckFileAvailableInterface : Serializable {
    fun checkFileAvailable(selectMedia: MediaItem?): Boolean {
        return true
    }
}