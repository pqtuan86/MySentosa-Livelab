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
import android.os.AsyncTask;
import android.view.Window;

import com.mysentosa.android.sg.R;
import com.mysentosa.android.sg.utils.AlertHelper;
import com.mysentosa.android.sg.utils.Const;
import com.mysentosa.android.sg.utils.CustomCallback;
import com.mysentosa.android.sg.utils.HttpHelper;

public class AvailableTiketsAsyncTask extends AsyncTask<Void, Void, Boolean> {

	private static final String CODE_JSON = "StatusCode";
	private static final String DATA_JSON = "Data";
	private static final String FAILED_JSON = "FailedIds";
	private static final String CHECK_EVENT_TICKETS = "EventTickets/CheckTicketsAvailable";
	private static final String EVENT_ID = "eventIds"; // [{index}](int)(index
														// is array index)
	CustomCallback mListener;
	Integer[] id;
	ArrayList<Integer> arrayId;
	ProgressDialog pd;
	
	private int mStatusCode;
	private WeakReference<Activity> mActivityRef;

	public AvailableTiketsAsyncTask(Activity context, CustomCallback mListener,
			ArrayList<Integer> arrayId) {
		this.mListener = mListener;
		this.arrayId = arrayId;
		pd = new ProgressDialog(context);
		pd.setCancelable(false);
		pd.requestWindowFeature(Window.FEATURE_NO_TITLE);
		pd.setMessage("Checking for available tickets");
		
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
			ArrayList<NameValuePair> nameValue = new ArrayList<NameValuePair>(
					arrayId.size());
			for (int i = 0; i < arrayId.size(); i++)
				nameValue.add(new BasicNameValuePair(EVENT_ID + "[" + i + "]",
						String.valueOf(arrayId.get(i))));

			String result = HttpHelper.sendRequestUsingPost(
					HttpHelper.BASE_ADDRESS + CHECK_EVENT_TICKETS, nameValue);
			//LogHelper.i("TAG","result: "+result);
			JSONObject rootObject = new JSONObject(result);
			mStatusCode = rootObject.getInt(CODE_JSON);
			if (mStatusCode == 0) {
				return true;
			} else {
				updatePackage(rootObject);
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

	private void updatePackage(JSONObject rootObject) throws Exception {
		Const.mFailedTicketsItems.clear();
		JSONObject jObj = rootObject.getJSONObject(DATA_JSON);			
		JSONArray jArr = jObj.getJSONArray(FAILED_JSON);			
		for(int i=0; i<jArr.length(); i++){
			Const.mFailedTicketsItems.add(jArr.getJSONObject(i).getInt("Id"));
		}
	}
}
