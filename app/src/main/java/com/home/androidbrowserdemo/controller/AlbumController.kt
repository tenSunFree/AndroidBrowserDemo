package com.home.androidbrowserdemo.controller

import android.graphics.Bitmap
import android.view.View

interface AlbumController {

    val albumView: View

    var albumTitle: String

    fun setAlbumCover(bitmap: Bitmap)

    fun activate()

    fun deactivate()
}
