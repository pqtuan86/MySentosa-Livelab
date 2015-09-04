package com.mysentosa.android.sg.asynctask;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONObject;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.view.Window;

import com.mysentosa.android.sg.R;
import com.mysentosa.android.sg.models.MyPurchasesDetailItem;
import com.mysentosa.android.sg.utils.AlertHelper;
import com.mysentosa.android.sg.utils.Const;
import com.mysentosa.android.sg.utils.CustomCallback;
import com.mysentosa.android.sg.utils.HttpHelper;
import com.mysentosa.android.sg.utils.JSONParser;
import com.mysentosa.android.sg.utils.SHAUtils;
import com.mysentosa.android.sg.utils.SentosaUtils;

public class MyPurchaseDetailAsyncTask extends AsyncTask<Void, Void, Boolean> {

	private static final String DATA_JSON = "Data";
	private static final String CODE_JSON = "StatusCode";
	private static final String MY_PURCHASE_DETAIL = "TicketPurchases/TicketsDetails";
	CustomCallback mListener;
	ProgressDialog pd;
	SharedPreferences sharePref;
	String Reservation_id;
	String timeStamp;
	String tokenHash;
	private WeakReference<Activity> mActivityRef;
	private int mStatusCode;
	
	public MyPurchaseDetailAsyncTask(Activity context, CustomCallback mListener,
			String reservation_id) {
		this.mListener = mListener;
		sharePref = PreferenceManager.getDefaultSharedPreferences(context);
		this.Reservation_id = reservation_id;		
		pd = new ProgressDialog(context);
		pd.setCancelable(false);
		pd.requestWindowFeature(Window.FEATURE_NO_TITLE);
		pd.setMessage("Loading");
		
		mActivityRef = new WeakReference<Activity>(context);
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
			nameValue.add(new BasicNameValuePair("reservationId", Reservation_id));

			String result = HttpHelper.sendRequestUsingPost(
					HttpHelper.BASE_ADDRESS + MY_PURCHASE_DETAIL, nameValue);
			
			JSONObject rootObject = new JSONObject(result);
			mStatusCode = rootObject.getInt(CODE_JSON);
			if (mStatusCode == 0) {
				 JSONArray dataObject = rootObject.getJSONArray(DATA_JSON);
				 updatePackage(dataObject);
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
		
		if (mStatusCode == -1 && mActivityRef.get() != null)
			AlertHelper.showKillSwitchAlert(mActivityRef.get(), R.string.error_ticketing_kill_switch);
	}
	
	private void updatePackage(JSONArray rootObject) {
		Const.mPurchaseDetailItems.clear();
		MyPurchasesDetailItem[] ticketsResponse = JSONParser.getResponse(
				MyPurchasesDetailItem[].class, new String(rootObject.toString()));
		if (ticketsResponse != null) {
			for (MyPurchasesDetailItem tickets : ticketsResponse) {
				Const.mPurchaseDetailItems.add(tickets);
			}
		}
	}
}
