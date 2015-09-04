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

import com.mysentosa.android.sg.ProfileAndSettingsActivity;
import com.mysentosa.android.sg.utils.CustomCallback;
import com.mysentosa.android.sg.utils.HttpHelper;
import com.mysentosa.android.sg.utils.SHAUtils;
import com.mysentosa.android.sg.utils.SentosaUtils;

public class ConfirmBookingAsyncTask extends AsyncTask<Void, Void, Boolean> {

	private static final String CODE_JSON = "StatusCode";
	private static final String CONFIRM_BOOKING = "ShoppingCart/ConfirmBooking";
	CustomCallback mListener;
	ProgressDialog pd;
	SharedPreferences sharePref;
	SharedPreferences mPrefs;
	String birthDate = "01-01-1970";
	String timeStamp;
	String tokenHash;
	

	public ConfirmBookingAsyncTask(Context context, CustomCallback mListener) {
		this.mListener = mListener;
		sharePref = PreferenceManager.getDefaultSharedPreferences(context);
		mPrefs = context.getSharedPreferences(
				ProfileAndSettingsActivity.USER_DETAILS_PREFS,
				context.MODE_PRIVATE);
		pd = new ProgressDialog(context);
		pd.setCancelable(false);
		pd.requestWindowFeature(Window.FEATURE_NO_TITLE);
		pd.setMessage("Checking purchase");

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
					.getString("Device_Id", null)));
			nameValue.add(new BasicNameValuePair("timestamp", timeStamp));
			nameValue.add(new BasicNameValuePair("token_hash", tokenHash));
			nameValue.add(new BasicNameValuePair("sessionid", sharePref
					.getString("SessionId", null)));
			nameValue.add(new BasicNameValuePair("name", mPrefs.getString(
					"NAME", null)));
			nameValue.add(new BasicNameValuePair("email", mPrefs.getString(
					"EMAIL", null)));
			nameValue.add(new BasicNameValuePair("address", mPrefs.getString(
					"POSTAL_CODE", "")));

			// (dd-MM-yyyy) format

			String dd = mPrefs.getString("DD", null);
			if (dd != null) {
				String mm = mPrefs.getString("MM", null);
				if (mm != null) {
					if (mm.length() <= 1)
						mm = "0" + mm;
				}
				String yyyy = mPrefs.getString("YYYY", null);
				birthDate = dd + "-" + mm + "-" + yyyy;
			}

			nameValue.add(new BasicNameValuePair("birthdate", birthDate));

			// 'f': female, 'm': male
			nameValue.add(new BasicNameValuePair("gender", (mPrefs.getString(
					"GENDER", "male").equalsIgnoreCase("male")) ? "m" : "f"));
			nameValue.add(new BasicNameValuePair("mobile", mPrefs.getString(
					"MOBILE", "11111111")));
			nameValue.add(new BasicNameValuePair("nric", sharePref.getString(
					"NRIC", "")));
			// boolean true or false
			nameValue.add(new BasicNameValuePair("newsletter", sharePref
					.getString("NEWS", "false")));

			
			
			String result = HttpHelper.sendCustomRequestUsingPost(
					HttpHelper.BASE_ADDRESS + CONFIRM_BOOKING, nameValue);
	
		
			
			JSONObject rootObject = new JSONObject(result);
			if (rootObject.getInt(CODE_JSON) == 0) {
				// JSONObject dataObject = rootObject.optJSONObject(DATA_JSON);
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
