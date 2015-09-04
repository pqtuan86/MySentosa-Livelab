package com.mysentosa.android.sg;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebView;
import android.widget.TextView;

import com.mysentosa.android.sg.asynctask.TermAndConditionAsyncTask;
import com.mysentosa.android.sg.utils.Const;
import com.mysentosa.android.sg.utils.CustomCallback;

public class TicketTermsActivity extends Activity {
	private WebView mWebView;	
	public static String Header = "<HTML><HEAD></HEAD><BODY>";
	public static String Footer = "</BODY></HTML>";

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.term_screen);

		TermAndConditionAsyncTask terms = new TermAndConditionAsyncTask(this,
				new CustomCallback() {
					@Override
					public void isFnished(boolean isSucceed) {						
						if (isSucceed) {
							initializeViews();
						}
					}
				});
		terms.execute();

	}

	private void initializeViews() {		
		mWebView = (WebView) findViewById(R.id.webview);
		mWebView.setVisibility(View.VISIBLE);
		mWebView.getSettings().setJavaScriptEnabled(true);
		mWebView.setVerticalScrollBarEnabled(false);	
		mWebView.setHorizontalScrollBarEnabled(false);
		TextView tv_screen_title = (TextView) this
				.findViewById(R.id.header_title);
		mWebView.loadDataWithBaseURL(null, Header + Const.sbTerms.toString().trim()
				+ Footer, "text/html", "UTF-8", null);
	}

}
