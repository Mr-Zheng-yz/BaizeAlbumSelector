package com.baize.albumselector.view

import android.content.Context
import android.util.AttributeSet
import android.widget.VideoView

class CustomVideoView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : VideoView(context, attrs, defStyleAttr) {

    private var mVideoWidth = 0
    private var mVideoHeight = 0

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        mVideoWidth = w
        mVideoHeight = h
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
//        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        var width = getDefaultSize(mVideoWidth, widthMeasureSpec)
        var height = getDefaultSize(mVideoHeight, heightMeasureSpec)
        if (mVideoWidth > 0 && mVideoHeight > 0) {
            when {
                mVideoWidth * height > width * mVideoHeight -> {
                    height = width * mVideoHeight / mVideoWidth;
                }
                mVideoWidth * height < width * mVideoHeight -> {
                    width = height * mVideoWidth / mVideoHeight;
                }
            }
        }
        setMeasuredDimension (width, height)
    }

}