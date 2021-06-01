package com.baize.albumselector.bean

import com.baize.albumselector.utils.CheckFileAvailableInterface
import java.io.Serializable

data class SelectAlbumConfig(
    var maxSelectLimit: Int = 9,
    var autoFinishActivity: Boolean = false,
    var supportGif: Boolean = false,
    var supportVideo: Boolean = false,
    var isDefaultVideo: Boolean = false
) : Serializable {

    private var checkFileAvailableList: MutableList<CheckFileAvailableInterface>? = null

    fun checkFileAvailable(mediaItem: MediaItem?): Boolean {
        checkFileAvailableList?.forEach { checker ->
            if (!checker.checkFileAvailable(mediaItem)) {
                return@checkFileAvailable false
            }
        }
        return true
    }

    class Builder {
        internal var maxSelectLimit: Int = 9
        internal var autoFinishActivity: Boolean = false
        internal var supportGif: Boolean = false
        internal var supportVideo: Boolean = false
        internal var isDefaultVideo: Boolean = false
        internal var checkFileAvailableList: MutableList<CheckFileAvailableInterface> = mutableListOf()

        fun maxSelectLimit(limit: Int): Builder {
            maxSelectLimit = limit
            return this
        }

        fun autoFinishActivity(bool: Boolean): Builder {
            autoFinishActivity = bool
            return this
        }

        fun supportGif(bool: Boolean): Builder {
            supportGif = bool
            return this
        }

        fun supportVideo(bool: Boolean): Builder {
            supportVideo = bool
            return this
        }

        fun defaultVideo(bool: Boolean): Builder {
            isDefaultVideo = bool
            return this
        }

        fun addFileAvailableRule(fileAvailableRule: CheckFileAvailableInterface): Builder {
            checkFileAvailableList.add(fileAvailableRule)
            return this
        }

        fun build(): SelectAlbumConfig =
            SelectAlbumConfig(maxSelectLimit,autoFinishActivity, supportGif, supportVideo, isDefaultVideo)
    }

}