package com.mysentosa.android.sg;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.android.volley.Response.ErrorListener;
import com.android.volley.VolleyError;
import com.flurry.android.FlurryAgent;
import com.mysentosa.android.sg.custom_views.CustomMenu;
import com.mysentosa.android.sg.helper.AlertDialogHelper;
import com.mysentosa.android.sg.helper.ProgressDialogHelper;
import com.mysentosa.android.sg.helper.VolleyErrorHelper;
import com.mysentosa.android.sg.imageloader.ImageFetcher;
import com.mysentosa.android.sg.utils.Const.FlurryStrings;
import com.mysentosa.android.sg.utils.SentosaUtils;
import com.urbanairship.UAirship;

public class BaseActivity extends FragmentActivity {

	public static final int ACTIVITY_MAP = 22, ACTIVITY_HOME = 23,
			ACTIVITY_EVENT_PROMO = 24, ACTIVITY_THINGS_TO_DO = 25,
			ACTIVITY_MYBOOKMARKS = 26;
	
	public static final String SOURCE_ACTIVITY = "SOURCE_ACTIVITY";

	// Here we hold a reference to custom menu. This is initialized in the
	// extended activities.
	public CustomMenu customMenu = null;
	private boolean isActivityBottomOfStack = false;
	public static final String ACTIVITY_BOTTOM_OF_STACK = "ACTIVITY_TOP_OF_STACK";
	public boolean isAlive;
	public ImageFetcher mImageFetcher;

	ProgressDialogHelper mPBLoading;
    AlertDialogHelper alertDialog;
    
	@Override
	public void onBackPressed() {
//		if (!isActivityBottomOfStack) {
//			super.onBackPressed();
//			overridePendingTransition(0, 0);
//		} else {
//			showDialog(209);
//		}

	    if (this instanceof HomeActivity) {
	        showDialog(209);
	    } else {
	        super.onBackPressed();
	        overridePendingTransition(0, 0);
	    }

	}

	@Override
	protected void onCreate(Bundle arg0) {
		super.onCreate(arg0);
		// Appirater.appLaunched(this);
		mPBLoading = new ProgressDialogHelper(this);
		mImageFetcher = ((SentosaApplication) this.getApplication()).mImageFetcher;
	}

	@Override
	protected void onStart() {
		super.onStart();
		UAirship.shared().getAnalytics().activityStarted(this);

		isActivityBottomOfStack = this.getIntent().getBooleanExtra(
				ACTIVITY_BOTTOM_OF_STACK, false);
		Log.d(" testing", " testing fluryy key " + FlurryStrings.FLURRY_KEY);
		FlurryAgent.onStartSession(this, FlurryStrings.FLURRY_KEY);
		FlurryAgent.onPageView();
		isAlive = true;
		if (customMenu == null) {
			customMenu = new CustomMenu(this, false);
			// FrameLayout.Layout Params flp = new
			// FrameLayout.LayoutParams(LayoutParams.FILL_PARENT,
			// LayoutParams.FILL_PARENT);
			// flp.gravity = Gravity.BOTTOM;
			// flp.bottomMargin = Math.round(-1*customMenu.getMenuHt());
			// customMenu.setLayoutParams(flp);
			FrameLayout fl = (FrameLayout) this.findViewById(
					android.R.id.content).getRootView();
			fl.addView(customMenu.getMenu());
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
	}

	@Override
	public void onStop() {
		super.onStop();
		UAirship.shared().getAnalytics().activityStopped(this);

		FlurryAgent.onEndSession(this);
		Log.i("onStop", "onStop");
		isAlive = false;
	}

	@Override
	public void startActivity(Intent i) {
		// if(customMenu!=null) customMenu.hideMenu();
		i.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
		super.startActivity(i);
		overridePendingTransition(R.anim.fadein, R.anim.fadeout);
	}

	@Override
	public void startActivityForResult(Intent i, int requestCode) {
		// if(customMenu!=null) customMenu.hideMenu();
		i.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
		super.startActivityForResult(i, requestCode);
		overridePendingTransition(R.anim.fadein, R.anim.fadeout);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		Log.i("onDestroy", "onDestroy");
	}

	@Override
	protected Dialog onCreateDialog(int id, Bundle args) {
		if (id == 209) {
			AlertDialog.Builder builderCall = new AlertDialog.Builder(this);
			builderCall.setTitle("Are you sure you want to quit?");
			// builder.setIcon(R.drawable.stub_thumb);
			builderCall.setNegativeButton("Cancel",
					new android.content.DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							dialog.dismiss();
						}
					});
			builderCall.setPositiveButton("Quit",
					new android.content.DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							dialog.dismiss();
							setResult(NavigationManagerActivity.QUIT_NAVIGATION_MANAGER_ACTIVITY);
							BaseActivity.super.onBackPressed();
							overridePendingTransition(0, 0);
						}
					});
			return builderCall.create();
		}
		return null;
	}

	ErrorListener mErrorListener = new ErrorListener() {

        @Override
        public void onErrorResponse(VolleyError error) {
            dismissProgressDialog();
            String errorText = "";
            String errorFromServerReturn = SentosaUtils.getErrorMessageFromErrorResponse(error.networkResponse);
            if (SentosaUtils.isValidString(errorFromServerReturn)) {
                errorText = errorFromServerReturn;
            } else {
                errorText = VolleyErrorHelper.handleError(error);
            }

            if (SentosaUtils.isValidString(errorText)) {
                showErrorDialog(errorText);
            }
        }
    };
    
    void showErrorDialog(String message) {
        alertDialog = new AlertDialogHelper(this, "Error", message, "OK");
        alertDialog.show();
    }

    void dismissProgressDialog() {
        if (mPBLoading != null) {
            mPBLoading.dismiss();
        }
    }
}
