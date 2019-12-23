package com.home.androidbrowserdemo.controller.listener

import android.view.GestureDetector
import android.view.MotionEvent
import com.home.androidbrowserdemo.view.components.NinjaWebView

class NinjaGestureListener(private val webView: NinjaWebView) :
    GestureDetector.SimpleOnGestureListener() {

    private var longPress = true

    override fun onLongPress(e: MotionEvent) {
        if (longPress) {
            webView.onLongPress()
        }
    }

    override fun onDoubleTapEvent(e: MotionEvent): Boolean {
        longPress = false
        return false
    }

    override fun onShowPress(e: MotionEvent) {
        longPress = true
    }
}
