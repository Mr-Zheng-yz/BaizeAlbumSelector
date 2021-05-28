package com.baize.albumselector

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.lifecycle.ViewModelProvider
import com.baize.albumselector.bean.MediaFolder
import com.baize.albumselector.bean.SelectAlbumConfig

class AlbumSelectorActivity : AppCompatActivity(), OnLocalImageFileClickListener {

    companion object {
        fun open(context: Context, bundle: Bundle) {
            val intent = Intent(context, AlbumSelectorActivity::class.java)
            intent.putExtras(bundle)
            context.startActivity(intent)
        }
    }

    private var albumFileGridFragment: AlbumFileGridFragment? = null
    lateinit var albumViewModel: AlbumViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_album_selector)
        albumViewModel = ViewModelProvider(
            this,
            ViewModelProvider.NewInstanceFactory()
        ).get(AlbumViewModel::class.java)

        albumViewModel.albumSelectConfig = SelectAlbumConfig.Builder()
            .maxSelectLimit(intent.extras?.getInt(MultimediaTools.MAX_LIMIT) ?: 9)
            .maxSelectLimit(intent.extras?.getInt(MultimediaTools.MAX_LIMIT) ?: 9)
            .supportGif(intent.extras?.getBoolean(MultimediaTools.SUPPORT_GIF) ?: false)
            .supportVideo(intent.extras?.getBoolean(MultimediaTools.SUPPORT_VIDEO) ?: false)
            .defaultVideo(intent.extras?.getBoolean(MultimediaTools.DEFAULT_VIDEO) ?: false)
            .build()

        albumFileGridFragment = AlbumFileGridFragment.getInstance(intent.extras)
        supportFragmentManager.beginTransaction()
            .add(R.id.fragment_file_page, albumFileGridFragment!!)
            .commitAllowingStateLoss()
    }

    override fun onBackPressed() {
        val activityBackStackEntryCount = supportFragmentManager.backStackEntryCount
        if (activityBackStackEntryCount > 0) {
            supportFragmentManager.popBackStack()
            return
        }
        val backStackEntryCount =
            albumFileGridFragment?.childFragmentManager?.backStackEntryCount ?: 0
        if (backStackEntryCount > 0) {
            albumFileGridFragment?.childFragmentManager?.popBackStack()
            return
        }
        super.onBackPressed()
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    override fun onLocalImageFileClick(
        position: Int,
        limitNumber: Int,
        autoFinishActivity: Boolean,
        currentImageFolder: MediaFolder,
        supportGif: Boolean,
        supportVideo: Boolean,
        currentSortOrder: String,
        isDefaultVideo: Boolean
    ) {
        val bundle = Bundle()
        bundle.putSerializable(MultimediaTools.CURRENT_POSITION, position)
        bundle.putString(MultimediaTools.IMAGE_FOLDER_PATH, currentImageFolder.path)
        bundle.putString(MultimediaTools.SORT_ORDER, currentSortOrder)
        bundle.putInt(MultimediaTools.MAX_LIMIT, limitNumber)
        bundle.putBoolean(MultimediaTools.AUTO_FINISH_ACTIVITY, autoFinishActivity)
        bundle.putBoolean(MultimediaTools.SUPPORT_GIF, supportGif)
        bundle.putBoolean(MultimediaTools.SUPPORT_VIDEO, supportVideo)
        bundle.putBoolean(MultimediaTools.DEFAULT_VIDEO, isDefaultVideo)
        val albumFilePageFragment = AlbumFilePreviewFragment.getInstance()
        albumFilePageFragment.arguments = bundle
        supportFragmentManager.beginTransaction()
            .add(R.id.fragment_file_page, albumFilePageFragment)
            .addToBackStack(null).commitAllowingStateLoss()
    }
}