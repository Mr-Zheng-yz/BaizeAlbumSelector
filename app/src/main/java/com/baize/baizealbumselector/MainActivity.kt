package com.baize.baizealbumselector

import android.Manifest
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.Switch
import android.widget.TextView
import androidx.appcompat.widget.SwitchCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.baize.albumselector.AlbumSelectorActivity
import com.baize.albumselector.MultimediaTools
import com.baize.albumselector.bean.MediaItem
import com.baize.albumselector.bean.SelectAlbumConfig
import com.baize.albumselector.extensin.checkAndRequestPermission
import com.baize.albumselector.utils.CheckFileAvailableInterface

class MainActivity : AppCompatActivity() {

    lateinit var etMaxLimit: EditText
    lateinit var isAutoFinish: SwitchCompat
    lateinit var isSupportGif: SwitchCompat
    lateinit var isSupportVideo: SwitchCompat
    lateinit var isDefaultVideo: SwitchCompat
    lateinit var recyclerView: RecyclerView
    private val imageAdapter by lazy { ImageAdapter() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        etMaxLimit = findViewById(R.id.et_max_limit)
        isAutoFinish = findViewById(R.id.is_autoFinish)
        isSupportGif = findViewById(R.id.is_supportGif)
        isSupportVideo = findViewById(R.id.is_supportVideo)
        isDefaultVideo = findViewById(R.id.is_defaultVideo)
        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.apply {
            layoutManager = GridLayoutManager(this@MainActivity, 3)
            adapter = imageAdapter
        }
    }

    fun openAlbum(v: View) {
        checkAndRequestPermission(Manifest.permission.READ_EXTERNAL_STORAGE) {
            //方式一：
//            MultimediaTools.getAlbum(this, etMaxLimit.text.toString().toIntOrNull() ?: 9
//                , selectedMedia,
//                autoFinishActivity = isAutoFinish.isChecked,
//                supportGif = isSupportGif.isChecked,
//                supportVideo = isSupportVideo.isChecked,
//                defaultVideo = isDefaultVideo.isChecked
//            )
            //方式二：
            val config = SelectAlbumConfig.Builder()
                .maxSelectLimit(etMaxLimit.text.toString().toIntOrNull() ?: 9)
                .autoFinishActivity(isAutoFinish.isChecked)
                .supportGif(isSupportGif.isChecked)
                .supportVideo(isSupportVideo.isChecked)
                .defaultVideo(isDefaultVideo.isChecked)
                .addFileAvailableRule(object : CheckFileAvailableInterface {
                    override fun checkFileAvailable(selectMedia: MediaItem?): Boolean {
                        //视频时长不能大于10秒
                        return selectMedia?.duration ?: 0 <= 10 * 1000
                    }
                }).build()
            MultimediaTools.getAlbum(
                context = this,
                albumConfig = config,
                selectorPath = imageAdapter.datas
            )
            //如果相册Activity不关闭，使用回调通知
            MultimediaTools.setSelectCompleteListener {
                updateImage(it)
                SecondActivity.open(this@MainActivity, it)
            }
        }
    }

    private fun updateImage(images:ArrayList<String>?){
        images?.let {
            imageAdapter.datas.clear()
            imageAdapter.datas.addAll(images)
            imageAdapter.notifyDataSetChanged()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        data?.let {
            if (requestCode == MultimediaTools.REQUEST_CODE && resultCode == RESULT_OK) {
                updateImage(data.getStringArrayListExtra(MultimediaTools.SELECTOR_PATH))
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        MultimediaTools.release()
    }

}