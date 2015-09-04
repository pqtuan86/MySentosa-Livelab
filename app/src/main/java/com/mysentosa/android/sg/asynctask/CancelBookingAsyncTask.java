package com.mysentosa.android.sg.asynctask;

import java.util.ArrayList;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.view.Window;

import com.mysentosa.android.sg.utils.CustomCallback;
import com.mysentosa.android.sg.utils.HttpHelper;
import com.mysentosa.android.sg.utils.SHAUtils;
import com.mysentosa.android.sg.utils.SentosaUtils;

public class CancelBookingAsyncTask extends AsyncTask<Void, Void, Boolean> {
	
	private static final String CODE_JSON = "StatusCode";
	private static final String CANCEL_BOOKING = "ShoppingCart/CancelBooking";
	CustomCallback mListener;
	ProgressDialog pd;
	SharedPreferences sharePref;
	String timeStamp;
	String tokenHash;
	
	public CancelBookingAsyncTask(Context context, CustomCallback mListener) {
		this.mListener = mListener;
		sharePref = PreferenceManager.getDefaultSharedPreferences(context);

		pd = new ProgressDialog(context);
		pd.setCancelable(false);
		pd.requestWindowFeature(Window.FEATURE_NO_TITLE);
		pd.setMessage("Loading");
	}

	@Override
	protected void onPreExecute() {
		super.onPreExecute();
		if (pd != null)
			pd.show();
	}

	@Override
	protected Boolean doInBackground(Void... arg0) {
		try {
			
			timeStamp = String.valueOf(SentosaUtils.getTimeStamp());
			String token = sharePref.getString("Token", null);
			tokenHash = SHAUtils.SHA1(token + timeStamp);

			ArrayList<NameValuePair> nameValue = new ArrayList<NameValuePair>();
			nameValue.add(new BasicNameValuePair("device_id", sharePref
					.getString("Device_Id", "0")));
			nameValue.add(new BasicNameValuePair("timestamp", timeStamp));
			nameValue.add(new BasicNameValuePair("token_hash",tokenHash));
			nameValue.add(new BasicNameValuePair("sessionid", sharePref
					.getString("SessionId", null)));
			
			String result = HttpHelper.sendCustomRequestUsingPost(
					HttpHelper.BASE_ADDRESS + CANCEL_BOOKING, nameValue);
			

			JSONObject rootObject = new JSONObject(result);
			if (rootObject.getInt(CODE_JSON) == 0) {			
				return true;
			} else {
				return false;
			}
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	@Override
	protected void onPostExecute(Boolean result) {
		super.onPostExecute(result);
		if (pd != null && pd.isShowing()) {
			pd.dismiss();
		}
		mListener.isFnished(result);
	}
}
