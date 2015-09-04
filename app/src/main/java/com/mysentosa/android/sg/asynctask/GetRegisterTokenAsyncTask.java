package com.mysentosa.android.sg.asynctask;

import java.util.ArrayList;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;

import com.mysentosa.android.sg.utils.HttpHelper;
import com.mysentosa.android.sg.utils.SentosaUtils;

public class GetRegisterTokenAsyncTask extends AsyncTask<Void, Void, Void> {

	private static final String CODE_JSON = "StatusCode";
	private static final String DATA_JSON = "Data";
	private static final String RETRIEVE_TOKEN_TICKETS = "Devices/GetToken";
	SharedPreferences sharedPrefs;
	
	public GetRegisterTokenAsyncTask(Context context) {
		 sharedPrefs = PreferenceManager
                .getDefaultSharedPreferences(context);
 
	}

	@Override
	protected void onPreExecute() {
		super.onPreExecute();
	}

	@Override
	protected Void doInBackground(Void... arg0) {
		try {
			ArrayList<NameValuePair> pairs = new ArrayList<NameValuePair>();
			pairs.add(new BasicNameValuePair("udid", "android"
					+ SentosaUtils.getDeviceID()));
			String result = HttpHelper.sendRequestUsingPut(
					HttpHelper.BASE_ADDRESS + RETRIEVE_TOKEN_TICKETS, pairs);
		
			JSONObject rootObject = new JSONObject(result);
			if (rootObject.getInt(CODE_JSON) == 0) {
				JSONObject dataObject = rootObject.optJSONObject(DATA_JSON);
				updatePackage(dataObject);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	protected void onPostExecute(Void result) {
		super.onPostExecute(result);
	}
	

	private void updatePackage(JSONObject rootObject) throws Exception {	
		Editor edit = sharedPrefs.edit();
		edit.putString("Device_Id", rootObject.getString("Id"));
		edit.putString("Token", rootObject.getString("Token"));
		edit.putString("Point", rootObject.getString("Point"));
		edit.putString("UpdatedAt", rootObject.getString("UpdatedAt"));
		edit.commit(); 
	}

}
