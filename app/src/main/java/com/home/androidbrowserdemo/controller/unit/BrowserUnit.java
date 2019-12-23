package com.home.androidbrowserdemo.controller.unit;

import android.Manifest;
import android.app.Activity;
import android.app.DownloadManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.URLUtil;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.preference.PreferenceManager;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.home.androidbrowserdemo.R;
import com.home.androidbrowserdemo.utils.NinjaToast;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Locale;
import java.util.Objects;
import java.util.regex.Pattern;

public class BrowserUnit {

    public static final int PROGRESS_MAX = 100;
    private static final String SEARCH_ENGINE_GOOGLE = "https://www.google.com/search?q=";
    private static final String SEARCH_ENGINE_DUCKDUCKGO = "https://duckduckgo.com/?q=";
    private static final String SEARCH_ENGINE_STARTPAGE = "https://startpage.com/do/search?query=";
    private static final String SEARCH_ENGINE_BING = "http://www.bing.com/search?q=";
    private static final String SEARCH_ENGINE_BAIDU = "https://www.baidu.com/s?wd=";
    private static final String SEARCH_ENGINE_QWANT = "https://www.qwant.com/?q=";
    private static final String SEARCH_ENGINE_STARTPAGE_DE = "https://startpage.com/do/search?lui=deu&language=deutsch&query=";
    private static final String SEARCH_ENGINE_SEARX = "https://searx.me/?q=";
    private static final String URL_ENCODING = "UTF-8";
    private static final String URL_ABOUT_BLANK = "about:blank";
    public static final String URL_SCHEME_ABOUT = "about:";
    public static final String URL_SCHEME_MAIL_TO = "mailto:";
    private static final String URL_SCHEME_FILE = "file://";
    private static final String URL_SCHEME_HTTP = "https://";
    public static final String URL_SCHEME_INTENT = "intent://";
    private static final String URL_PREFIX_GOOGLE_PLAY = "www.google.com/url?q=";
    private static final String URL_SUFFIX_GOOGLE_PLAY = "&sa";
    private static final String URL_PREFIX_GOOGLE_PLUS = "plus.url.google.com/url?q=";
    private static final String URL_SUFFIX_GOOGLE_PLUS = "&rct";

    private static boolean isURL(String url) {
        if (url == null) {
            return false;
        }
        url = url.toLowerCase(Locale.getDefault());
        if (url.startsWith(URL_ABOUT_BLANK)
                || url.startsWith(URL_SCHEME_MAIL_TO)
                || url.startsWith(URL_SCHEME_FILE)) {
            return true;
        }
        String regex = "^((ftp|http|https|intent)?://)"                      // support scheme
                + "?(([0-9a-z_!~*'().&=+$%-]+: )?[0-9a-z_!~*'().&=+$%-]+@)?" // ftp的user@
                + "(([0-9]{1,3}\\.){3}[0-9]{1,3}"                            // IP形式的URL -> 199.194.52.184
                + "|"                                                        // 允许IP和DOMAIN（域名）
                + "(.)*"                                                     // 域名 -> www.
                // + "([0-9a-z_!~*'()-]+\\.)*"                               // 域名 -> www.
                + "([0-9a-z][0-9a-z-]{0,61})?[0-9a-z]\\."                    // 二级域名
                + "[a-z]{2,6})"                                              // first level domain -> .com or .museum
                + "(:[0-9]{1,4})?"                                           // 端口 -> :80
                + "((/?)|"                                                   // a slash isn't required if there is no file name
                + "(/[0-9a-z_!~*'().;?:@&=+$,%#-]+)+/?)$";
        Pattern pattern = Pattern.compile(regex);
        return pattern.matcher(url).matches();
    }

    public static String queryWrapper(Context context, String query) {
        // Use prefix and suffix to process some special links
        String temp = query.toLowerCase(Locale.getDefault());
        if (temp.contains(URL_PREFIX_GOOGLE_PLAY) && temp.contains(URL_SUFFIX_GOOGLE_PLAY)) {
            int start = temp.indexOf(URL_PREFIX_GOOGLE_PLAY) + URL_PREFIX_GOOGLE_PLAY.length();
            int end = temp.indexOf(URL_SUFFIX_GOOGLE_PLAY);
            query = query.substring(start, end);
        } else if (temp.contains(URL_PREFIX_GOOGLE_PLUS) && temp.contains(URL_SUFFIX_GOOGLE_PLUS)) {
            int start = temp.indexOf(URL_PREFIX_GOOGLE_PLUS) + URL_PREFIX_GOOGLE_PLUS.length();
            int end = temp.indexOf(URL_SUFFIX_GOOGLE_PLUS);
            query = query.substring(start, end);
        }
        if (isURL(query)) {
            if (query.startsWith(URL_SCHEME_ABOUT) || query.startsWith(URL_SCHEME_MAIL_TO)) {
                return query;
            }
            if (!query.contains("://")) {
                query = URL_SCHEME_HTTP + query;
            }
            return query;
        }
        try {
            query = URLEncoder.encode(query, URL_ENCODING);
        } catch (UnsupportedEncodingException u) {
            Log.w("browser", "Unsupported Encoding Exception");
        }
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        String custom = sp.getString(context.getString(R.string.sp_search_engine_custom), SEARCH_ENGINE_STARTPAGE);
        final int i = Integer.valueOf(Objects.requireNonNull(sp.getString(context.getString(R.string.sp_search_engine), "9")));
        switch (i) {
            case 0:
                return SEARCH_ENGINE_STARTPAGE + query;
            case 1:
                return SEARCH_ENGINE_STARTPAGE_DE + query;
            case 2:
                return SEARCH_ENGINE_BAIDU + query;
            case 3:
                return SEARCH_ENGINE_BING + query;
            case 4:
                return SEARCH_ENGINE_DUCKDUCKGO + query;
            case 6:
                return SEARCH_ENGINE_SEARX + query;
            case 7:
                return SEARCH_ENGINE_QWANT + query;
            case 8:
                return custom + query;
            case 9:
            default:
                return SEARCH_ENGINE_GOOGLE + query;
        }
    }

    public static void download(final Context context, final String url, final String contentDisposition, final String mimeType) {
        String text = context.getString(R.string.dialog_title_download) + " - " + URLUtil.guessFileName(url, contentDisposition, mimeType);
        final BottomSheetDialog dialog = new BottomSheetDialog(context);
        View dialogView = View.inflate(context, R.layout.dialog_action, null);
        TextView textView = dialogView.findViewById(R.id.dialog_text);
        textView.setText(text);
        Button action_ok = dialogView.findViewById(R.id.action_ok);
        action_ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
                String filename = URLUtil.guessFileName(url, contentDisposition, mimeType); // Maybe unexpected filename.
                CookieManager cookieManager = CookieManager.getInstance();
                String cookie = cookieManager.getCookie(url);
                request.addRequestHeader("Cookie", cookie);
                request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                request.setTitle(filename);
                request.setMimeType(mimeType);
                request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, filename);
                DownloadManager manager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
                assert manager != null;
                if (Build.VERSION.SDK_INT >= 23) {
                    int hasWRITE_EXTERNAL_STORAGE = context.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE);
                    if (hasWRITE_EXTERNAL_STORAGE != PackageManager.PERMISSION_GRANTED) {
                        Activity activity = (Activity) context;
                        HelperUnit.grantPermissionsStorage(activity);
                    } else {
                        manager.enqueue(request);
                        try {
                            NinjaToast.INSTANCE.show(context, R.string.toast_start_download);
                        } catch (Exception e) {
                            Toast.makeText(context, R.string.toast_start_download, Toast.LENGTH_SHORT).show();
                        }
                    }
                } else {
                    manager.enqueue(request);
                    try {
                        NinjaToast.INSTANCE.show(context, R.string.toast_start_download);
                    } catch (Exception e) {
                        Toast.makeText(context, R.string.toast_start_download, Toast.LENGTH_SHORT).show();
                    }
                }
                dialog.cancel();
            }
        });
        Button action_cancel = dialogView.findViewById(R.id.action_cancel);
        action_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.cancel();
            }
        });
        dialog.setContentView(dialogView);
        dialog.show();
        HelperUnit.setBottomSheetBehavior(dialog, dialogView, BottomSheetBehavior.STATE_EXPANDED);
    }
}
