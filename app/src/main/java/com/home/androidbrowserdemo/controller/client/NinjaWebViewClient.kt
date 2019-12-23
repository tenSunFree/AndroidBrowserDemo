package com.home.androidbrowserdemo.controller.client

import android.annotation.TargetApi
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.net.http.SslError
import android.os.Build
import android.os.Message
import android.view.View
import android.webkit.*
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.preference.PreferenceManager
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.textfield.TextInputLayout
import com.home.androidbrowserdemo.R
import com.home.androidbrowserdemo.controller.unit.HelperUnit
import com.home.androidbrowserdemo.controller.unit.IntentUnit
import com.home.androidbrowserdemo.model.Record
import com.home.androidbrowserdemo.model.RecordAction
import com.home.androidbrowserdemo.utils.NinjaToast
import com.home.androidbrowserdemo.view.components.NinjaWebView
import java.net.URISyntaxException

class NinjaWebViewClient(private val ninjaWebView: NinjaWebView) : WebViewClient() {

    private val context: Context = ninjaWebView.context
    private val sp: SharedPreferences

    private val white: Boolean
    private var enable: Boolean = false
    fun enableAdBlock(enable: Boolean) {
        this.enable = enable
    }

    init {
        this.sp = PreferenceManager.getDefaultSharedPreferences(context)
        this.white = false
        this.enable = true
    }

    override fun onPageFinished(view: WebView, url: String) {
        super.onPageFinished(view, url)
        if (sp.getBoolean("saveHistory", true)) {
            val action = RecordAction(context)
            action.open(true)
            if (action.checkHistory(url)) {
                action.deleteHistoryItemByURL(url)
                action.addHistory(Record(ninjaWebView.title, url, System.currentTimeMillis()))
            } else {
                action.addHistory(Record(ninjaWebView.title, url, System.currentTimeMillis()))
            }
            action.close()
        }
        if (ninjaWebView.isForeground) {
            ninjaWebView.invalidate()
        } else {
            ninjaWebView.postInvalidate()
        }
    }

    override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
        val uri = Uri.parse(url)
        return handleUri(view, uri)
    }

    @TargetApi(Build.VERSION_CODES.N)
    override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
        val uri = request.url
        return handleUri(view, uri)
    }

    private fun handleUri(webView: WebView, uri: Uri): Boolean {
        val url = uri.toString()
        val parsedUri = Uri.parse(url)
        val packageManager = context.packageManager
        val browseIntent = Intent(Intent.ACTION_VIEW).setData(parsedUri)
        if (url.startsWith("http")) {
            webView.loadUrl(url, ninjaWebView.requestHeaders)
            return true
        }
        if (browseIntent.resolveActivity(packageManager) != null) {
            context.startActivity(browseIntent)
            return true
        }
        if (url.startsWith("intent:")) {
            try {
                val intent = Intent.parseUri(url, Intent.URI_INTENT_SCHEME)
                if (intent.resolveActivity(context.packageManager) != null) {
                    try {
                        context.startActivity(intent)
                    } catch (e: Exception) {
                        NinjaToast.show(context, R.string.toast_load_error)
                    }
                    return true
                }
                // try to find fallback url
                val fallbackUrl = intent.getStringExtra("browser_fallback_url")
                if (fallbackUrl != null) {
                    webView.loadUrl(fallbackUrl)
                    return true
                }
                // invite to install
                val marketIntent =
                    Intent(Intent.ACTION_VIEW).setData(Uri.parse("market://details?id=" + intent.getPackage()!!))
                if (marketIntent.resolveActivity(packageManager) != null) {
                    context.startActivity(marketIntent)
                    return true
                }
            } catch (e: URISyntaxException) {
                // not an intent uri
                return false
            }

        }
        return true // do nothing in other cases
    }

    override fun onFormResubmission(view: WebView, doNotResend: Message, resend: Message) {
        val holder = IntentUnit.getContext() as? Activity ?: return

        val dialog = BottomSheetDialog(holder)
        val dialogView = View.inflate(holder, R.layout.dialog_action, null)
        val textView = dialogView.findViewById<TextView>(R.id.dialog_text)
        textView.setText(R.string.dialog_content_resubmission)
        val actionOk = dialogView.findViewById<Button>(R.id.action_ok)
        actionOk.setOnClickListener {
            resend.sendToTarget()
            dialog.cancel()
        }
        val actionCancel = dialogView.findViewById<Button>(R.id.action_cancel)
        actionCancel.setOnClickListener {
            doNotResend.sendToTarget()
            dialog.cancel()
        }
        dialog.setContentView(dialogView)
        dialog.show()
        HelperUnit.setBottomSheetBehavior(dialog, dialogView, BottomSheetBehavior.STATE_EXPANDED)
    }

    override fun onReceivedSslError(view: WebView, handler: SslErrorHandler, error: SslError) {
        var message = "\"SSL Certificate error.\""
        when (error.primaryError) {
            SslError.SSL_UNTRUSTED -> message = "\"Certificate authority is not trusted.\""
            SslError.SSL_EXPIRED -> message = "\"Certificate has expired.\""
            SslError.SSL_IDMISMATCH -> message = "\"Certificate Hostname mismatch.\""
            SslError.SSL_NOTYETVALID -> message = "\"Certificate is not yet valid.\""
            SslError.SSL_DATE_INVALID -> message = "\"Certificate date is invalid.\""
            SslError.SSL_INVALID -> message = "\"Certificate is invalid.\""
        }
        val text = message + " - " + context.getString(R.string.dialog_content_ssl_error)

        val dialog = BottomSheetDialog(context)
        val dialogView = View.inflate(context, R.layout.dialog_action, null)
        val textView = dialogView.findViewById<TextView>(R.id.dialog_text)
        textView.text = text
        val actionOk = dialogView.findViewById<Button>(R.id.action_ok)
        actionOk.setOnClickListener {
            handler.proceed()
            dialog.cancel()
        }
        val actionCancel = dialogView.findViewById<Button>(R.id.action_cancel)
        actionCancel.setOnClickListener {
            handler.cancel()
            dialog.cancel()
        }
        dialog.setContentView(dialogView)
        dialog.show()
        HelperUnit.setBottomSheetBehavior(dialog, dialogView, BottomSheetBehavior.STATE_EXPANDED)
    }

    override fun onReceivedHttpAuthRequest(
        view: WebView, handler: HttpAuthHandler, host: String, realm: String
    ) {
        val dialog = BottomSheetDialog(context)
        val dialogView = View.inflate(context, R.layout.dialog_edit_bookmark, null)
        val passUserNameET = dialogView.findViewById<EditText>(R.id.pass_userName)
        val passUserPWET = dialogView.findViewById<EditText>(R.id.pass_userPW)
        val loginTitle = dialogView.findViewById<TextInputLayout>(R.id.login_title)
        loginTitle.visibility = View.GONE
        val actionOk = dialogView.findViewById<Button>(R.id.action_ok)
        actionOk.setOnClickListener {
            val user = passUserNameET.text.toString().trim { it <= ' ' }
            val pass = passUserPWET.text.toString().trim { it <= ' ' }
            handler.proceed(user, pass)
            dialog.cancel()
        }
        val actionCancel = dialogView.findViewById<Button>(R.id.action_cancel)
        actionCancel.setOnClickListener {
            handler.cancel()
            dialog.cancel()
        }
        dialog.setContentView(dialogView)
        dialog.show()
        HelperUnit.setBottomSheetBehavior(dialog, dialogView, BottomSheetBehavior.STATE_EXPANDED)
    }
}
