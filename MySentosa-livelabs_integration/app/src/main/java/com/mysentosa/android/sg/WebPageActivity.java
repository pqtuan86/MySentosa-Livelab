package com.mysentosa.android.sg;

import android.annotation.TargetApi;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.mysentosa.android.sg.utils.HttpHelper;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class WebPageActivity extends BaseActivity {

    public static final String TAG = WebPageActivity.class.getSimpleName();
    public static final String PAGE_TYPE = "PAGE_TYPE";

    public static final int PAGE_TYPE_GETTING = 1;
    public static final int PAGE_TYPE_ISLANDER = 2;
    public static final int PAGE_TYPE_ISLANDER_REGISTER = 3;
    public static final int PAGE_TYPE_ISLANDER_RENEW = 4;
    public static final int PAGE_TERMS_CONDITIONS = 5;
    public static final int PAGE_TYPE_AROUND = 6;

    private int type;
    private String url;

    private ValueCallback<Uri> mUploadMessage;
    private final static int FILECHOOSER_RESULTCODE = 1;

    @InjectView(R.id.header_title) TextView headerTitle;
    @InjectView(R.id.container) FrameLayout frameContainer;
    @InjectView(R.id.webview) WebView webView;
    @InjectView(R.id.pb_loading) ProgressBar progressBar;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.web_page_screen);
        ButterKnife.inject(this);

        Intent intent = this.getIntent();
        if (intent != null) {
            type = intent.getIntExtra(PAGE_TYPE, 0);
        }
        initializeViews();
    }


    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private void initializeViews() {
        webView.setVisibility(View.INVISIBLE);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setAllowFileAccess(true);
        webView.getSettings().setBuiltInZoomControls(true);

        webView.getSettings().setLoadWithOverviewMode(true);
        webView.getSettings().setUseWideViewPort(true);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            webView.getSettings().setDisplayZoomControls(false);
        }

        webView.setVerticalScrollBarEnabled(false);
        webView.setWebViewClient(new MyWebViewClient());

        webView.setWebChromeClient(new WebChromeClient() {
            //The undocumented magic method override
            //Eclipse will swear at you if you try to put @Override here

            // For Android < 3.0
            public void openFileChooser(ValueCallback<Uri> uploadMsg) {
                mUploadMessage = uploadMsg;
                Intent i = new Intent(Intent.ACTION_GET_CONTENT);
                i.addCategory(Intent.CATEGORY_OPENABLE);
                i.setType("image/*");
                WebPageActivity.this.startActivityForResult(Intent.createChooser(i, "File Chooser"), FILECHOOSER_RESULTCODE);

            }

            // For Android 3.0+
            public void openFileChooser(ValueCallback uploadMsg, String acceptType) {
                mUploadMessage = uploadMsg;
                Intent i = new Intent(Intent.ACTION_GET_CONTENT);
                i.addCategory(Intent.CATEGORY_OPENABLE);
                i.setType("*/*");
                WebPageActivity.this.startActivityForResult(Intent.createChooser(i, "File Browser"), FILECHOOSER_RESULTCODE);
            }

            //For Android 4.1
            public void openFileChooser(ValueCallback<Uri> uploadMsg, String acceptType, String capture) {
                mUploadMessage = uploadMsg;
                Intent i = new Intent(Intent.ACTION_GET_CONTENT);
                i.addCategory(Intent.CATEGORY_OPENABLE);
                i.setType("image/*");
                WebPageActivity.this.startActivityForResult(Intent.createChooser(i, "File Chooser"), WebPageActivity.FILECHOOSER_RESULTCODE);
            }
        });

        float density = this.getResources().getDisplayMetrics().density;

        switch (type) {
            case PAGE_TERMS_CONDITIONS:
                headerTitle.setText(R.string.terms_conditions);
                url = getString(R.string.link_terms_conditions);
                frameContainer.setPadding(0, (int) (30 * density), 0, 0);
                break;

            case PAGE_TYPE_GETTING:
                headerTitle.setText(R.string.getting_to_sentosa);
                url = HttpHelper.BASE_HOST + "/Content/HTML/getting.html";
                frameContainer.setPadding(0, (int) (30 * density), 0, 0);
                break;

            case PAGE_TYPE_AROUND:
                headerTitle.setText(R.string.getting_around_sentosa);
                url = getString(R.string.link_getting_around_sentosa);
                frameContainer.setPadding(0, (int) (15 * density), 0, 0);
                webView.setInitialScale(100);
                break;

            case PAGE_TYPE_ISLANDER:
                headerTitle.setText(R.string.sentosa_island_privileges);
                url = HttpHelper.BASE_HOST + "/Content/HTML/islander.html";
                frameContainer.setPadding(0, (int) (10 * density), 0, 0);
                break;

            case PAGE_TYPE_ISLANDER_REGISTER:
                headerTitle.setText(R.string.register);
                url = getString(R.string.link_islander_register);
                frameContainer.setPadding(0, (int) (15 * density), 0, 0);
                webView.setInitialScale(100);
                break;

            case PAGE_TYPE_ISLANDER_RENEW:
                headerTitle.setText(R.string.renew_membership);
                url = getString(R.string.link_islander_renew);
                frameContainer.setPadding(0, (int) (23 * density), 0, 0);
                break;

            default:
                finish();
                break;
        }

        webView.loadUrl(url);
    }

    private class MyWebViewClient extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            view.loadUrl(url);
            return true;
        }

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            super.onPageStarted(view, url, favicon);
            progressBar.setVisibility(View.VISIBLE);
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
            view.clearCache(true);
            progressBar.setVisibility(View.GONE);
            webView.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (requestCode == FILECHOOSER_RESULTCODE) {
            if (null == mUploadMessage)
                return;
            Uri result = intent == null || resultCode != RESULT_OK ? null : intent.getData();
            mUploadMessage.onReceiveValue(result);
            mUploadMessage = null;
        }
    }

    @Override
    public void onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack();
        } else {
            super.onBackPressed();
        }
    }
}