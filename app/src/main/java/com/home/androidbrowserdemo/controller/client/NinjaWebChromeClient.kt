package com.home.androidbrowserdemo.controller.client

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.view.View
import android.webkit.GeolocationPermissions
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
import android.webkit.WebView
import com.home.androidbrowserdemo.controller.unit.HelperUnit
import com.home.androidbrowserdemo.view.components.NinjaWebView

class NinjaWebChromeClient(private val ninjaWebView: NinjaWebView) : WebChromeClient() {

    override fun onCreateWindow(
        view: WebView, dialog: Boolean,
        userGesture: Boolean, resultMsg: android.os.Message
    ): Boolean {
        val result = view.hitTestResult
        val data = result.extra
        val context = view.context
        val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(data))
        context.startActivity(browserIntent)
        return false
    }

    override fun onProgressChanged(view: WebView, progress: Int) {
        super.onProgressChanged(view, progress)
        ninjaWebView.update(progress)
    }

    override fun onReceivedTitle(view: WebView, title: String) {
        super.onReceivedTitle(view, title)
        ninjaWebView.update(title)
    }

    override fun onShowCustomView(view: View, callback: CustomViewCallback) {
        ninjaWebView.browserController!!.onShowCustomView(view, callback)
        super.onShowCustomView(view, callback)
    }

    override fun onHideCustomView() {
        ninjaWebView.browserController!!.onHideCustomView()
        super.onHideCustomView()
    }

    override fun onShowFileChooser(
        webView: WebView, filePathCallback: ValueCallback<Array<Uri>>,
        fileChooserParams: FileChooserParams
    ): Boolean {
        ninjaWebView.browserController!!.showFileChooser(filePathCallback)
        return true
    }

    override fun onGeolocationPermissionsShowPrompt(
        origin: String, callback: GeolocationPermissions.Callback
    ) {
        val activity = ninjaWebView.context as Activity
        HelperUnit.grantPermissionsLoc(activity)
        callback.invoke(origin, true, false)
        super.onGeolocationPermissionsShowPrompt(origin, callback)
    }
}
