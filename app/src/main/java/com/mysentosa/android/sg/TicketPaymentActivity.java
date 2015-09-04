package com.mysentosa.android.sg;

import android.app.Activity;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.http.SslError;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.SslErrorHandler;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.flurry.android.FlurryAgent;
import com.google.analytics.tracking.android.EasyTracker;
import com.google.analytics.tracking.android.Fields;
import com.google.analytics.tracking.android.MapBuilder;
import com.mysentosa.android.sg.asynctask.CallbackAsyncTask;
import com.mysentosa.android.sg.asynctask.CancelBookingAsyncTask;
import com.mysentosa.android.sg.asynctask.ConfirmBookingAsyncTask;
import com.mysentosa.android.sg.provider.utils.SentosaDatabaseStructure.ContentURIs;
import com.mysentosa.android.sg.utils.Const;
import com.mysentosa.android.sg.utils.Const.FlurryStrings;
import com.mysentosa.android.sg.utils.CustomCallback;
import com.mysentosa.android.sg.utils.HttpHelper;

public class TicketPaymentActivity extends Activity {
	private WebView mWebView;
	SharedPreferences sharePref;
	ProgressBar progressBar;
	ImageView iv_cancel;
	private boolean isCallback = false;
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		FlurryAgent.logEvent(FlurryStrings.TelemoneyPage);
		setContentView(R.layout.payment_screen);
		sharePref = PreferenceManager.getDefaultSharedPreferences(this);
		initializeViews();
		
		EasyTracker easyTracker = EasyTracker.getInstance(this);
		easyTracker.set(Fields.SCREEN_NAME, Const.GAStrings.TELEMONEY);
	    easyTracker.send(MapBuilder
		      .createAppView()
		      .build()
	    );
	}

	private void initializeViews() {
		iv_cancel = (ImageView) findViewById(R.id.iv_cancel);
		iv_cancel.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				CancelBookingAsyncTask cancelBooking = new CancelBookingAsyncTask(
						TicketPaymentActivity.this, new CustomCallback() {
							@Override
							public void isFnished(boolean isSucceed) {
								setResult(Const.RESULT_CANCEL_CODE);
								finish();
							}
						});
				cancelBooking.execute();
			}
		});

		progressBar = (ProgressBar) findViewById(R.id.progressBar1);
		progressBar.setVisibility(View.VISIBLE);

		mWebView = (WebView) findViewById(R.id.webview);
		mWebView.getSettings().setJavaScriptEnabled(true);
		
		mWebView.getSettings().setLoadWithOverviewMode(true);
		mWebView.getSettings().setSupportZoom(true);
		mWebView.getSettings().setBuiltInZoomControls(true);
		
		mWebView.setVerticalScrollBarEnabled(false);
		mWebView.setHorizontalScrollBarEnabled(false);
		mWebView.getSettings().setUseWideViewPort(true);
		mWebView.getSettings().setSavePassword(false);
		mWebView.getSettings().setSaveFormData(false);
		
		mWebView.setWebViewClient(new WebViewClient() {
		    
			@Override
			public void onPageStarted(WebView view, String url, Bitmap favicon) {
				super.onPageStarted(view, url, favicon);
			}

			@Override
			public boolean shouldOverrideUrlLoading(WebView view, String url) {		
				if (url.startsWith(HttpHelper.BASE_ADDRESS
						+ HttpHelper.SHOPPINGCART)) {
					if (!isCallback) {
						isCallback = true;
						CallbackAsyncTask callback = new CallbackAsyncTask(
								TicketPaymentActivity.this,
								new CustomCallback() {
									@Override
									public void isFnished(boolean isSucceed) {
										if (isSucceed) {
											ConfirmBookingAsyncTask confirmAPI = new ConfirmBookingAsyncTask(
													TicketPaymentActivity.this,
													new CustomCallback() {
														@Override
														public void isFnished(
																boolean isSucceed) {
															if (isSucceed) {
																getContentResolver()
																		.delete(ContentURIs.CART_URI,
																				null,
																				null);
																setResult(RESULT_OK);
																finish();
															} else {
																setResult(Const.RESULT_CANCEL_CODE);
																finish();
															}
														}
													});
											confirmAPI.execute();
										} else {
											setResult(RESULT_CANCELED);
											finish();
										}
									}
								});
						callback.execute();
					}
				} else {
					view.loadUrl(url);
				}

				return true;
			}

			@Override
			public void onPageFinished(WebView view, String url) {
				super.onPageFinished(view, url);
				mWebView.setVisibility(View.VISIBLE);
				progressBar.setVisibility(View.GONE);
			}
			
			@Override
	        public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
	            handler.proceed(); // Ignore SSL certificate errors
	        }
		});
        
		mWebView.loadUrl(sharePref.getString("PaymentUrl", null));
	}
	
	@Override
	public void onBackPressed() {
	    // do nothing
	}
}
