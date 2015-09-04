package com.mysentosa.android.sg.helper;

import com.mysentosa.android.sg.R;

import android.app.ProgressDialog;
import android.content.Context;

public class ProgressDialogHelper {

	String title, message;
	ProgressDialog mDialog;
	Context mContext;
	
//	public ProgressDialogHelper(Context context, String title, String message) {
//		this.mContext = context;
//		this.title = title;
//		this.message = message;
//		mDialog = new ProgressDialog(mContext);
//	}
	
	public ProgressDialogHelper(Context context) {
		this.mContext = context;
		this.title = "";
		this.message = "";
		
		mDialog = new ProgressDialog(mContext);
	}
	
	public void show() {
		try
		{
			if(mDialog!=null && !mDialog.isShowing())
			{    
			    mDialog.setCancelable(false);  
			    mDialog.setTitle(title);
	            mDialog.setMessage(message);
	            mDialog.show();
	            mDialog.setContentView(R.layout.progress_dialog);
	            //Title and Message not work now
	            //Have to re-implement if want to set Text and Title
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void dismiss() {
		try
		{
			if(mDialog!=null && mDialog.isShowing())
				mDialog.dismiss();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
}
