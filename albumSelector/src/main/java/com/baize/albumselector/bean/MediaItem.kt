package com.baize.albumselector.bean

import java.text.SimpleDateFormat
import java.util.*

class MediaItem {
    var name: String = ""       //图片/视频的名字
    var path: String = ""      //图片/视频的路径
    var height: Int = 0
    var width: Int = 0
    var imageModifyDate: Long = 0
    var imageAddDate: Long = 0
    var duration: Int = 0  //视频时长

    fun getDurationDesc() : String {
        if (duration == 0) return ""
        val minutes = duration / 1000 / 60
        val seconds = duration / 1000 % 60
        val preTxt = if (minutes > 0) {
            "${minutes}分"
        } else ""
        return "${preTxt}${seconds}秒"
    }

    fun getModifyDateText(): String {
        return formatTime(Date(imageModifyDate * 1000), "MM-dd HH:mm:ss")
    }

    fun getAddDateText(): String {
        return formatTime(Date(imageAddDate * 1000), "MM-dd HH:mm:ss")
    }

    fun formatTime(date: Date, format: String = "yyyy-MM-dd HH:mm"): String {
        val simpleDateFormat = SimpleDateFormat(format, Locale.getDefault())
        return simpleDateFormat.format(date.time).toString()
    }

    fun isVideo() : Boolean {
        return path.endsWith(".mp4")
    }

    fun dimensionRatio(): Double {
        return if (height == 0 || width == 0) {
            0.0
        } else {
            width / (height * 1.0)
        }
    }
}