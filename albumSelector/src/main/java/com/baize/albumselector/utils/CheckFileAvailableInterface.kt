package com.baize.albumselector.utils

import com.baize.albumselector.bean.MediaItem

interface CheckFileAvailableInterface {
    fun checkFileAvailable(selectMedia: MediaItem?): Boolean {
        return true
    }
}