package com.home.androidbrowserdemo.view.components;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.WebSettings;
import android.webkit.WebView;

import androidx.preference.PreferenceManager;

import com.home.androidbrowserdemo.R;
import com.home.androidbrowserdemo.controller.AlbumController;
import com.home.androidbrowserdemo.controller.BrowserController;
import com.home.androidbrowserdemo.controller.client.NinjaWebChromeClient;
import com.home.androidbrowserdemo.controller.client.NinjaWebViewClient;
import com.home.androidbrowserdemo.controller.handler.NinjaClickHandler;
import com.home.androidbrowserdemo.controller.listener.NinjaDownloadListener;
import com.home.androidbrowserdemo.controller.listener.NinjaGestureListener;
import com.home.androidbrowserdemo.controller.unit.BrowserUnit;
import com.home.androidbrowserdemo.controller.unit.ViewUnit;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Objects;

public class NinjaWebView extends WebView implements AlbumController {

    private OnScrollChangeListener onScrollChangeListener;

    public NinjaWebView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public NinjaWebView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onScrollChanged(int l, int t, int old_l, int old_t) {
        super.onScrollChanged(l, t, old_l, old_t);
        if (onScrollChangeListener != null) {
            onScrollChangeListener.onScrollChange(t, old_t);
        }
    }

    public void setOnScrollChangeListener(OnScrollChangeListener onScrollChangeListener) {
        this.onScrollChangeListener = onScrollChangeListener;
    }

    public interface OnScrollChangeListener {
        /**
         * Called when the scroll position of a view changes.
         *
         * @param scrollY    Current vertical scroll origin.
         * @param oldScrollY Previous vertical scroll origin.
         */
        void onScrollChange(int scrollY, int oldScrollY);
    }

    private Context context;
    private int dimen144dp;
    private int dimen108dp;

    private Album album;
    private NinjaWebViewClient webViewClient;
    private NinjaWebChromeClient webChromeClient;
    private NinjaDownloadListener downloadListener;
    private NinjaClickHandler clickHandler;
    private GestureDetector gestureDetector;
    private SharedPreferences sp;
    private WebSettings webSettings;
    private boolean foreground;
    public boolean isForeground() {
        return foreground;
    }
    private BrowserController browserController = null;
    public BrowserController getBrowserController() {
        return browserController;
    }
    public void setBrowserController(BrowserController browserController) {
        this.browserController = browserController;
        this.album.setBrowserController(browserController);
    }
    public NinjaWebView(Context context) {
        super(context); // Cannot create a dialog, the WebView context is not an activity
        this.context = context;
        this.dimen144dp = getResources().getDimensionPixelSize(R.dimen.layout_width_290dp);
        this.dimen108dp = getResources().getDimensionPixelSize(R.dimen.layout_height_420dp);
        this.foreground = false;
        this.album = new Album(this.context, this, this.browserController);
        this.webViewClient = new NinjaWebViewClient(this);
        this.webChromeClient = new NinjaWebChromeClient(this);
        this.downloadListener = new NinjaDownloadListener(this.context);
        this.clickHandler = new NinjaClickHandler(this);
        this.gestureDetector = new GestureDetector(context, new NinjaGestureListener(this));
        initWebView();
        initWebSettings();
        initPreferences();
        initAlbum();
    }

    private synchronized void initWebView() {
        setWebViewClient(webViewClient);
        setWebChromeClient(webChromeClient);
        setDownloadListener(downloadListener);
        setOnTouchListener(new OnTouchListener() {
            @SuppressLint("ClickableViewAccessibility")
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                gestureDetector.onTouchEvent(motionEvent);
                return false;
            }
        });
    }

    @TargetApi(Build.VERSION_CODES.O)
    private synchronized void initWebSettings() {
        webSettings = getSettings();
        webSettings.setBuiltInZoomControls(true);
        webSettings.setDisplayZoomControls(false);
        webSettings.setSupportZoom(true);
        webSettings.setSupportMultipleWindows(true);
        webSettings.setLoadWithOverviewMode(true);
        webSettings.setUseWideViewPort(true);
        if (Build.VERSION.SDK_INT >= 26) {
            webSettings.setSafeBrowsingEnabled(true);
        }
    }

    public synchronized void initPreferences() {
        sp = PreferenceManager.getDefaultSharedPreferences(context);
        String userAgent = sp.getString("userAgent", "");
        webSettings = getSettings();
        if (!userAgent.isEmpty()) {
            String MyUA = "\"" + userAgent + "\"";
            webSettings.setUserAgentString(MyUA);
        }
        webViewClient.enableAdBlock(sp.getBoolean(context.getString(R.string.sp_ad_block), true));
        webSettings = getSettings();
        webSettings.setTextZoom(Integer.parseInt(Objects.requireNonNull(sp.getString("sp_fontSize", "100"))));
        webSettings.setAllowFileAccessFromFileURLs(sp.getBoolean(("sp_remote"), false));
        webSettings.setAllowUniversalAccessFromFileURLs(sp.getBoolean(("sp_remote"), false));
        webSettings.setDomStorageEnabled(sp.getBoolean(("sp_remote"), false));
        webSettings.setBlockNetworkImage(!sp.getBoolean(context.getString(R.string.sp_images), true));
        webSettings.setJavaScriptEnabled(sp.getBoolean(context.getString(R.string.sp_javascript), true));
        webSettings.setJavaScriptCanOpenWindowsAutomatically(sp.getBoolean(context.getString(R.string.sp_javascript), true));
        webSettings.setGeolocationEnabled(sp.getBoolean(context.getString(R.string.sp_location), false));
        CookieManager manager = CookieManager.getInstance();
        manager.setAcceptCookie(sp.getBoolean(context.getString(R.string.sp_cookies), true));
    }

    private synchronized void initAlbum() {
        album.setAlbumCover(null);
        album.setAlbumTitle(context.getString(R.string.app_name));
        album.setBrowserController(browserController);
    }

    public synchronized HashMap<String, String> getRequestHeaders() {
        HashMap<String, String> requestHeaders = new HashMap<>();
        requestHeaders.put("DNT", "1");
        if (sp.getBoolean(context.getString(R.string.sp_savedata), false)) {
            requestHeaders.put("Save-Data", "on");
        }
        return requestHeaders;
    }

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    public synchronized void loadUrl(String url) {
        super.loadUrl(BrowserUnit.queryWrapper(context, url.trim()), getRequestHeaders());
    }

    @NotNull
    @Override
    public View getAlbumView() {
        return album.getAlbumView();
    }

    @Override
    public void setAlbumCover(@NotNull Bitmap bitmap) {
        album.setAlbumCover(bitmap);
        browserController.updateQuantity();
    }

    @Override
    public String getAlbumTitle() {
        return album.getAlbumTitle();
    }

    @Override
    public void setAlbumTitle(String title) {
        album.setAlbumTitle(title);
    }

    @Override
    public synchronized void activate() {
        requestFocus();
        foreground = true;
        album.activate();
    }

    @Override
    public synchronized void deactivate() {
        clearFocus();
        foreground = false;
        album.deactivate();
    }

    public synchronized void update(int progress) {
        if (foreground) {
            browserController.updateProgress(progress);
        }
        if (isLoadFinish()) {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    setAlbumCover(ViewUnit.capture(NinjaWebView.this, dimen144dp, dimen108dp, Bitmap.Config.RGB_565));
                }
            }, 250);
            if (prepareRecord()) {
                browserController.updateAutoComplete();
            }
        }
    }

    public synchronized void update(String title) {
        album.setAlbumTitle(title);
    }

    @Override
    public synchronized void destroy() {
        stopLoading();
        onPause();
        clearHistory();
        setVisibility(GONE);
        removeAllViews();
        super.destroy();
    }

    public boolean isLoadFinish() {
        return getProgress() >= BrowserUnit.PROGRESS_MAX;
    }

    public void onLongPress() {
        Message click = clickHandler.obtainMessage();
        click.setTarget(clickHandler);
        requestFocusNodeHref(click);
    }

    private boolean prepareRecord() {
        String title = getTitle();
        String url = getUrl();
        return !(title == null
                || title.isEmpty()
                || url == null
                || url.isEmpty()
                || url.startsWith(BrowserUnit.URL_SCHEME_ABOUT)
                || url.startsWith(BrowserUnit.URL_SCHEME_MAIL_TO)
                || url.startsWith(BrowserUnit.URL_SCHEME_INTENT));
    }
}
