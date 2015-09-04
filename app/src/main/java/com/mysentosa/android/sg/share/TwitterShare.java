package com.mysentosa.android.sg.share;


import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.auth.AccessToken;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.mysentosa.android.sg.R;

public class TwitterShare {
	private Activity mActivity;
	private ProgressDialog progressDialog;
	private Dialog postDialog;
	private static final String CONSUMER_KEY = "5kWHIH9fp96tzICKRZR14w";
	private static final String CONSUMER_SECRET = "hWbpkLuqaOjTlDvLPJiB3hQUqscF3stedukCSfUG3E";
	protected static final int OAUTHERROR = 0, TWEET_EMPTY = 1, TWEET_TOO_LONG = 2, TWEET_POST = 3, ERROR_POST = 4, ACCESS_REVOKED = 5, DUPLICATE_TWEET = 6;
	public static final String CALLBACK_URL = "com-company2359-sentosa:///";
	private final int POST_DIALOG = 0, PROGRESS_DIALOG = 1;

	private static Twitter mTwitter;
	private String message = "";

	public TwitterShare(Activity activity) {
		this.mActivity = activity;
		initializeTwitterObject();
	} 

	public static void initializeTwitterObject() {
		mTwitter = new TwitterFactory().getInstance();
		mTwitter.setOAuthConsumer(CONSUMER_KEY, CONSUMER_SECRET);
		mTwitter.setOAuthAccessToken(null);
	}

	public boolean isSessionValid() {	
		return SessionStore.restoreTwitterAuthorizationToken(mTwitter, mActivity);
	}

	public void displayDialog(int id) {
		switch(id) {
		case POST_DIALOG:
			if(!postDialog.isShowing())
				postDialog.show();
			break;
		case PROGRESS_DIALOG:
			if(!progressDialog.isShowing())
				progressDialog.show();
			break;
		}
	}

	public void dismissDialog(int id) {
		switch(id) {
		case POST_DIALOG:
			if(postDialog.isShowing())
				postDialog.dismiss();
			break;
		case PROGRESS_DIALOG:
			if(progressDialog.isShowing())
				progressDialog.dismiss();
			break;
		}
	}

	public void sendTweet(String msg) {
		this.message = msg;
		buildTwitterDialog();
		if(!isSessionValid()) {
			initializeTwitterObject();
			authorizeTwitter();
		}
		else displayDialog(POST_DIALOG);	
	}


	public void authorizeTwitter() {
		new TwitterOAuthDialog(mActivity, mTwitter, new DialogListener() {

			@Override
			public void onFail() {
				displayToast(OAUTHERROR);
			}

			@Override
			public void onComplete(AccessToken at) {
				SessionStore.saveTwitterAuthorizationToken(at, mActivity);
				displayDialog(POST_DIALOG);
			}
		}).show();
	}

	public class SendTweetTask extends AsyncTask<Void, Void, Integer> {
		@Override
		protected Integer doInBackground(Void... params) {
			try {
				mTwitter.updateStatus(message);
			} catch(TwitterException e) {
				e.printStackTrace();
				if(e.getStatusCode()==401) {
					SessionStore.clearTwitterAuthorizationToken(mActivity);
					return ACCESS_REVOKED;
				}
				if(e.getStatusCode()==403) {
					return DUPLICATE_TWEET;
				}
				return ERROR_POST;
			} catch (Exception e) {
				e.printStackTrace();
				return ERROR_POST;
			}
			return TWEET_POST;
		}

		@Override 
		protected void onPostExecute(Integer result) {
			dismissDialog(PROGRESS_DIALOG);
			displayToast(result);
		}
	}

	public void buildTwitterDialog() {
		postDialog = new Dialog(mActivity,R.style.ShareDialog);

		//postDialog.requestWindowFeature(Window.FEATURE_LEFT_ICON);
		postDialog.setContentView(R.layout.dialog_twitter_alert);
		//postDialog.setFeatureDrawableResource(Window.FEATURE_LEFT_ICON, R.drawable.icon);

		//this is the character count
		final TextView charCount = (TextView) postDialog.findViewById(R.id.tv_char_count);

		//set the relevant texts
		final EditText tweetText = (EditText) postDialog.findViewById(R.id.et_edit_message);
		tweetText.setText(message);

		tweetText.addTextChangedListener(new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				//This sets a textview to the current length
				if(charCount!=null)
					charCount.setText(String.valueOf((140-s.length())));
			}

			@Override
			public void afterTextChanged(Editable s) {
			}
		});

		//Setting current character count
		charCount.setText(String.valueOf((140-tweetText.getText().length())));

		//set the onClickListener for post and cancel buttons
		(postDialog.findViewById(R.id.btn_twitter_post)).setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				String txt = tweetText.getText().toString();
				if(txt.equals("") || txt==null) 
					displayToast(TWEET_EMPTY);
				else if(txt.length()>=140)
					displayToast(TWEET_TOO_LONG);				
				else {
					message = txt;
					dismissDialog(POST_DIALOG);
					displayDialog(PROGRESS_DIALOG);
					new SendTweetTask().execute();
				}
			}

		});

		(postDialog.findViewById(R.id.btn_twitter_cancel)).setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				dismissDialog(POST_DIALOG);
			}

		});

		progressDialog = new ProgressDialog(mActivity);
		progressDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		progressDialog.setMessage(mActivity.getString(R.string.twitter_posting_tweet));
		progressDialog.setCancelable(false);
	}

	public void displayToast(int TOAST_ID) {
		final String message;
		switch(TOAST_ID) {
		case OAUTHERROR:
			message = mActivity.getText(R.string.twitter_oauth_error).toString();
			break;
		case TWEET_EMPTY:
			message = mActivity.getText(R.string.twitter_tweet_empty).toString();
			break;
		case TWEET_TOO_LONG:
			message = mActivity.getText(R.string.twitter_tweet_too_long).toString();
			break;
		case TWEET_POST:
			message = mActivity.getText(R.string.twitter_tweet_succeful).toString();
			break;	
		case ERROR_POST:
			message = mActivity.getText(R.string.twitter_tweet_error).toString();
			break;			
		case DUPLICATE_TWEET:
			message = mActivity.getText(R.string.twitter_duplicate_tweet).toString();
			break;			
		case ACCESS_REVOKED:
			message = mActivity.getText(R.string.twitter_access_revoked).toString();
			break;			
		default:
			message = mActivity.getText(R.string.twitter_oauth_error).toString();
			break;
		}	

		Toast.makeText(mActivity.getApplicationContext(), message, Toast.LENGTH_LONG).show();

	}

	public interface DialogListener {
		public void onComplete(AccessToken at);
		public void onFail();
	} 

}
