/*
 * Copyright 2010 Facebook, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.mysentosa.android.sg.share;


import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.auth.AccessToken;
import twitter4j.auth.RequestToken;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.mysentosa.android.sg.R;
import com.mysentosa.android.sg.share.TwitterShare.DialogListener;


public class TwitterOAuthDialog extends Dialog {

	static final int FB_BLUE = 0xFF6D84B4;
	static final float[] DIMENSIONS_DIFF_LANDSCAPE = {20, 60};
	static final float[] DIMENSIONS_DIFF_PORTRAIT = {40, 60};
	static final FrameLayout.LayoutParams FILL =
		new FrameLayout.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT,
				ViewGroup.LayoutParams.FILL_PARENT);
	static final int MARGIN = 4;
	static final int PADDING = 2;
	static final String DISPLAY_STRING = "touch";
	static final String FB_ICON = "icon.png";

	private String mUrl;
	private ProgressDialog mSpinner;
	private ImageView mCrossImage;
	private WebView mWebView;
	private FrameLayout mContent;
	private Activity mActivity;
	private DialogListener mListener;
	private Twitter mTwitter;
	private static final String CALLBACK_URL = "com-company2359-sentosa:///";
	private static RequestToken mReqToken;
	public TwitterOAuthDialog(Context context, Twitter twitter, DialogListener Listener) {
		super(context, android.R.style.Theme_Translucent_NoTitleBar);
		mActivity = (Activity)context;
		mTwitter = twitter;
		mListener = Listener;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mSpinner = new ProgressDialog(getContext());
		mSpinner.requestWindowFeature(Window.FEATURE_NO_TITLE);
		mSpinner.setMessage("Loading Page...");

		requestWindowFeature(Window.FEATURE_NO_TITLE);
		mContent = new FrameLayout(getContext());

		/* Create the 'x' image, but don't add to the mContent layout yet
		 * at this point, we only need to know its drawable width and height 
		 * to place the webview
		 */
		createCrossImage();

		/* Now we know 'x' drawable width and height, 
		 * layout the webivew and add it the mContent layout
		 */
		int crossWidth = mCrossImage.getDrawable().getIntrinsicWidth();
		setUpWebView(crossWidth / 2);

		/* Finally add the 'x' image to the mContent layout and
		 * add mContent to the Dialog view
		 */
		mContent.addView(mCrossImage, new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
		addContentView(mContent, new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));

		retrieveRequestToken();

	}

	private void retrieveRequestToken() {
		mSpinner.show();
		new Thread() {
			@Override
			public void run() {
				try {
					if(mReqToken == null)
						mReqToken = mTwitter.getOAuthRequestToken(CALLBACK_URL);
					mUrl = mReqToken.getAuthenticationURL();
					mWebView.loadUrl(mUrl);
				} catch (TwitterException e) {
					mActivity.runOnUiThread(new Runnable() {

						@Override
						public void run() {
							if(mSpinner.isShowing()) mSpinner.dismiss();
							mListener.onFail(); 
							TwitterOAuthDialog.this.dismiss();							
						}
					});	
				}
			}
		}.start();
	}

	private void createCrossImage() {
		mCrossImage = new ImageView(getContext());
		// Dismiss the dialog when user click on the 'x'
		mCrossImage.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				//mListener.onFail();
				TwitterOAuthDialog.this.dismiss();
			}
		});
		Drawable crossDrawable = getContext().getResources().getDrawable(R.drawable.icon_close);
		mCrossImage.setImageDrawable(crossDrawable);
		/* 'x' should not be visible while webview is loading
		 * make it visible only after webview has fully loaded
		 */
		mCrossImage.setVisibility(View.INVISIBLE);
	}

	private void setUpWebView(int margin) {
		LinearLayout webViewContainer = new LinearLayout(getContext());
		mWebView = new WebView(getContext());
		mWebView.setVerticalScrollBarEnabled(false);
		mWebView.setHorizontalScrollBarEnabled(false);
		mWebView.setWebViewClient(new TwitterOAuthDialog.mWebViewClient());
		mWebView.getSettings().setJavaScriptEnabled(true);
		//mWebView.loadUrl(mUrl);
		mWebView.setLayoutParams(FILL);
		mWebView.setVisibility(View.INVISIBLE);
			

		webViewContainer.setPadding(margin, margin, margin, margin);
		webViewContainer.addView(mWebView);
		mContent.addView(webViewContainer);
	}

	private class mWebViewClient extends WebViewClient {

		@Override
		public boolean shouldOverrideUrlLoading(WebView view, String url) {
			Log.i("Redirect",url);
			if(url.startsWith(TwitterShare.CALLBACK_URL)) {	
				TwitterOAuthDialog.this.dismiss();
				String oauthVerifier = Uri.parse(url).getQueryParameter("oauth_verifier");
				if(oauthVerifier != null) {
					Log.i("oauthVerifier",oauthVerifier);
					new TwitterTask(oauthVerifier).execute();
				}
				else mListener.onFail();
				return true;
			} 
			view.loadUrl(url);
			return true;
		}

		@Override
		public void onReceivedError(WebView view, int errorCode,
				String description, String failingUrl) {
			super.onReceivedError(view, errorCode, description, failingUrl);
			mListener.onFail();
			TwitterOAuthDialog.this.dismiss();
		}

		@Override
		public void onPageStarted(WebView view, String url, Bitmap favicon) {
			Log.d("Facebook-WebView", "Webview loading URL: " + url);
			super.onPageStarted(view, url, favicon);
			mSpinner.show();
		}

		@Override
		public void onPageFinished(WebView view, String url) {
			super.onPageFinished(view, url);
			mSpinner.dismiss();
			mContent.setBackgroundColor(Color.TRANSPARENT);
			mWebView.setVisibility(View.VISIBLE);
			mCrossImage.setVisibility(View.VISIBLE);
		}
	}

	public void goBack() {
		mWebView.goBack();
	}

	public boolean canGoBack() {
		return mWebView.canGoBack();
	}

	@Override
	public void onBackPressed() { 
		if (mWebView.canGoBack()) {
			mWebView.loadUrl(mUrl);
		}
	}

	public void setUrl(String url) {
		mUrl = url;
		mWebView.loadUrl(url);
	}


	public class TwitterTask extends AsyncTask<Void, Void, Boolean> {
		final String oauthVerifier;
		public TwitterTask(String oauthVerifier) {
			this.oauthVerifier = oauthVerifier;
		}

		@Override
		protected Boolean doInBackground(Void... params) {
			final AccessToken at;
			try {
				at = mTwitter.getOAuthAccessToken(mReqToken, oauthVerifier);
				mTwitter.setOAuthAccessToken(at);
				mActivity.runOnUiThread(new Runnable() {
					@Override
					public void run() {
						mListener.onComplete(at);
					}
				});
			} catch (TwitterException e) {
				e.printStackTrace();
				mListener.onFail();
			}
			return true;
		}
	}
}
