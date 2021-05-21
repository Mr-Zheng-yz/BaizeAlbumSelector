package com.baize.albumselector.extensin

import androidx.fragment.app.Fragment

fun Fragment.getArgumentsString(key: String, defaultValue: String = ""): String {
    return arguments?.getString(key, defaultValue) ?: defaultValue
}

fun Fragment.getArgumentsInt(key: String, defaultValue: Int = 0): Int {
    return arguments?.getInt(key, defaultValue) ?: defaultValue
}

fun Fragment.getArgumentsBoolean(key: String, defaultValue: Boolean = false): Boolean {
    return arguments?.getBoolean(key, defaultValue) ?: defaultValue
}

fun Fragment.getArgumentsArrayStringList(key: String): ArrayList<String> {
    return arguments?.getStringArrayList(key) ?: arrayListOf()
}