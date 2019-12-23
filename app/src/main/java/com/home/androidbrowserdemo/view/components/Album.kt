package com.home.androidbrowserdemo.view.components

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.home.androidbrowserdemo.R
import com.home.androidbrowserdemo.controller.AlbumController
import com.home.androidbrowserdemo.controller.BrowserController

class Album(
    private val context: Context, private val albumController: AlbumController,
    private var browserController: BrowserController?
) {

    var albumView: View? = null
        private set
    private var albumCover: ImageView? = null
    private var albumTitle: TextView? = null

    fun setAlbumCover(bitmap: Bitmap?) {
        albumCover!!.setImageBitmap(bitmap)
    }

    fun getAlbumTitle(): String {
        return albumTitle!!.text.toString()
    }

    fun setAlbumTitle(title: String?) {
        albumTitle!!.text = title
    }

    fun setBrowserController(browserController: BrowserController?) {
        this.browserController = browserController
    }

    init {
        initUI()
    }

    @SuppressLint("InflateParams")
    private fun initUI() {
        albumView = LayoutInflater.from(context).inflate(R.layout.album, null, false)
        albumView!!.setOnClickListener {
            browserController!!.showAlbum(albumController)
            browserController!!.hideOverview()
            browserController!!.showEchelonFragment(false)
        }
        albumView!!.setOnLongClickListener {
            browserController!!.removeAlbum(albumController)
            true
        }
        val albumClose = albumView!!.findViewById<ImageView>(R.id.album_close)
        albumCover = albumView!!.findViewById(R.id.album_cover)
        albumTitle = albumView!!.findViewById(R.id.album_title)
        albumTitle!!.text = context.getString(R.string.app_name)
        albumClose.setOnClickListener { browserController!!.removeAlbum(albumController) }
    }

    fun activate() {
        albumView!!.setBackgroundResource(R.drawable.album_shape_accent)
    }

    fun deactivate() {
        albumView!!.setBackgroundResource(R.drawable.album_shape_transparent)
    }
}
