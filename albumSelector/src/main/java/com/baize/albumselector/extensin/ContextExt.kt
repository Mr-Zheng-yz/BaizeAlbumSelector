package com.baize.albumselector.extensin

import android.app.Activity
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

fun Activity.checkAndRequestPermission(permission: String, requestCode: Int = 111, success: () -> Unit) {
    if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
        ActivityCompat.requestPermissions(this, arrayOf(permission), requestCode)
    } else {
        success.invoke()
    }
}