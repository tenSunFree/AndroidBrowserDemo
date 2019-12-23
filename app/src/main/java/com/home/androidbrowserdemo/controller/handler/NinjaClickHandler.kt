package com.home.androidbrowserdemo.controller.handler

import android.os.Handler
import android.os.Message
import com.home.androidbrowserdemo.view.components.NinjaWebView

class NinjaClickHandler(private val webView: NinjaWebView) : Handler() {

    override fun handleMessage(message: Message) {
        super.handleMessage(message)
        webView.browserController!!.onLongPress(message.data.getString("url"))
    }
}
