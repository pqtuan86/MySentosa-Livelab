package com.mysentosa.android.sg.asynctask;

import java.util.ArrayList;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;

import android.content.Context;
import android.os.AsyncTask;

import com.mysentosa.android.sg.utils.CustomCallback;
import com.mysentosa.android.sg.utils.HttpHelper;

public class GetPromocodeAsyncTask extends AsyncTask<Void, Void, Boolean> {

	private static final String CODE_JSON = "StatusCode";
	private static final String EVENT_TICKETS = "EventTickets/Detail";
	private static final String EVENT_ID = "eventId";
	private static final String EVENT_PRMO = "promoCode";
	CustomCallback mListener;
	String id;
	String promoCode;
	
	public GetPromocodeAsyncTask(Context context, CustomCallback mListener, String id, String promocode) {
		this.mListener = mListener;
		this.id = id;
		this.promoCode = promocode;			
	}

	@Override
	protected void onPreExecute() {
		super.onPreExecute();
	}

	@Override
	protected Boolean doInBackground(Void... arg0) {
		try {
			
			ArrayList<NameValuePair> nameValue = new ArrayList<NameValuePair>(2);
			nameValue.add(new BasicNameValuePair(EVENT_ID, id));
			nameValue.add(new BasicNameValuePair(EVENT_PRMO, promoCode));
			String result = HttpHelper.sendRequestUsingPost(
					HttpHelper.BASE_ADDRESS + EVENT_TICKETS, nameValue);		
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
		mListener.isFnished(result);
	}

}
