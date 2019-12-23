package com.home.androidbrowserdemo.controller.listener

import android.app.Activity
import android.content.Context
import android.webkit.DownloadListener

import com.home.androidbrowserdemo.controller.unit.BrowserUnit
import com.home.androidbrowserdemo.controller.unit.IntentUnit

class NinjaDownloadListener(private val context: Context) : DownloadListener {

    override fun onDownloadStart(
        url: String, userAgent: String,
        contentDisposition: String, mimeType: String, contentLength: Long
    ) {
        val holder = IntentUnit.getContext()
        if (holder !is Activity) {
            BrowserUnit.download(context, url, contentDisposition, mimeType)
            return
        }
    }
}
