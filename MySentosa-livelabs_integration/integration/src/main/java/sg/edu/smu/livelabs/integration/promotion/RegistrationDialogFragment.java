package sg.edu.smu.livelabs.integration.promotion;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import java.io.File;
import java.io.IOException;

import sg.edu.smu.livelabs.integration.LiveLabsApi;
import sg.edu.smu.livelabs.integration.R;
import sg.edu.smu.livelabs.integration.model.Promotion;

/**
 * Created by smu on 8/12/15.
 */
public class RegistrationDialogFragment extends DialogFragment {
    public static final String TAG = "LIVELABS";
    private static final String PROMOTION_KEY = "promotion";
    private Promotion promotion;
    private WebView regWebView;
    public static RegistrationDialogFragment newInstance(Promotion promotion) {
        RegistrationDialogFragment f = new RegistrationDialogFragment();
        Bundle args = new Bundle();
        args.putSerializable(PROMOTION_KEY, promotion);
        f.setArguments(args);
        return f;
    }

    public RegistrationDialogFragment() {
        setStyle(STYLE_NORMAL, android.R.style.Theme_Holo_Light_DarkActionBar);
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        promotion = (Promotion) getArguments().getSerializable(PROMOTION_KEY);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View v = inflater.inflate(R.layout.registration_dialogfragment, container, false);
        getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        regWebView = (WebView) v.findViewById(R.id.webview);
        return v;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        String url = String.format("%s?mac=%s&campaignId=%s&code=%s",
                promotion.getRegUrl(),
                LiveLabsApi.getInstance().macAddressSHA1,
                promotion.getCampaignId(),
                promotion.getRegDiscountCode());
        WebSettings ws = regWebView.getSettings();
        ws.setJavaScriptEnabled(true);
        ws.setCacheMode(WebSettings.LOAD_NO_CACHE);
        ws.setAllowContentAccess(true);
        ws.setAllowFileAccess(true);
        ws.setLoadsImagesAutomatically(true);
        ws.setDatabaseEnabled(true);
        ws.setJavaScriptCanOpenWindowsAutomatically(true);


        regWebView.setWebViewClient(new WebViewClient());
        regWebView.setWebChromeClient(new WebChromeClient() {
            public boolean onShowFileChooser(
                    WebView webView, ValueCallback<Uri[]> filePathCallback,
                    FileChooserParams fileChooserParams) {
                return false;
            }
        });

        regWebView.loadUrl(url);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

    }
}
