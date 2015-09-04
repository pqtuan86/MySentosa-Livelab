package com.mysentosa.android.sg.asynctask;

import org.json.JSONObject;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.mysentosa.android.sg.models.LocationItem;
import com.mysentosa.android.sg.utils.Const;
import com.mysentosa.android.sg.utils.HttpHelper;
import com.mysentosa.android.sg.utils.JSONParser;

public class GetLocationsAsyncTask extends AsyncTask<Void, Void, Void> {

	private static final String DATA_JSON = "Data";
	private static final String CODE_JSON = "StatusCode";
	private static final String LOCATION_JSON = "Locations";
	private static final String API = "Locations";

	public GetLocationsAsyncTask(Context context) {	
	}

	@Override
	protected void onPreExecute() {
		super.onPreExecute();
	}

	@Override
	protected Void doInBackground(Void... arg0) {
		try {

			String result = HttpHelper.sendRequestUsingGet(
					HttpHelper.BASE_ADDRESSV2 + API, null);
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
		LocationItem[] ticketsResponse = JSONParser.getResponse(
				LocationItem[].class,
				new String(rootObject.getJSONArray(LOCATION_JSON).toString()));
		if (ticketsResponse != null) {
			for (LocationItem tickets : ticketsResponse) {
				Const.mLocationItem.add(tickets);
			}
		}
		
	}

}
