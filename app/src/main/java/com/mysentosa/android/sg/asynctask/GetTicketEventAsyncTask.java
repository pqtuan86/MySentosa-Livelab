package com.mysentosa.android.sg.asynctask;

import org.json.JSONObject;

import android.content.Context;
import android.os.AsyncTask;

import com.mysentosa.android.sg.utils.CustomCallback;
import com.mysentosa.android.sg.utils.HttpHelper;

public class GetTicketEventAsyncTask extends AsyncTask<Void, Void, Boolean> {

	private static final String CODE_JSON = "StatusCode";
	private static final String EVENT_TICKETS = "EventTickets";
	CustomCallback mListener;
	
	public GetTicketEventAsyncTask(Context context, CustomCallback mListener) {
		this.mListener = mListener;
	}

	@Override
	protected void onPreExecute() {
		super.onPreExecute();
	}

	@Override
	protected Boolean doInBackground(Void... arg0) {
		try {
			String result = HttpHelper.sendRequestUsingGet(
					HttpHelper.BASE_ADDRESS + EVENT_TICKETS, null);		
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
