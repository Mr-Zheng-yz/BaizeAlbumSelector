package com.baize.baizealbumselector

import android.Manifest
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.baize.albumselector.AlbumSelectorActivity
import com.baize.albumselector.MultimediaTools
import com.baize.albumselector.extensin.checkAndRequestPermission

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    fun openAlbum(v: View) {
        checkAndRequestPermission(Manifest.permission.READ_EXTERNAL_STORAGE) {
            MultimediaTools.getAlbum(this, 2, arrayListOf(),
                autoFinishActivity = true,
                supportGif = true,
                supportVideo = true,
                defaultVideo = false
            )
        }
    }
}