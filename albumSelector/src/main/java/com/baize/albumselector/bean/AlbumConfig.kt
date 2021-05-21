package com.baize.albumselector.bean

class SelectAlbumConfig(
    var maxSelectLimit: Int = 9,
    var autoFinishActivity: Boolean = false,
    var supportGif: Boolean = false,
    var supportVideo: Boolean = false,
    var isDefaultVideo: Boolean = false
) {

    class Builder {
        var maxSelectLimit: Int = 9
        var autoFinishActivity: Boolean = false
        var supportGif: Boolean = false
        var supportVideo: Boolean = false
        var isDefaultVideo: Boolean = false

        fun maxSelectLimit(limit: Int): Builder {
            maxSelectLimit = limit
            return this
        }

        fun autoFinishActivity(bool: Boolean):Builder {
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

        fun build(): SelectAlbumConfig =
            SelectAlbumConfig(maxSelectLimit, supportGif, supportVideo, isDefaultVideo)

    }

}