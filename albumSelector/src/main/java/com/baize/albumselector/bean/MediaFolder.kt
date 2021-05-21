package com.baize.albumselector.bean

class MediaFolder {
    var name: String? = null  //当前文件夹的名字
    var path: String? = null  //当前文件夹的路径
    var cover: MediaItem? = null   //当前文件夹需要要显示的缩略图，默认为最近的一次图片
    var images = arrayListOf<MediaItem>()//当前文件夹下所有图片的集合

    fun showImagesCount(): String {
        return "(${images.size})"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as MediaFolder

        if (name != other.name) return false
        if (path != other.path) return false
        return true
    }

    override fun hashCode(): Int {
        var result = name?.hashCode() ?: 0
        result = 31 * result + (path?.hashCode() ?: 0)
        return result
    }


}