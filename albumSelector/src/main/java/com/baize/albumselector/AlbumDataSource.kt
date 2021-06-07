package com.baize.albumselector

import android.annotation.SuppressLint
import android.content.Context
import android.os.AsyncTask
import android.os.Environment
import android.os.SystemClock
import android.provider.MediaStore
import android.text.TextUtils
import android.util.Log
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import com.baize.albumselector.bean.MediaFolder
import com.baize.albumselector.bean.MediaItem
import java.io.File

class AlbumDataSource(
    var context: Context,
    private val loadedListener: OnImagesLoadedListener,
    private val lifecycle: Lifecycle
) : LifecycleObserver {

    init {
        lifecycle.addObserver(this)
    }

    private val IMAGE_PROJECTION = arrayOf(
        MediaStore.Images.Media.DISPLAY_NAME,
        MediaStore.Images.Media.DATA,
        MediaStore.Images.Media.DATE_MODIFIED,
        MediaStore.Images.Media.WIDTH,
        MediaStore.Images.Media.HEIGHT,
        MediaStore.Images.Media.MIME_TYPE,
        MediaStore.Images.Media.DATE_ADDED
    )

    private val VIDEO_PROJECTION = arrayOf(
        MediaStore.Video.Media.DISPLAY_NAME,
        MediaStore.Video.Thumbnails._ID,
        MediaStore.Video.Thumbnails.DATA,
        MediaStore.Video.Media.DURATION,
        MediaStore.Video.Media.SIZE,
        MediaStore.Images.Media.WIDTH,
        MediaStore.Images.Media.HEIGHT,
        MediaStore.Images.Media.MIME_TYPE,
        MediaStore.Video.Media.DATE_MODIFIED,
        MediaStore.Images.Media.DATE_ADDED
    )

    private var where = MediaStore.Images.Media.MIME_TYPE + " = ? or " +
            MediaStore.Images.Media.MIME_TYPE + " = ? or " +
            MediaStore.Images.Media.MIME_TYPE + " = ? "

    private var whereArgs = arrayOf("image/jpeg", "image/png", "image/jpg")

    private val mediaFolders = ArrayList<MediaFolder>()   //所有的图片文件夹
//    private var queryMediaJob: Job? = null

    private val asyncTask = @SuppressLint("StaticFieldLeak")
    object : AsyncTask<String, Int, ArrayList<MediaItem>>() {
        override fun doInBackground(vararg params: String?): ArrayList<MediaItem> {
            val allImage = queryImage(params[0], params[1], params[2] == "true")
            if (params[3] == "true") {
                queryVideo(params[0], params[1], params[4] == "true", allImage)
            }
            Log.i("yanze", "query Done!")
            return allImage
        }

        override fun onPostExecute(result: ArrayList<MediaItem>?) {
            loadedListener.onImagesLoaded(mediaFolders)
        }

    }

    fun queryWithAsyncTask(
        folderPath: String?,
        sortOrder: String,
        supportGif: Boolean = true,
        supportVideo: Boolean = true,
        defaultVideo: Boolean = false
    ) {
        // 0:path 1:sortOrder 2:supportGif 3:supportVideo 4:defaultVideo
        asyncTask.execute(folderPath,sortOrder,supportGif.toString(),supportVideo.toString(),defaultVideo.toString())
    }

    //使用携程查询
//    fun query(
//        folderPath: String?,
//        sortOrder: String,
//        supportGif: Boolean = true,
//        supportVideo: Boolean = true,
//        defaultVideo: Boolean = false
//    ) {
//        mediaFolders.clear()
//        queryMediaJob = GlobalScope.launch {
//            withContext(Dispatchers.IO) {
//                val allImage = queryImage(folderPath, sortOrder, supportGif)
//                if (supportVideo) {
//                    queryVideo(folderPath, sortOrder, defaultVideo, allImage)
//                }
//            }
//            withContext(Dispatchers.Main) {
//                Log.i("yanze", "query Done!")
//                loadedListener.onImagesLoaded(mediaFolders)
//            }
//        }
//    }

    private fun queryImage(
        folderPath: String?,
        sortOrder: String?,
        supportGif: Boolean = false
    ): ArrayList<MediaItem> {
        if (supportGif) {
            where += " or " + MediaStore.Images.Media.MIME_TYPE + "= ? "
            whereArgs = arrayOf("image/jpeg", "image/png", "image/jpg", "image/gif")
        }
        val resolverCursor = when (true) {
            TextUtils.isEmpty(folderPath) || "/" == folderPath -> {
                context.contentResolver.query(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    IMAGE_PROJECTION,
                    where,
                    whereArgs,
                    sortOrder ?: MediaStore.Images.Media.DATE_ADDED + " DESC"
                )
            }
            else -> {
                context.contentResolver.query(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    IMAGE_PROJECTION,
                    MediaStore.Images.Media.DATA + " like '%" + folderPath + "%' and " + where,
                    whereArgs,
                    sortOrder
                )
            }
        }
        val allImages = ArrayList<MediaItem>()
        resolverCursor?.use { cursor ->
            while (cursor.moveToNext()) {
                val imageName =
                    cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME))
                val imagePath =
                    cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA))
                //单位秒
                val imageModifyDate =
                    cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_MODIFIED))
                //单位秒
                val imageAddDate =
                    cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_ADDED))
                val imageWidth =
                    cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.WIDTH))
                val imageHeight =
                    cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.HEIGHT))
                val imageMimeType =
                    cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.MIME_TYPE))

                if (TextUtils.equals(imageMimeType, "image/gif")) {
                    if (!supportGif) {
                        continue
                    }
                }

                if (imageWidth == 0 || imageHeight == 0) {
                    continue
                }

                //封装实体
                val imageItem = MediaItem()
                imageItem.name = imageName ?: ""
                imageItem.path = imagePath ?: ""
                imageItem.width = imageWidth
                imageItem.height = imageHeight
                imageItem.imageModifyDate = imageModifyDate
                imageItem.imageAddDate = imageAddDate
                allImages.add(imageItem)

                //根据父路径分类存放图片
                val imageFolder = generateImageFolder(imagePath)
                if (!mediaFolders.contains(imageFolder)) {
                    val images = ArrayList<MediaItem>()
                    images.add(imageItem)
                    imageFolder.cover = imageItem
                    imageFolder.images = images
                    mediaFolders.add(imageFolder)
                } else {
                    mediaFolders[mediaFolders.indexOf(imageFolder)].images.add(imageItem)
                }
            }
            //确保第一条是所有图片
            if (cursor.count > 0 && allImages.size > 0) {
                //构造所有图片的集合
                val allImagesFolder = MediaFolder()
                allImagesFolder.name = "全部图片"
                allImagesFolder.path = "/"
                allImagesFolder.cover = allImages[0]
                allImagesFolder.images = allImages
                mediaFolders.add(0, allImagesFolder)
            }
            cursor.close()
        }
        return allImages
    }

    private fun queryVideo(
        folderPath: String?,
        sortOrder: String?,
        defaultVideo: Boolean,
        allImages: ArrayList<MediaItem>
    ) {
        val videoWhere = MediaStore.Video.Media.MIME_TYPE + "=?"
        val resourceVideoCursor = when (true) {
            (TextUtils.isEmpty(folderPath) || "v/" == folderPath) -> {
                context.contentResolver.query(
                    MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                    VIDEO_PROJECTION,
                    videoWhere,
                    arrayOf("video/mp4"),
                    sortOrder ?: sortOrder ?: MediaStore.Video.Media.DATE_ADDED + " DESC"
                )
            }
            else -> {
                context.contentResolver.query(
                    MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                    VIDEO_PROJECTION,
                    MediaStore.Video.Media.DATA + " like '%" + folderPath + "%' and " + videoWhere,
                    arrayOf("video/mp4"),
                    sortOrder
                )
            }
        }
        val allVideos = ArrayList<MediaItem>()
        resourceVideoCursor?.use { cursor ->
            while (resourceVideoCursor.moveToNext()) {
                val path = cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.DATA))
                val duration =
                    cursor.getInt(cursor.getColumnIndex(MediaStore.Video.Media.DURATION))
                val displayName =
                    cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.DISPLAY_NAME))
                val modifyTime =
                    cursor.getLong(cursor.getColumnIndex(MediaStore.Video.Media.DATE_MODIFIED))
                val addTime =
                    cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATE_ADDED))
                val imageWidth =
                    cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.WIDTH))
                val imageHeight =
                    cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.HEIGHT))
                //包装成对象
                val imageItem = MediaItem()
                imageItem.name = displayName ?: ""
                imageItem.path = path
                imageItem.imageModifyDate = modifyTime
                imageItem.imageAddDate = addTime
                imageItem.width = imageWidth
                imageItem.height = imageHeight
                imageItem.duration = duration
                allVideos.add(imageItem)
                //按排序规则将视频插入到正确位置
                val targetIndex =
                    if (TextUtils.equals(sortOrder, MediaStore.Images.Media.DATE_ADDED + " DESC")) {
                        allImages.indexOfFirst { it.imageAddDate < imageItem.imageAddDate }
                    } else {
                        allImages.indexOfFirst { it.imageModifyDate < imageItem.imageModifyDate }
                    }
                if (targetIndex in 0..allImages.size) {
                    allImages.add(targetIndex, imageItem)
                }
                //根据父文件夹分类
                val imageFolder = generateImageFolder(path)
                if (!mediaFolders.contains(imageFolder)) {
                    val images = ArrayList<MediaItem>()
                    images.add(imageItem)
                    imageFolder.cover = imageItem
                    imageFolder.images = images
                    mediaFolders.add(imageFolder)
                } else {
                    mediaFolders[mediaFolders.indexOf(imageFolder)].images.add(imageItem)
                }
            }
            //全部视频
            if (cursor.count > 0 && allVideos.size > 0) {
                val allVideosFolder = MediaFolder()
                allVideosFolder.name = "全部视频"
                allVideosFolder.path = "v/"
                allVideosFolder.cover = allVideos[0]
                allVideosFolder.images = allVideos
                if (!mediaFolders.contains(allVideosFolder)) {
                    if (mediaFolders.isEmpty()) {
                        mediaFolders.add(allVideosFolder)
                    } else {
                        if (defaultVideo) {
                            mediaFolders.add(0, allVideosFolder)
                        } else {
                            mediaFolders.add(1, allVideosFolder)
                        }
                    }
                }
            }
            resourceVideoCursor.close()
        }
    }

    //生成图片对应文件夹
    private fun generateImageFolder(path: String): MediaFolder {
        val imageFolder = MediaFolder()
        val imageFile = File(path)
        val imageParentFile = imageFile.parentFile
        if (imageParentFile != null) {
            imageFolder.name = imageParentFile.name
            imageFolder.path = imageParentFile.absolutePath
        } else {
            imageFolder.name = "other_${SystemClock.currentThreadTimeMillis()}"
            imageFolder.path =
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).absolutePath
//            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
//                imageFolder.path = BaseApplication.getInstance()
//                    .getExternalFilesDir(MediaStore.EXTRA_MEDIA_ALBUM).absolutePath
//            } else {
//                imageFolder.path =
//                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).absolutePath
//            }
        }
        return imageFolder
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    fun onDestroy() {
        asyncTask.cancel(true)
//        queryMediaJob?.cancel()
        lifecycle.removeObserver(this)
    }
}


/** 所有图片加载完成的回调接口  */
interface OnImagesLoadedListener {
    fun onImagesLoaded(imageFolders: MutableList<MediaFolder>)
}