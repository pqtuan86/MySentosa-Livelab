package com.mysentosa.android.sg.helper;

import com.mysentosa.android.sg.R;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;

public class AlertDialogHelper {

	String title, message, positiveButtonText, negativeButtonText, neutralButtonText;
	OnClickListener postiveClickListener, negativeClickListener;
	AlertDialog mDialog;
	Context mContext;
	
	public AlertDialogHelper(Context context, String title, String message, String posText, String negText) {
		this.mContext = context;
		this.title = title;
		this.message = message;
		this.positiveButtonText = posText;
		this.negativeButtonText = negText;
		this.neutralButtonText = null;
	}
	
	public AlertDialogHelper(Context context, String title, String message, String neutralButtonText) {
		this.mContext = context;
		this.title = title;
		this.message = message;
		this.positiveButtonText = null;
		this.neutralButtonText = neutralButtonText;
		this.negativeButtonText = null;
	}
	
    public AlertDialogHelper(Context context, String title, String message, String neutralButtonText, String posText,
            String negText) {
        this.mContext = context;
        this.title = title;
        this.message = message;
        this.positiveButtonText = posText;
        this.negativeButtonText = negText;
        this.neutralButtonText = neutralButtonText;
    }
	
	public void setPostiveClickListener(OnClickListener posClickListener) {
		this.postiveClickListener = posClickListener;
	}
	
	public void setNegativeClickListener(OnClickListener negClickListener) {
		this.negativeClickListener = negClickListener;
	}
	
	public void show() {
		AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
		builder.setMessage(message);
		builder.setTitle(title);
		builder.setCancelable(false);
		
		if(postiveClickListener == null && positiveButtonText!=null)
		{
			builder.setPositiveButton(positiveButtonText, new OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					dismiss();
				}
			});
		}
		else {
			builder.setPositiveButton(positiveButtonText, postiveClickListener);
		}
		
		if(negativeClickListener == null && negativeButtonText!=null)
		{
			builder.setNegativeButton(negativeButtonText, new OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					dismiss();
				}
			});
		}
		else {
			builder.setNegativeButton(negativeButtonText, negativeClickListener);
		}
		
		if(neutralButtonText!=null)
		{
			builder.setNeutralButton(neutralButtonText, new OnClickListener() {
				
				@Override
				public void onClick(DialogInterface arg0, int arg1) {
					dismiss();
				}
			});
		}
		
		mDialog = builder.create();
		try {
			mDialog.show();
		} catch (Exception e) {
		}
	}
	
	public void dismiss() {
		try {
			if(mDialog!=null && mDialog.isShowing())
				mDialog.dismiss();
			
		} catch (Exception e) {
		}
	}
}
