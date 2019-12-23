package com.home.androidbrowserdemo.view.activity

import android.annotation.SuppressLint
import android.app.*
import android.content.*
import android.content.ContentValues.TAG
import android.content.pm.ActivityInfo
import android.database.Cursor
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.print.PrintAttributes
import android.print.PrintManager
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.*
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import androidx.preference.PreferenceManager
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.home.androidbrowserdemo.R
import com.home.androidbrowserdemo.controller.AlbumController
import com.home.androidbrowserdemo.controller.BrowserContainer
import com.home.androidbrowserdemo.controller.BrowserController
import com.home.androidbrowserdemo.controller.listener.SwipeTouchListener
import com.home.androidbrowserdemo.controller.unit.BrowserUnit
import com.home.androidbrowserdemo.controller.unit.HelperUnit
import com.home.androidbrowserdemo.controller.unit.IntentUnit
import com.home.androidbrowserdemo.controller.unit.ViewUnit
import com.home.androidbrowserdemo.model.BookmarkList
import com.home.androidbrowserdemo.model.RecordAction
import com.home.androidbrowserdemo.utils.NinjaToast
import com.home.androidbrowserdemo.view.adapter.CompleteAdapter
import com.home.androidbrowserdemo.view.components.NinjaWebView
import com.home.androidbrowserdemo.view.fragment.MainAllPaginationFragment
import com.mobapphome.mahencryptorlib.MAHEncryptor
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*
import kotlin.math.abs
import kotlin.math.floor
import kotlin.math.roundToInt

class MainActivity : AppCompatActivity(), BrowserController, View.OnClickListener {

    companion object {
        private const val INPUT_FILE_REQUEST_CODE = 1
        private fun hideKeyboard(activity: Activity) {
            val imm =
                activity.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
            //Find the currently focused view, so we can grab the correct window token from it.
            var view = activity.currentFocus
            //If no view currently has focus, create a new one, just so we can grab a window token from it
            if (view == null) {
                view = View(activity)
            }
            Objects.requireNonNull(imm).hideSoftInputFromWindow(view.windowToken, 0)
        }
    }

    var isShowEchelonFragment = false
    lateinit var echelonFragment: MainAllPaginationFragment
    lateinit var fragmentManager: FragmentManager
    lateinit var fragmentTransaction: FragmentTransaction
    private var menuTabPreview: LinearLayout? = null
    private var menu_newTabOpen: LinearLayout? = null
    private var menu_closeTab: LinearLayout? = null
    private var menu_quit: LinearLayout? = null
    private var menu_shareScreenshot: LinearLayout? = null
    private var menu_shareLink: LinearLayout? = null
    private var menu_sharePDF: LinearLayout? = null
    private var menu_openWith: LinearLayout? = null
    private var menu_searchSite: LinearLayout? = null
    private var menu_settings: LinearLayout? = null
    private var menu_download: LinearLayout? = null
    private var menu_saveScreenshot: LinearLayout? = null
    private var menu_saveBookmark: LinearLayout? = null
    private var menu_savePDF: LinearLayout? = null
    private var menu_saveStart: LinearLayout? = null
    private var menu_fileManager: LinearLayout? = null
    private val menu_fav: LinearLayout? = null
    private val menu_sc: LinearLayout? = null
    private val menu_openFav: LinearLayout? = null
    private val menu_shareCP: LinearLayout? = null
    private var floatButton_tabView: View? = null
    private var floatButton_saveView: View? = null
    private var floatButton_shareView: View? = null
    private var floatButton_moreView: View? = null
    private var tab_plus: ImageButton? = null
    private var tab_plus_bottom: ImageButton? = null
    private var searchUp: ImageButton? = null
    private var searchDown: ImageButton? = null
    private var searchCancel: ImageButton? = null
    private var quantityTextView: TextView? = null
    private var omniboxOverflow: ImageButton? = null
    private var omniboxOverview: ImageView? = null
    private var open_startPage: ImageButton? = null
    private var open_bookmark: ImageButton? = null
    private var open_history: ImageButton? = null
    private var open_menu: ImageButton? = null
    private var fab_imageButtonNav: FloatingActionButton? = null
    private var inputBox: AutoCompleteTextView? = null
    private var progressBar: ProgressBar? = null
    private var searchBox: EditText? = null
    private var bottomSheetDialog: BottomSheetDialog? = null
    private var bottomSheetDialog_OverView: BottomSheetDialog? = null
    private var ninjaWebView: NinjaWebView? = null
    private var listView: ListView? = null
    private var omniboxTitle: TextView? = null
    private var gridView: GridView? = null
    private var customView: View? = null
    private var videoView: VideoView? = null
    private var tab_ScrollView: HorizontalScrollView? = null
    private var overview_top: LinearLayout? = null
    private var overview_topButtons: LinearLayout? = null
    private var appBar: RelativeLayout? = null
    private var omnibox: RelativeLayout? = null
    private var searchPanel: RelativeLayout? = null
    private var contentFrame: FrameLayout? = null
    private var tab_container: LinearLayout? = null
    private var fullscreenHolder: FrameLayout? = null
    private var title: String? = null
    private var url: String? = null
    private val overViewTab: String? = null
    private var downloadReceiver: BroadcastReceiver? = null
    private var mBehavior: BottomSheetBehavior<*>? = null
    private var activity: Activity? = null
    private var context: Context? = null
    private var sp: SharedPreferences? = null
    private var mahEncryptor: MAHEncryptor? = null
    private var originalOrientation: Int = 0
    private var dimen156dp: Float = 0.toFloat()
    private var dimen117dp: Float = 0.toFloat()
    private var searchOnSite: Boolean = false
    private var onPause: Boolean = false
    private var customViewCallback: WebChromeClient.CustomViewCallback? = null
    private var filePathCallback: ValueCallback<Array<Uri>>? = null
    private var currentAlbumController: AlbumController? = null
    private var mFilePathCallback: ValueCallback<Array<Uri>>? = null

    private fun prepareRecord(): Boolean {
        val webView = currentAlbumController as NinjaWebView?
        val title = webView!!.title
        val url = webView.url
        return (title == null
                || title.isEmpty()
                || url == null
                || url.isEmpty()
                || url.startsWith(BrowserUnit.URL_SCHEME_ABOUT)
                || url.startsWith(BrowserUnit.URL_SCHEME_MAIL_TO)
                || url.startsWith(BrowserUnit.URL_SCHEME_INTENT))
    }

    private inner class VideoCompletionListener : MediaPlayer.OnCompletionListener,
        MediaPlayer.OnErrorListener {
        override fun onError(mp: MediaPlayer, what: Int, extra: Int): Boolean {
            return false
        }

        override fun onCompletion(mp: MediaPlayer) {
            onHideCustomView()
        }
    }

    @SuppressLint("ApplySharedPref")
    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Android對webview做了優化, 旨在減少內存佔用以提高性能
        // 因此在默認情況下會智能的繪製html中需要繪製的部分, 其實就是當前屏幕展示的html內容
        // 因此會出現未顯示的圖像是空白的, 解決辦法是調用enableSlowWholeDocumentDraw()
        WebView.enableSlowWholeDocumentDraw()
        fragmentManager = supportFragmentManager
        context = this@MainActivity
        activity = this@MainActivity
        sp = PreferenceManager.getDefaultSharedPreferences(context!!)
        sp!!.edit().putInt("restart_changed", 0).apply()
        sp!!.edit().putBoolean("pdf_create", false).apply()
        // 亮色狀態欄黑色字體模式
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        }
        HelperUnit.applyTheme(context)
        setContentView(R.layout.activity_main)
        if (Objects.requireNonNull(sp!!.getString("saved_key_ok", "no")) == "no") {
            val chars =
                "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890!§$%&/()=?;:_-.,+#*<>".toCharArray()
            val sb = StringBuilder()
            val random = Random()
            for (i in 0..24) {
                val c = chars[random.nextInt(chars.size)]
                sb.append(c)
            }
            if (Locale.getDefault().country == "CN") {
                sp!!.edit().putString(getString(R.string.sp_search_engine), "2").apply()
            }
            sp!!.edit().putString("saved_key", sb.toString()).apply()
            sp!!.edit().putString("saved_key_ok", "yes").apply()
            sp!!.edit().putString("setting_gesture_tb_up", "08").apply()
            sp!!.edit().putString("setting_gesture_tb_down", "01").apply()
            sp!!.edit().putString("setting_gesture_tb_left", "07").apply()
            sp!!.edit().putString("setting_gesture_tb_right", "06").apply()
            sp!!.edit().putString("setting_gesture_nav_up", "04").apply()
            sp!!.edit().putString("setting_gesture_nav_down", "05").apply()
            sp!!.edit().putString("setting_gesture_nav_left", "03").apply()
            sp!!.edit().putString("setting_gesture_nav_right", "02").apply()
            sp!!.edit().putBoolean(getString(R.string.sp_location), false).apply()
        }
        try {
            mahEncryptor =
                MAHEncryptor.newInstance(Objects.requireNonNull(sp!!.getString("saved_key", "")))
        } catch (e: Exception) {
            e.printStackTrace()
        }
        contentFrame = findViewById(R.id.main_content)
        appBar = findViewById(R.id.appBar)
        dimen156dp = resources.getDimensionPixelSize(R.dimen.layout_width_156dp).toFloat()
        dimen117dp = resources.getDimensionPixelSize(R.dimen.layout_height_117dp).toFloat()
        initOmnibox()
        initSearchPanel()
        initOverview()
        downloadReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                bottomSheetDialog = BottomSheetDialog(context)
                val dialogView = View.inflate(context, R.layout.dialog_action, null)
                val textView = dialogView.findViewById<TextView>(R.id.dialog_text)
                textView.setText(R.string.toast_downloadComplete)
                val action_ok = dialogView.findViewById<Button>(R.id.action_ok)
                action_ok.setOnClickListener {
                    startActivity(Intent(DownloadManager.ACTION_VIEW_DOWNLOADS))
                    hideBottomSheetDialog()
                }
                val action_cancel = dialogView.findViewById<Button>(R.id.action_cancel)
                action_cancel.setOnClickListener { hideBottomSheetDialog() }
                bottomSheetDialog!!.setContentView(dialogView)
                bottomSheetDialog!!.show()
                HelperUnit.setBottomSheetBehavior(
                    bottomSheetDialog,
                    dialogView,
                    BottomSheetBehavior.STATE_EXPANDED
                )
            }
        }
        val filter = IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE)
        registerReceiver(downloadReceiver, filter)
        dispatchIntent(intent)
        if (sp!!.getBoolean("start_tabStart", false)) {
            showOverview()
            Handler().postDelayed({ mBehavior!!.state = BottomSheetBehavior.STATE_EXPANDED }, 250)
        }
    }

    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode != INPUT_FILE_REQUEST_CODE || mFilePathCallback == null) {
            super.onActivityResult(requestCode, resultCode, data)
            return
        }
        var results: Array<Uri>? = null
        // Check that the response is a good one
        if (resultCode == Activity.RESULT_OK) {
            if (data != null) {
                // If there is not data, then we may have taken a photo
                val dataString = data.dataString
                if (dataString != null) {
                    results = arrayOf(Uri.parse(dataString))
                }
            }
        }
        mFilePathCallback!!.onReceiveValue(results)
        mFilePathCallback = null
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
    }

    override fun onPause() {
        onPause = true
        super.onPause()
    }

    @SuppressLint("ApplySharedPref")
    public override fun onResume() {
        super.onResume()
        val list = BrowserContainer.list()
        quantityTextView!!.text = list.size.toString()
        if (sp!!.getInt("restart_changed", 1) == 1) {
            sp!!.edit().putInt("restart_changed", 0).apply()
            val dialog = BottomSheetDialog(context!!)
            val dialogView = View.inflate(context, R.layout.dialog_action, null)
            val textView = dialogView.findViewById<TextView>(R.id.dialog_text)
            textView.setText(R.string.toast_restart)
            val action_ok = dialogView.findViewById<Button>(R.id.action_ok)
            action_ok.setOnClickListener { onDestroy() }
            val action_cancel = dialogView.findViewById<Button>(R.id.action_cancel)
            action_cancel.setOnClickListener { dialog.cancel() }
            dialog.setContentView(dialogView)
            dialog.show()
            HelperUnit.setBottomSheetBehavior(
                dialog,
                dialogView,
                BottomSheetBehavior.STATE_EXPANDED
            )
        }
        dispatchIntent(intent)
        updateOmnibox()
        if (sp!!.getBoolean("pdf_create", false)) {
            sp!!.edit().putBoolean("pdf_create", false).apply()
            if (sp!!.getBoolean("pdf_share", false)) {
                sp!!.edit().putBoolean("pdf_share", false).apply()
                startActivity(Intent(DownloadManager.ACTION_VIEW_DOWNLOADS))
            } else {
                bottomSheetDialog = BottomSheetDialog(context!!)
                val dialogView = View.inflate(context, R.layout.dialog_action, null)
                val textView = dialogView.findViewById<TextView>(R.id.dialog_text)
                textView.setText(R.string.toast_downloadComplete)
                val action_ok = dialogView.findViewById<Button>(R.id.action_ok)
                action_ok.setOnClickListener {
                    startActivity(Intent(DownloadManager.ACTION_VIEW_DOWNLOADS))
                    hideBottomSheetDialog()
                }
                val action_cancel = dialogView.findViewById<Button>(R.id.action_cancel)
                action_cancel.setOnClickListener { hideBottomSheetDialog() }
                bottomSheetDialog!!.setContentView(dialogView)
                bottomSheetDialog!!.show()
                HelperUnit.setBottomSheetBehavior(
                    bottomSheetDialog,
                    dialogView,
                    BottomSheetBehavior.STATE_EXPANDED
                )
            }
        }
    }

    public override fun onDestroy() {
        BrowserContainer.clear()
        IntentUnit.setContext(null)
        unregisterReceiver(downloadReceiver)
        super.onDestroy()
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        when (keyCode) {
            KeyEvent.KEYCODE_MENU -> return showOverflow()
            KeyEvent.KEYCODE_BACK -> {
                hideKeyboard(activity!!)
                hideOverview()
                if (isShowEchelonFragment) {
                    showEchelonFragment(false)
                } else if (fullscreenHolder != null || customView != null || videoView != null) {
                    return onHideCustomView()
                } else if (omnibox!!.visibility == View.GONE && sp!!.getBoolean(
                        "sp_toolbarShow",
                        true
                    )
                ) {
                    showOmnibox()
                } else {
                    if (ninjaWebView!!.canGoBack()) {
                        ninjaWebView!!.goBack()
                    } else {
                        removeAlbum(currentAlbumController)
                    }
                }
                return true
            }
        }
        return false
    }

    @Synchronized
    override fun showAlbum(albumController: AlbumController) {
        if (currentAlbumController != null) {
            currentAlbumController!!.deactivate()
            val av = albumController as View?
            contentFrame!!.removeAllViews()
            contentFrame!!.addView(av)
        } else {
            contentFrame!!.removeAllViews()
            contentFrame!!.addView(albumController as View?)
        }
        currentAlbumController = albumController
        currentAlbumController!!.activate()
        updateOmnibox()
    }

    override fun updateAutoComplete() {
        val action = RecordAction(this)
        action.open(false)
        val list = action.listEntries(activity, true)
        action.close()
        val adapter = CompleteAdapter(
            this,
            R.layout.complete_item,
            list
        )
        inputBox!!.setAdapter(adapter)
        adapter.notifyDataSetChanged()
        inputBox!!.threshold = 1
        inputBox!!.dropDownVerticalOffset = -16
        inputBox!!.dropDownWidth = ViewUnit.getWindowWidth(this)
        inputBox!!.onItemClickListener =
            AdapterView.OnItemClickListener { parent, view, position, id ->
                val url =
                    (view.findViewById<View>(R.id.complete_item_url) as TextView).text.toString()
                updateAlbum(url)
                hideKeyboard(activity!!)
            }
    }

    private fun showOverview() {
        overview_top!!.visibility = View.VISIBLE
        overview_topButtons!!.visibility = View.VISIBLE
        mBehavior!!.state = BottomSheetBehavior.STATE_COLLAPSED
        if (currentAlbumController != null) {
            currentAlbumController!!.deactivate()
            currentAlbumController!!.activate()
        }
        bottomSheetDialog_OverView!!.show()
        Handler().postDelayed({
            tab_ScrollView!!.smoothScrollTo(
                currentAlbumController!!.albumView.left,
                0
            )
        }, 250)
    }

    override fun hideOverview() {
        if (bottomSheetDialog_OverView != null) {
            bottomSheetDialog_OverView!!.cancel()
        }
    }

    private fun hideBottomSheetDialog() {
        if (bottomSheetDialog != null) {
            bottomSheetDialog!!.cancel()
        }
    }

    override fun onClick(v: View) {
        RecordAction(context)
        ninjaWebView = currentAlbumController as NinjaWebView?
        try {
            title = ninjaWebView!!.title.trim { it <= ' ' }
            url = ninjaWebView!!.url.trim { it <= ' ' }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        when (v.id) {
            // Menu overflow
            R.id.tab_plus -> {
                hideBottomSheetDialog()
                hideOverview()
                addAlbum(
                    getString(R.string.app_name),
                    sp!!.getString("favoriteURL", "https://github.com/scoute-dich/browser"),
                    true
                )
            }
            R.id.tab_plus_bottom, R.id.menu_newTabOpen -> {
                hideBottomSheetDialog()
                hideOverview()
                addAlbum(
                    getString(R.string.app_name),
                    sp!!.getString("favoriteURL", "https://github.com/scoute-dich/browser"),
                    true
                )
            }
            R.id.menu_closeTab -> {
                hideBottomSheetDialog()
                removeAlbum(currentAlbumController)
            }
            R.id.menu_tabPreview -> {
                hideBottomSheetDialog()
                showOverview()
            }
            R.id.menu_quit -> {
                hideBottomSheetDialog()
                doubleTapsQuit()
            }
            R.id.menu_shareLink -> {
                hideBottomSheetDialog()
                if (prepareRecord()) {
                    NinjaToast.show(context, getString(R.string.toast_share_failed))
                } else {
                    IntentUnit.share(context!!, title, url)
                }
            }
            R.id.menu_sharePDF -> {
                hideBottomSheetDialog()
                printPDF(true)
            }
            R.id.menu_openWith -> {
                hideBottomSheetDialog()
                val intent = Intent(Intent.ACTION_VIEW)
                intent.data = Uri.parse(url)
                val chooser = Intent.createChooser(intent, getString(R.string.menu_open_with))
                startActivity(chooser)
            }
            // Omnibox
            R.id.menu_searchSite -> {
                hideBottomSheetDialog()
                hideKeyboard(activity!!)
                showSearchPanel()
            }
            R.id.contextLink_saveAs -> {
                hideBottomSheetDialog()
                printPDF(false)
            }
            R.id.menu_fileManager -> {
                hideBottomSheetDialog()
                val intent2 = Intent(Intent.ACTION_VIEW)
                intent2.type = "*/*"
                intent2.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                context!!.startActivity(Intent.createChooser(intent2, null))
            }
            R.id.menu_download -> {
                hideBottomSheetDialog()
                startActivity(Intent(DownloadManager.ACTION_VIEW_DOWNLOADS))
            }
            R.id.floatButton_tab -> {
                menu_newTabOpen!!.visibility = View.VISIBLE
                menu_closeTab!!.visibility = View.VISIBLE
                menuTabPreview!!.visibility = View.VISIBLE
                menu_quit!!.visibility = View.VISIBLE
                menu_shareScreenshot!!.visibility = View.GONE
                menu_shareLink!!.visibility = View.GONE
                menu_sharePDF!!.visibility = View.GONE
                menu_openWith!!.visibility = View.GONE
                menu_saveScreenshot!!.visibility = View.GONE
                menu_saveBookmark!!.visibility = View.GONE
                menu_savePDF!!.visibility = View.GONE
                menu_saveStart!!.visibility = View.GONE
                floatButton_tabView!!.visibility = View.VISIBLE
                floatButton_saveView!!.visibility = View.INVISIBLE
                floatButton_shareView!!.visibility = View.INVISIBLE
                floatButton_moreView!!.visibility = View.INVISIBLE
                menu_searchSite!!.visibility = View.GONE
                menu_fileManager!!.visibility = View.GONE
                menu_settings!!.visibility = View.GONE
                menu_download!!.visibility = View.GONE
                menu_fav!!.visibility = View.GONE
                menu_sc!!.visibility = View.GONE
                menu_openFav!!.visibility = View.VISIBLE
                menu_shareCP!!.visibility = View.GONE
            }
            R.id.floatButton_share -> {
                menu_newTabOpen!!.visibility = View.GONE
                menu_closeTab!!.visibility = View.GONE
                menuTabPreview!!.visibility = View.GONE
                menu_quit!!.visibility = View.GONE
                menu_shareScreenshot!!.visibility = View.VISIBLE
                menu_shareLink!!.visibility = View.VISIBLE
                menu_sharePDF!!.visibility = View.VISIBLE
                menu_openWith!!.visibility = View.VISIBLE
                menu_saveScreenshot!!.visibility = View.GONE
                menu_saveBookmark!!.visibility = View.GONE
                menu_savePDF!!.visibility = View.GONE
                menu_saveStart!!.visibility = View.GONE
                floatButton_tabView!!.visibility = View.INVISIBLE
                floatButton_saveView!!.visibility = View.INVISIBLE
                floatButton_shareView!!.visibility = View.VISIBLE
                floatButton_moreView!!.visibility = View.INVISIBLE
                menu_searchSite!!.visibility = View.GONE
                menu_fileManager!!.visibility = View.GONE
                menu_settings!!.visibility = View.GONE
                menu_download!!.visibility = View.GONE
                menu_fav!!.visibility = View.GONE
                menu_sc!!.visibility = View.GONE
                menu_openFav!!.visibility = View.GONE
                menu_shareCP!!.visibility = View.VISIBLE
            }
            R.id.floatButton_save -> {
                menu_newTabOpen!!.visibility = View.GONE
                menu_closeTab!!.visibility = View.GONE
                menuTabPreview!!.visibility = View.GONE
                menu_quit!!.visibility = View.GONE
                menu_shareScreenshot!!.visibility = View.GONE
                menu_shareLink!!.visibility = View.GONE
                menu_sharePDF!!.visibility = View.GONE
                menu_openWith!!.visibility = View.GONE
                menu_saveScreenshot!!.visibility = View.VISIBLE
                menu_saveBookmark!!.visibility = View.VISIBLE
                menu_savePDF!!.visibility = View.VISIBLE
                menu_saveStart!!.visibility = View.VISIBLE
                menu_searchSite!!.visibility = View.GONE
                menu_fileManager!!.visibility = View.GONE
                floatButton_tabView!!.visibility = View.INVISIBLE
                floatButton_saveView!!.visibility = View.VISIBLE
                floatButton_shareView!!.visibility = View.INVISIBLE
                floatButton_moreView!!.visibility = View.INVISIBLE
                menu_settings!!.visibility = View.GONE
                menu_download!!.visibility = View.GONE
                menu_fav!!.visibility = View.GONE
                menu_sc!!.visibility = View.VISIBLE
                menu_openFav!!.visibility = View.GONE
                menu_shareCP!!.visibility = View.GONE
            }
            R.id.floatButton_more -> {
                menu_newTabOpen!!.visibility = View.GONE
                menu_closeTab!!.visibility = View.GONE
                menuTabPreview!!.visibility = View.GONE
                menu_quit!!.visibility = View.GONE
                menu_shareScreenshot!!.visibility = View.GONE
                menu_shareLink!!.visibility = View.GONE
                menu_sharePDF!!.visibility = View.GONE
                menu_openWith!!.visibility = View.GONE
                menu_saveScreenshot!!.visibility = View.GONE
                menu_saveBookmark!!.visibility = View.GONE
                menu_savePDF!!.visibility = View.GONE
                menu_saveStart!!.visibility = View.GONE
                floatButton_tabView!!.visibility = View.INVISIBLE
                floatButton_saveView!!.visibility = View.INVISIBLE
                floatButton_shareView!!.visibility = View.INVISIBLE
                floatButton_moreView!!.visibility = View.VISIBLE
                menu_settings!!.visibility = View.VISIBLE
                menu_searchSite!!.visibility = View.VISIBLE
                menu_fileManager!!.visibility = View.VISIBLE
                menu_download!!.visibility = View.VISIBLE
                menu_fav!!.visibility = View.VISIBLE
                menu_sc!!.visibility = View.GONE
                menu_openFav!!.visibility = View.GONE
                menu_shareCP!!.visibility = View.GONE
            }
        }
    }

    private fun printPDF(share: Boolean) {
        try {
            if (share) {
                sp!!.edit().putBoolean("pdf_share", true).apply()
            } else {
                sp!!.edit().putBoolean("pdf_share", false).apply()
            }
            val title = HelperUnit.fileName(ninjaWebView!!.url)
            val printManager = getSystemService(Context.PRINT_SERVICE) as PrintManager
            val printAdapter = ninjaWebView!!.createPrintDocumentAdapter(title)
            Objects.requireNonNull(printManager)
                .print(title, printAdapter, PrintAttributes.Builder().build())
            sp!!.edit().putBoolean("pdf_create", true).apply()
        } catch (e: Exception) {
            NinjaToast.show(context, R.string.toast_error)
            sp!!.edit().putBoolean("pdf_create", false).apply()
            e.printStackTrace()
        }
    }


    private fun dispatchIntent(intent: Intent) {
        val action = intent.action
        val url = intent.getStringExtra(Intent.EXTRA_TEXT)
        if ("" == action) {
            Log.i(TAG, "resumed FOSS browser")
        } else if (intent.action != null && intent.action == Intent.ACTION_WEB_SEARCH) {
            addAlbum(null, intent.getStringExtra(SearchManager.QUERY), true)
        } else if (filePathCallback != null) {
            filePathCallback = null
        } else if ("sc_history" == action) {
            addAlbum(
                null,
                sp!!.getString("favoriteURL", "https://github.com/scoute-dich/browser"),
                true
            )
            showOverview()
            Handler().postDelayed({ open_history!!.performClick() }, 250)
        } else if ("sc_bookmark" == action) {
            addAlbum(
                null,
                sp!!.getString("favoriteURL", "https://github.com/scoute-dich/browser"),
                true
            )
            showOverview()
            Handler().postDelayed({ open_bookmark!!.performClick() }, 250)
        } else if ("sc_startPage" == action) {
            addAlbum(
                null,
                sp!!.getString("favoriteURL", "https://github.com/scoute-dich/browser"),
                true
            )
            showOverview()
            Handler().postDelayed({ open_startPage!!.performClick() }, 250)
        } else if (Intent.ACTION_SEND == action) {
            addAlbum(null, url, true)
        } else {
            if (!onPause) {
                addAlbum(
                    null,
                    sp!!.getString("favoriteURL", "https://github.com/tenSunFree"),
                    true
                )
            }
        }
        getIntent().action = ""
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun initOmnibox() {
        omnibox = findViewById(R.id.main_omnibox)
        inputBox = findViewById(R.id.main_omnibox_input)
        quantityTextView = findViewById(R.id.text_view_quantity)
        omniboxOverview = findViewById(R.id.omnibox_overview)
        omniboxOverflow = findViewById(R.id.omnibox_overflow)
        omniboxTitle = findViewById(R.id.omnibox_title)
        progressBar = findViewById(R.id.main_progress_bar)
        text_view_quantity.setOnClickListener {
            showOverflow()
        }
        val nav_position = Objects.requireNonNull(sp!!.getString("nav_position", "0"))
        fab_imageButtonNav = when (nav_position) {
            "1" -> {
                findViewById(R.id.fab_imageButtonNav_left)
            }
            "2" -> {
                findViewById(R.id.fab_imageButtonNav_center)
            }
            else -> {
                findViewById(R.id.fab_imageButtonNav_right)
            }
        }
        fab_imageButtonNav!!.setOnLongClickListener {
            show_dialogFastToggle()
            false
        }
        omniboxOverflow!!.setOnLongClickListener {
            show_dialogFastToggle()
            false
        }
        fab_imageButtonNav!!.setOnClickListener {
            Toast.makeText(activity, "click fab_imageButtonNav", Toast.LENGTH_SHORT).show()
        }
        omniboxOverflow!!.setOnClickListener {
            Toast.makeText(activity, "click omniboxOverflow", Toast.LENGTH_SHORT).show()
        }
        if (sp!!.getBoolean("sp_gestures_use", true)) {
            fab_imageButtonNav!!.setOnTouchListener(object : SwipeTouchListener(context) {
                override fun onSwipeTop() {
                    performGesture("setting_gesture_nav_up")
                }

                override fun onSwipeBottom() {
                    performGesture("setting_gesture_nav_down")
                }

                override fun onSwipeRight() {
                    performGesture("setting_gesture_nav_right")
                }

                override fun onSwipeLeft() {
                    performGesture("setting_gesture_nav_left")
                }
            })

            omniboxOverflow!!.setOnTouchListener(object : SwipeTouchListener(context) {
                override fun onSwipeTop() {
                    performGesture("setting_gesture_nav_up")
                }

                override fun onSwipeBottom() {
                    performGesture("setting_gesture_nav_down")
                }

                override fun onSwipeRight() {
                    performGesture("setting_gesture_nav_right")
                }

                override fun onSwipeLeft() {
                    performGesture("setting_gesture_nav_left")
                }
            })

            inputBox!!.setOnTouchListener(object : SwipeTouchListener(context) {
                override fun onSwipeTop() {
                    performGesture("setting_gesture_tb_up")
                }

                override fun onSwipeBottom() {
                    performGesture("setting_gesture_tb_down")
                }

                override fun onSwipeRight() {
                    performGesture("setting_gesture_tb_right")
                }

                override fun onSwipeLeft() {
                    performGesture("setting_gesture_tb_left")
                }
            })
        }

        inputBox!!.setOnEditorActionListener(TextView.OnEditorActionListener { v, actionId, event ->
            val query = inputBox!!.text.toString().trim { it <= ' ' }
            if (query.isEmpty()) {
                NinjaToast.show(context, getString(R.string.toast_input_empty))
                return@OnEditorActionListener true
            }
            updateAlbum(query)
            showOmnibox()
            false
        })

        inputBox!!.onFocusChangeListener = View.OnFocusChangeListener { v, hasFocus ->
            if (inputBox!!.hasFocus()) {
                ninjaWebView!!.stopLoading()
                inputBox!!.setText(ninjaWebView!!.url)
                Handler().postDelayed({
                    omniboxTitle!!.visibility = View.GONE
                    inputBox!!.setSelection(0, inputBox!!.text.toString().length)
                }, 250)
            } else {
                omniboxTitle!!.visibility = View.VISIBLE
                omniboxTitle!!.text = ninjaWebView!!.title
                hideKeyboard(activity!!)
            }
        }
        updateAutoComplete()
        omniboxOverview!!.setOnClickListener(this)
    }

    private fun performGesture(gesture: String) {
        val gestureAction = Objects.requireNonNull(sp!!.getString(gesture, "0"))
        val controller: AlbumController
        ninjaWebView = currentAlbumController as NinjaWebView?
        when (gestureAction) {
            "01" -> {
            }
            "02" -> if (ninjaWebView!!.canGoForward()) {
                ninjaWebView!!.goForward()
            } else {
                NinjaToast.show(context, R.string.toast_webview_forward)
            }
            "03" -> if (ninjaWebView!!.canGoBack()) {
                ninjaWebView!!.goBack()
            } else {
                removeAlbum(currentAlbumController)
            }
            "04" -> ninjaWebView!!.pageUp(true)
            "05" -> ninjaWebView!!.pageDown(true)
            "06" -> {
                controller = this.nextAlbumController(false)!!
                showAlbum(controller)
            }
            "07" -> {
                controller = this.nextAlbumController(true)!!
                showAlbum(controller)
            }
            "08" -> showOverview()
            "09" -> {
                addAlbum(
                    getString(R.string.app_name),
                    sp!!.getString("favoriteURL", "https://github.com/scoute-dich/browser"),
                    true
                )
            }
            "10" -> removeAlbum(currentAlbumController)
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun initOverview() {
        bottomSheetDialog_OverView = BottomSheetDialog(context!!)
        val dialogView = View.inflate(context, R.layout.dialog_overiew, null)
        open_startPage = dialogView.findViewById(R.id.open_newTab_2)
        open_bookmark = dialogView.findViewById(R.id.open_bookmark_2)
        open_history = dialogView.findViewById(R.id.open_history_2)
        open_menu = dialogView.findViewById(R.id.open_menu)
        tab_container = dialogView.findViewById(R.id.tab_container)
        tab_plus = dialogView.findViewById(R.id.tab_plus)
        tab_plus!!.setOnClickListener(this)
        tab_plus_bottom = dialogView.findViewById(R.id.tab_plus_bottom)
        tab_plus_bottom!!.setOnClickListener(this)
        tab_ScrollView = dialogView.findViewById(R.id.tab_ScrollView)
        overview_top = dialogView.findViewById(R.id.overview_top)
        overview_topButtons = dialogView.findViewById(R.id.overview_topButtons)
        listView = dialogView.findViewById(R.id.home_list_2)
        gridView = dialogView.findViewById(R.id.home_grid_2)
        val overview_titleIcons_start =
            dialogView.findViewById<ImageButton>(R.id.overview_titleIcons_start)
        val overview_titleIcons_bookmarks =
            dialogView.findViewById<ImageButton>(R.id.overview_titleIcons_bookmarks)
        val overview_titleIcons_history =
            dialogView.findViewById<ImageButton>(R.id.overview_titleIcons_history)
        // allow scrolling in listView without closing the bottomSheetDialog
        listView!!.setOnTouchListener { v, event ->
            val action = event.action
            if (action == MotionEvent.ACTION_DOWN) {// Disallow NestedScrollView to intercept touch events.
                if (listView!!.canScrollVertically(-1)) {
                    v.parent.requestDisallowInterceptTouchEvent(true)
                }
            }
            // Handle ListView touch events.
            v.onTouchEvent(event)
            true
        }
        gridView!!.setOnTouchListener { v, event ->
            val action = event.action
            if (action == MotionEvent.ACTION_DOWN) {// Disallow NestedScrollView to intercept touch events.
                if (gridView!!.canScrollVertically(-1)) {
                    v.parent.requestDisallowInterceptTouchEvent(true)
                }
            }
            // Handle ListView touch events.
            v.onTouchEvent(event)
            true
        }
        bottomSheetDialog_OverView!!.setContentView(dialogView)
        mBehavior = BottomSheetBehavior.from(dialogView.parent as View)
        val peekHeight = (200 * resources.displayMetrics.density).roundToInt()
        mBehavior!!.peekHeight = peekHeight
        mBehavior!!.setBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                if (newState == BottomSheetBehavior.STATE_HIDDEN) {
                    hideOverview()
                } else if (newState == BottomSheetBehavior.STATE_EXPANDED) {
                    if (overViewTab == getString(R.string.album_title_bookmarks)) {
                        initBookmarkList()
                    } else if (overViewTab == getString(R.string.album_title_home)) {
                        open_startPage!!.performClick()
                    }
                    if (sp!!.getBoolean("overView_hide", true)) {
                        overview_top!!.visibility = View.GONE
                    } else {
                        overview_topButtons!!.visibility = View.GONE
                    }
                } else {
                    if (sp!!.getBoolean("overView_hide", true)) {
                        overview_top!!.visibility = View.VISIBLE
                    } else {
                        overview_topButtons!!.visibility = View.VISIBLE
                    }
                }
            }

            override fun onSlide(bottomSheet: View, slideOffset: Float) {
            }
        })
        overview_titleIcons_history.setOnClickListener { open_history!!.performClick() }
        when (Objects.requireNonNull(sp!!.getString("start_tab", "0"))) {
            "3" -> {
                overview_top!!.visibility = View.GONE
                open_bookmark!!.performClick()
            }
            "4" -> {
                overview_top!!.visibility = View.GONE
                open_history!!.performClick()
            }
            else -> {
                overview_top!!.visibility = View.GONE
                open_startPage!!.performClick()
            }
        }
    }

    private fun initSearchPanel() {
        searchPanel = findViewById(R.id.main_search_panel)
        searchBox = findViewById(R.id.main_search_box)
        searchUp = findViewById(R.id.main_search_up)
        searchDown = findViewById(R.id.main_search_down)
        searchCancel = findViewById(R.id.main_search_cancel)
        searchBox!!.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable) {
                if (currentAlbumController != null) {
                    (currentAlbumController as NinjaWebView).findAllAsync(s.toString())
                }
            }
        })
        searchBox!!.setOnEditorActionListener(TextView.OnEditorActionListener { v, actionId, event ->
            if (actionId != EditorInfo.IME_ACTION_DONE) {
                return@OnEditorActionListener false
            }
            if (searchBox!!.text.toString().isEmpty()) {
                NinjaToast.show(context, getString(R.string.toast_input_empty))
                return@OnEditorActionListener true
            }
            false
        })
        searchUp!!.setOnClickListener(View.OnClickListener {
            val query = searchBox!!.text.toString()
            if (query.isEmpty()) {
                NinjaToast.show(context, getString(R.string.toast_input_empty))
                return@OnClickListener
            }
            hideKeyboard(activity!!)
            (currentAlbumController as NinjaWebView).findNext(false)
        })
        searchDown!!.setOnClickListener(View.OnClickListener {
            val query = searchBox!!.text.toString()
            if (query.isEmpty()) {
                NinjaToast.show(context, getString(R.string.toast_input_empty))
                return@OnClickListener
            }
            hideKeyboard(activity!!)
            (currentAlbumController as NinjaWebView).findNext(true)
        })
        searchCancel!!.setOnClickListener { hideSearchPanel() }
    }

    private fun initBookmarkList() {
        val db = BookmarkList(context)
        val row: Cursor?
        db.open()
        val layoutStyle = R.layout.list_item_bookmark
        val xml_id = intArrayOf(R.id.record_item_title)
        val column = arrayOf("pass_title")
        val search = sp!!.getString("filter_bookmarks", "00")
        row = if (Objects.requireNonNull(search) == "00") {
            activity?.let { db.fetchAllData(it) }
        } else {
            db.fetchDataByFilter(search, "pass_creation")
        }
        val adapter = object : SimpleCursorAdapter(context, layoutStyle, row, column, xml_id, 0) {
            override fun getView(position: Int, convertView: View, parent: ViewGroup): View {
                val row = listView!!.getItemAtPosition(position) as Cursor
                val bookmarks_icon = row.getString(row.getColumnIndexOrThrow("pass_creation"))
                val v = super.getView(position, convertView, parent)
                val iv_icon = v.findViewById<ImageView>(R.id.ib_icon)
                HelperUnit.switchIcon(activity, bookmarks_icon, "pass_creation", iv_icon)
                return v
            }
        }
        listView!!.adapter = adapter
        listView!!.onItemClickListener =
            AdapterView.OnItemClickListener { _, _, _, _ ->
                val pass_content = row!!.getString(row.getColumnIndexOrThrow("pass_content"))
                val pass_icon = row.getString(row.getColumnIndexOrThrow("pass_icon"))
                val pass_attachment = row.getString(row.getColumnIndexOrThrow("pass_attachment"))
                updateAlbum(pass_content)
                toast_login(pass_icon, pass_attachment)
                hideOverview()
            }
        listView!!.onItemLongClickListener =
            AdapterView.OnItemLongClickListener { _, _, position, _ ->
                val row = listView!!.getItemAtPosition(position) as Cursor
                val _id = row.getString(row.getColumnIndexOrThrow("_id"))
                val pass_title = row.getString(row.getColumnIndexOrThrow("pass_title"))
                val pass_content = row.getString(row.getColumnIndexOrThrow("pass_content"))
                val pass_icon = row.getString(row.getColumnIndexOrThrow("pass_icon"))
                val pass_attachment = row.getString(row.getColumnIndexOrThrow("pass_attachment"))
                val pass_creation = row.getString(row.getColumnIndexOrThrow("pass_creation"))
                show_contextMenu_list(
                    pass_title, pass_content
                )
                true
            }
    }

    private fun show_dialogFastToggle() {
        bottomSheetDialog = BottomSheetDialog(context!!)
        val dialogView = View.inflate(context, R.layout.dialog_toggle, null)
        val sw_java = dialogView.findViewById<CheckBox>(R.id.switch_js)
        val sw_adBlock = dialogView.findViewById<CheckBox>(R.id.switch_adBlock)
        val sw_cookie = dialogView.findViewById<CheckBox>(R.id.switch_cookie)
        val dialog_title = dialogView.findViewById<TextView>(R.id.dialog_title)
        dialog_title.text = HelperUnit.domain(ninjaWebView!!.url)
        ninjaWebView = currentAlbumController as NinjaWebView?
        sw_java.isChecked = sp!!.getBoolean(getString(R.string.sp_javascript), true)
        sw_adBlock.isChecked = sp!!.getBoolean(getString(R.string.sp_ad_block), true)
        sw_cookie.isChecked = sp!!.getBoolean(getString(R.string.sp_cookies), true)
        sw_java.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                sp!!.edit().putBoolean(getString(R.string.sp_javascript), true).apply()
            } else {
                sp!!.edit().putBoolean(getString(R.string.sp_javascript), false).apply()
            }
        }
        sw_adBlock.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                sp!!.edit().putBoolean(getString(R.string.sp_ad_block), true).apply()
            } else {
                sp!!.edit().putBoolean(getString(R.string.sp_ad_block), false).apply()
            }
        }
        sw_cookie.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                sp!!.edit().putBoolean(getString(R.string.sp_cookies), true).apply()
            } else {
                sp!!.edit().putBoolean(getString(R.string.sp_cookies), false).apply()
            }
        }
        val toggle_history = dialogView.findViewById<ImageButton>(R.id.toggle_history)
        val toggle_historyView = dialogView.findViewById<View>(R.id.toggle_historyView)
        val toggle_location = dialogView.findViewById<ImageButton>(R.id.toggle_location)
        val toggle_locationView = dialogView.findViewById<View>(R.id.toggle_locationView)
        val toggle_images = dialogView.findViewById<ImageButton>(R.id.toggle_images)
        val toggle_imagesView = dialogView.findViewById<View>(R.id.toggle_imagesView)
        val toggle_remote = dialogView.findViewById<ImageButton>(R.id.toggle_remote)
        val toggle_remoteView = dialogView.findViewById<View>(R.id.toggle_remoteView)
        val toggle_invert = dialogView.findViewById<ImageButton>(R.id.toggle_invert)
        val toggle_invertView = dialogView.findViewById<View>(R.id.toggle_invertView)
        if (sp!!.getBoolean("saveHistory", false)) {
            toggle_historyView.visibility = View.VISIBLE
        } else {
            toggle_historyView.visibility = View.INVISIBLE
        }
        toggle_history.setOnClickListener {
            if (sp!!.getBoolean("saveHistory", false)) {
                toggle_historyView.visibility = View.INVISIBLE
                sp!!.edit().putBoolean("saveHistory", false).apply()
            } else {
                toggle_historyView.visibility = View.VISIBLE
                sp!!.edit().putBoolean("saveHistory", true).apply()
            }
        }
        if (sp!!.getBoolean(getString(R.string.sp_location), false)) {
            toggle_locationView.visibility = View.VISIBLE
        } else {
            toggle_locationView.visibility = View.INVISIBLE
        }
        toggle_location.setOnClickListener {
            if (sp!!.getBoolean(getString(R.string.sp_location), false)) {
                toggle_locationView.visibility = View.INVISIBLE
                sp!!.edit().putBoolean(getString(R.string.sp_location), false).apply()
            } else {
                toggle_locationView.visibility = View.VISIBLE
                sp!!.edit().putBoolean(getString(R.string.sp_location), true).apply()
            }
        }
        if (sp!!.getBoolean(getString(R.string.sp_images), true)) {
            toggle_imagesView.visibility = View.VISIBLE
        } else {
            toggle_imagesView.visibility = View.INVISIBLE
        }
        toggle_images.setOnClickListener {
            if (sp!!.getBoolean(getString(R.string.sp_images), true)) {
                toggle_imagesView.visibility = View.INVISIBLE
                sp!!.edit().putBoolean(getString(R.string.sp_images), false).apply()
            } else {
                toggle_imagesView.visibility = View.VISIBLE
                sp!!.edit().putBoolean(getString(R.string.sp_images), true).apply()
            }
        }
        if (sp!!.getBoolean("sp_remote", true)) {
            toggle_remoteView.visibility = View.VISIBLE
        } else {
            toggle_remoteView.visibility = View.INVISIBLE
        }
        toggle_remote.setOnClickListener {
            if (sp!!.getBoolean("sp_remote", true)) {
                toggle_remoteView.visibility = View.INVISIBLE
                sp!!.edit().putBoolean("sp_remote", false).apply()
            } else {
                toggle_remoteView.visibility = View.VISIBLE
                sp!!.edit().putBoolean("sp_remote", true).apply()
            }
        }
        if (sp!!.getBoolean("sp_invert", false)) {
            toggle_invertView.visibility = View.VISIBLE
        } else {
            toggle_invertView.visibility = View.INVISIBLE
        }
        toggle_invert.setOnClickListener {
            if (sp!!.getBoolean("sp_invert", false)) {
                toggle_invertView.visibility = View.INVISIBLE
                sp!!.edit().putBoolean("sp_invert", false).apply()
            } else {
                toggle_invertView.visibility = View.VISIBLE
                sp!!.edit().putBoolean("sp_invert", true).apply()
            }
            HelperUnit.initRendering(contentFrame)
        }
        val but_OK = dialogView.findViewById<Button>(R.id.action_ok)
        but_OK.setOnClickListener {
            if (ninjaWebView != null) {
                hideBottomSheetDialog()
                ninjaWebView!!.initPreferences()
                ninjaWebView!!.reload()
            }
        }
        val action_cancel = dialogView.findViewById<Button>(R.id.action_cancel)
        action_cancel.setOnClickListener { hideBottomSheetDialog() }
        bottomSheetDialog!!.setContentView(dialogView)
        bottomSheetDialog!!.show()
        HelperUnit.setBottomSheetBehavior(
            bottomSheetDialog,
            dialogView,
            BottomSheetBehavior.STATE_EXPANDED
        )
    }

    private fun toast_login(userName: String, passWord: String) {
        try {
            val decrypted_userName = mahEncryptor!!.decode(userName)
            val decrypted_userPW = mahEncryptor!!.decode(passWord)
            val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val unCopy = object : BroadcastReceiver() {
                override fun onReceive(context: Context, intent: Intent) {
                    val clip = ClipData.newPlainText("text", decrypted_userName)
                    clipboard.setPrimaryClip(clip)
                    NinjaToast.show(context, R.string.toast_copy_successful)
                }
            }
            val pwCopy = object : BroadcastReceiver() {
                override fun onReceive(context: Context, intent: Intent) {
                    val clip = ClipData.newPlainText("text", decrypted_userPW)
                    clipboard.setPrimaryClip(clip)
                    NinjaToast.show(context, R.string.toast_copy_successful)
                }
            }
            val intentFilter = IntentFilter("unCopy")
            registerReceiver(unCopy, intentFilter)
            val copy = Intent("unCopy")
            val copyUN =
                PendingIntent.getBroadcast(context, 0, copy, PendingIntent.FLAG_CANCEL_CURRENT)
            val intentFilter2 = IntentFilter("pwCopy")
            registerReceiver(pwCopy, intentFilter2)
            val copy2 = Intent("pwCopy")
            val copyPW =
                PendingIntent.getBroadcast(context, 1, copy2, PendingIntent.FLAG_CANCEL_CURRENT)
            val builder: NotificationCompat.Builder
            val mNotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            builder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val CHANNEL_ID = "browser_not"// The id of the channel.
                val name = getString(R.string.app_name)// The user-visible name of the channel.
                val mChannel =
                    NotificationChannel(CHANNEL_ID, name, NotificationManager.IMPORTANCE_HIGH)
                mNotificationManager.createNotificationChannel(mChannel)
                NotificationCompat.Builder(context!!, CHANNEL_ID)
            } else {

                NotificationCompat.Builder(context)
            }
            val action_UN = NotificationCompat.Action.Builder(
                R.drawable.icon_earth,
                getString(R.string.toast_titleConfirm_pasteUN),
                copyUN
            ).build()
            val action_PW = NotificationCompat.Action.Builder(
                R.drawable.icon_earth,
                getString(R.string.toast_titleConfirm_pastePW),
                copyPW
            ).build()
            val n = builder
                .setCategory(Notification.CATEGORY_MESSAGE)
                .setSmallIcon(R.drawable.ic_notification_ninja)
                .setContentTitle(getString(R.string.app_name))
                .setContentText(getString(R.string.toast_titleConfirm_paste))
                .setColor(resources.getColor(R.color.color_red))
                .setAutoCancel(true)
                .setPriority(Notification.PRIORITY_HIGH)
                .setVibrate(LongArray(0))
                .addAction(action_UN)
                .addAction(action_PW)
                .build()
            val notificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            if (decrypted_userName.isNotEmpty() || decrypted_userPW.isNotEmpty()) {
                notificationManager.notify(0, n)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            NinjaToast.show(context, R.string.toast_error)
        }

    }

    @Synchronized
    private fun addAlbum(title: String?, url: String?, foreground: Boolean) {
        ninjaWebView = NinjaWebView(context)
        ninjaWebView!!.browserController = this
        ninjaWebView!!.albumTitle = title
        ViewUnit.bound(context, ninjaWebView!!)
        val albumView = ninjaWebView!!.albumView
        if (currentAlbumController != null) {
            val index = BrowserContainer.indexOf(currentAlbumController!!) + 1
            BrowserContainer.add(ninjaWebView!!, index)
            tab_container!!.addView(
                albumView,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        } else {
            BrowserContainer.add(ninjaWebView!!)
            tab_container!!.addView(
                albumView,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }
        if (!foreground) {
            ViewUnit.bound(context, ninjaWebView!!)
            ninjaWebView!!.loadUrl(url)
            ninjaWebView!!.deactivate()
            return
        } else {
            showOmnibox()
            showAlbum(ninjaWebView!!)
        }
        if (url != null && !url.isEmpty()) {
            ninjaWebView!!.loadUrl(url)
        }
    }

    @Synchronized
    private fun updateAlbum(url: String?) {
        (currentAlbumController as NinjaWebView).loadUrl(url)
        updateOmnibox()
    }

    private fun closeTabConfirmation(okAction: Runnable) {
        if (!sp!!.getBoolean("sp_close_tab_confirm", false)) {
            okAction.run()
        } else {
            bottomSheetDialog = BottomSheetDialog(context!!)
            val dialogView = View.inflate(context, R.layout.dialog_action, null)
            val textView = dialogView.findViewById<TextView>(R.id.dialog_text)
            textView.setText(R.string.toast_close_tab)
            val action_ok = dialogView.findViewById<Button>(R.id.action_ok)
            action_ok.setOnClickListener {
                okAction.run()
                hideBottomSheetDialog()
            }
            val action_cancel = dialogView.findViewById<Button>(R.id.action_cancel)
            action_cancel.setOnClickListener { hideBottomSheetDialog() }
            bottomSheetDialog!!.setContentView(dialogView)
            bottomSheetDialog!!.show()
            HelperUnit.setBottomSheetBehavior(
                bottomSheetDialog,
                dialogView,
                BottomSheetBehavior.STATE_EXPANDED
            )
        }
    }

    @Synchronized
    override fun removeAlbum(albumController: AlbumController?) {
        if (BrowserContainer.size() <= 1) {
            if (!sp!!.getBoolean("sp_reopenLastTab", false)) {
                doubleTapsQuit()
            } else {
                updateAlbum(sp!!.getString("favoriteURL", "https://github.com/scoute-dich/browser"))
                hideOverview()
            }
        } else {
            closeTabConfirmation(Runnable {
                tab_container!!.removeView(albumController!!.albumView)
                var index = BrowserContainer.indexOf(albumController)
                BrowserContainer.remove(albumController)
                if (index >= BrowserContainer.size()) {
                    index = BrowserContainer.size() - 1
                }
                showAlbum(BrowserContainer.get(index))
            })
        }
    }

    private fun updateOmnibox() {
        if (ninjaWebView === currentAlbumController) {
            omniboxTitle!!.text = ninjaWebView!!.title
        } else {
            ninjaWebView = currentAlbumController as NinjaWebView?
            updateProgress(ninjaWebView!!.progress)
        }
    }

    private fun scrollChange() {
        if (Objects.requireNonNull(sp!!.getBoolean("hideToolbar", true))) {
            ninjaWebView!!.setOnScrollChangeListener { scrollY, oldScrollY ->
                val height =
                    floor(x = (ninjaWebView!!.contentHeight * ninjaWebView!!.resources.displayMetrics.density).toDouble())
                        .toInt()
                val webViewHeight = ninjaWebView!!.height
                val cutoff =
                    height - webViewHeight + 112 * resources.displayMetrics.density.roundToInt()
                when {
                    abs(scrollY - oldScrollY) > 50 -> {
                    }
                    scrollY in (oldScrollY + 1)..cutoff -> hideOmnibox()
                    scrollY < oldScrollY -> showOmnibox()
                }
            }
        }
    }

    @Synchronized
    override fun updateProgress(progress: Int) {
        progressBar!!.progress = progress
        updateOmnibox()
        updateAutoComplete()
        scrollChange()
        HelperUnit.initRendering(contentFrame)
        ninjaWebView!!.requestFocus()
        if (progress < BrowserUnit.PROGRESS_MAX) {
            progressBar!!.visibility = View.VISIBLE
        } else {
            progressBar!!.visibility = View.GONE
        }
    }

    override fun showFileChooser(filePathCallback: ValueCallback<Array<Uri>>) {
        if (mFilePathCallback != null) {
            mFilePathCallback!!.onReceiveValue(null)
        }
        mFilePathCallback = filePathCallback
        val contentSelectionIntent = Intent(Intent.ACTION_GET_CONTENT)
        contentSelectionIntent.addCategory(Intent.CATEGORY_OPENABLE)
        contentSelectionIntent.type = "*/*"
        val chooserIntent = Intent(Intent.ACTION_CHOOSER)
        chooserIntent.putExtra(Intent.EXTRA_INTENT, contentSelectionIntent)
        startActivityForResult(chooserIntent, INPUT_FILE_REQUEST_CODE)
    }

    override fun onShowCustomView(view: View?, callback: WebChromeClient.CustomViewCallback?) {
        if (view == null) {
            return
        }
        if (customView != null && callback != null) {
            callback.onCustomViewHidden()
            return
        }
        customView = view
        originalOrientation = requestedOrientation
        fullscreenHolder = FrameLayout(context!!)
        fullscreenHolder!!.addView(
            customView,
            FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
        )
        val decorView = window.decorView as FrameLayout
        decorView.addView(
            fullscreenHolder,
            FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
        )
        customView!!.keepScreenOn = true
        (currentAlbumController as View).visibility = View.GONE
        setCustomFullscreen(true)
        if (view is FrameLayout) {
            if (view.focusedChild is VideoView) {
                videoView = view.focusedChild as VideoView
                videoView!!.setOnErrorListener(VideoCompletionListener())
                videoView!!.setOnCompletionListener(VideoCompletionListener())
            }
        }
        customViewCallback = callback
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
    }

    override fun onHideCustomView(): Boolean {
        if (customView == null || customViewCallback == null || currentAlbumController == null) {
            return false
        }
        val decorView = window.decorView as FrameLayout
        decorView.removeView(fullscreenHolder)
        customView!!.keepScreenOn = false
        (currentAlbumController as View).visibility = View.VISIBLE
        setCustomFullscreen(false)
        fullscreenHolder = null
        customView = null
        if (videoView != null) {
            videoView!!.setOnErrorListener(null)
            videoView!!.setOnCompletionListener(null)
            videoView = null
        }
        requestedOrientation = originalOrientation
        return true
    }

    override fun onLongPress(url: String?) {}

    private fun doubleTapsQuit() {
        if (!sp!!.getBoolean("sp_close_browser_confirm", true)) {
            finish()
        } else {
            bottomSheetDialog = BottomSheetDialog(context!!)
            val dialogView = View.inflate(context, R.layout.dialog_action, null)
            val textView = dialogView.findViewById<TextView>(R.id.dialog_text)
            textView.setText(R.string.toast_quit)
            val action_ok = dialogView.findViewById<Button>(R.id.action_ok)
            action_ok.setOnClickListener { finish() }
            val action_cancel = dialogView.findViewById<Button>(R.id.action_cancel)
            action_cancel.setOnClickListener { hideBottomSheetDialog() }
            bottomSheetDialog!!.setContentView(dialogView)
            bottomSheetDialog!!.show()
            HelperUnit.setBottomSheetBehavior(
                bottomSheetDialog,
                dialogView,
                BottomSheetBehavior.STATE_EXPANDED
            )
        }
    }

    @SuppressLint("RestrictedApi")
    private fun showOmnibox() {
        if (!searchOnSite) {
            fab_imageButtonNav!!.visibility = View.GONE
            searchPanel!!.visibility = View.GONE
            omnibox!!.visibility = View.VISIBLE
            omniboxTitle!!.visibility = View.VISIBLE
            appBar!!.visibility = View.VISIBLE
            hideKeyboard(activity!!)
        }
    }

    @SuppressLint("RestrictedApi")
    private fun hideOmnibox() {
        if (!searchOnSite) {
            fab_imageButtonNav!!.visibility = View.VISIBLE
            searchPanel!!.visibility = View.GONE
            omnibox!!.visibility = View.GONE
            omniboxTitle!!.visibility = View.GONE
            appBar!!.visibility = View.GONE
        }
    }

    private fun hideSearchPanel() {
        searchOnSite = false
        searchBox!!.setText("")
        showOmnibox()
    }

    @SuppressLint("RestrictedApi")
    private fun showSearchPanel() {
        searchOnSite = true
        fab_imageButtonNav!!.visibility = View.GONE
        omnibox!!.visibility = View.GONE
        searchPanel!!.visibility = View.VISIBLE
        omniboxTitle!!.visibility = View.GONE
        appBar!!.visibility = View.VISIBLE
    }

    private fun showOverflow(): Boolean {
        showEchelonFragment(true)
        return true
    }

    private fun show_contextMenu_list(
        title: String,
        url: String
    ) {
        bottomSheetDialog = BottomSheetDialog(context!!)
        val dialogView = View.inflate(context, R.layout.dialog_menu_context_list, null)
        val db = BookmarkList(context)
        db.open()
        val contextList_edit = dialogView.findViewById<LinearLayout>(R.id.menu_contextList_edit)
        val contextList_fav = dialogView.findViewById<LinearLayout>(R.id.menu_contextList_fav)
        val contextList_sc = dialogView.findViewById<LinearLayout>(R.id.menu_contextLink_sc)
        val contextList_newTab = dialogView.findViewById<LinearLayout>(R.id.menu_contextList_newTab)
        val contextList_newTabOpen =
            dialogView.findViewById<LinearLayout>(R.id.menu_contextList_newTabOpen)
        if (overViewTab == getString(R.string.album_title_history)) {
            contextList_edit.visibility = View.GONE
        } else {
            contextList_edit.visibility = View.VISIBLE
        }
        contextList_fav.setOnClickListener {
            hideBottomSheetDialog()
            HelperUnit.setFavorite(context, url)
        }
        contextList_sc.setOnClickListener {
            hideBottomSheetDialog()
            HelperUnit.createShortcut(context, title, url)
        }
        contextList_newTab.setOnClickListener {
            addAlbum(getString(R.string.app_name), url, false)
            NinjaToast.show(context, getString(R.string.toast_new_tab_successful))
            hideBottomSheetDialog()
        }
        contextList_newTabOpen.setOnClickListener {
            addAlbum(getString(R.string.app_name), url, true)
            hideBottomSheetDialog()
            hideOverview()
        }
        bottomSheetDialog!!.setContentView(dialogView)
        bottomSheetDialog!!.show()
        HelperUnit.setBottomSheetBehavior(
            bottomSheetDialog,
            dialogView,
            BottomSheetBehavior.STATE_EXPANDED
        )
    }

    private fun setCustomFullscreen(fullscreen: Boolean) {
        val decorView = window.decorView
        if (fullscreen) {
            decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // hide nav bar
                    or View.SYSTEM_UI_FLAG_FULLSCREEN // hide status bar
                    or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)
        } else {
            decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_VISIBLE
        }
    }

    private fun nextAlbumController(next: Boolean): AlbumController? {
        if (BrowserContainer.size() <= 1) {
            return currentAlbumController
        }
        val list = BrowserContainer.list()
        var index = list.indexOf(currentAlbumController)
        if (next) {
            index++
            if (index >= list.size) {
                index = 0
            }
        } else {
            index--
            if (index < 0) {
                index = list.size - 1
            }
        }
        return list[index]
    }

    override fun showEchelonFragment(isShow: Boolean) {
        fragmentTransaction = fragmentManager.beginTransaction()
        isShowEchelonFragment = if (isShow) {
            val list = BrowserContainer.list()
            echelonFragment =
                MainAllPaginationFragment(this, list)//梯形布局
            fragmentTransaction.replace(R.id.constraint_layout, echelonFragment, "HOME")
            fragmentTransaction.commit()
            true
        } else {
            fragmentTransaction.remove(echelonFragment)
            fragmentTransaction.commit()
            false
        }
    }

    fun addAlbum() {
        addAlbum(
            getString(R.string.app_name),
            sp?.getString("favoriteURL", "https://github.com/tenSunFree"),
            true
        )
    }

    override fun updateQuantity() {
        val list = BrowserContainer.list()
        text_view_quantity.text = list.size.toString()
    }
}