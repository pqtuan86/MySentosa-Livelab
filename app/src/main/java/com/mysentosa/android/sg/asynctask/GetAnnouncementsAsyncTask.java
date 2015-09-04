package com.mysentosa.android.sg.asynctask;

import java.util.ArrayList;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONObject;
import org.osmdroid.util.GeoPoint;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.mysentosa.android.sg.MapActivity;
import com.mysentosa.android.sg.location.LocationFinder;
import com.mysentosa.android.sg.location.LocationNotifier;
import com.mysentosa.android.sg.location.PlacesConstants;
import com.mysentosa.android.sg.provider.SentosaContentProvider;
import com.mysentosa.android.sg.utils.Const;
import com.mysentosa.android.sg.utils.HttpHelper;
import com.mysentosa.android.sg.utils.LogHelper;

public class GetAnnouncementsAsyncTask extends AsyncTask<Void, Void, Boolean> {

	private static final String DATA_JSON = "Data";
	private static final String ANNOUNCEMENTS_JSON = "Announcements";

	private static final String ACCESSING_TIME_JSON = "AccessingTime";
	private static final String ID_JSON = "Id";
	private static final String CONTENT_JSON = "Content";
	private static final String FLAG_JSON = "OnlyInSentosa";

	private boolean flag;
	private SharedPreferences pref;
	private long accessingTime;
	private String announcementMessage = null;
	Context context;
	private boolean isDisplayed = false;
	
	public GetAnnouncementsAsyncTask(Context context) {
		this.context = context;
		pref = context.getSharedPreferences(SentosaContentProvider.DB_PREFS,
				Context.MODE_PRIVATE);
	}

	@Override
	protected void onPreExecute() {
		super.onPreExecute();
	}

	@Override
	protected Boolean doInBackground(Void... arg0) {
		try {
			String result = this.getResponseFromServer();
			JSONObject rootObject = new JSONObject(result);
			accessingTime = rootObject.optLong(ACCESSING_TIME_JSON);
			JSONObject dataObject = rootObject.optJSONObject(DATA_JSON);
			JSONArray announcements = dataObject
					.optJSONArray(ANNOUNCEMENTS_JSON);
			if (announcements.length() > 0) {
				// just fetching contents of 1st announcement in array
				announcementMessage = ((JSONObject) announcements.get(0))
						.optString(CONTENT_JSON);
				flag = ((JSONObject) announcements.get(0))
						.optBoolean(FLAG_JSON);
			}

			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	private LocationFinder locationFinder;

	@Override
	protected void onPostExecute(Boolean result) {
		super.onPostExecute(result);
		if (result) {
			if (!flag)
				displayAnnoucement();
			else {
				locationFinder = new LocationFinder(context,
						new LocationNotifier() {
							@Override
							public void updatedLocation(Location location) {
								LogHelper.d(" test"," test location update");
								if (location != null) {
									GeoPoint gp = new GeoPoint(location.getLatitude(), location.getLongitude());
									if (MapActivity.BOUNDING_BOX.contains(gp)) {
										locationFinder.disableLocationUpdates();
										displayAnnoucement();
									}
								}
							}

							@Override
							public void listenersDisabled() {
								Toast.makeText(context, "you are not within Sentosa", 1000).show();
							}
						}, PlacesConstants.MAX_TIME_TO_GET_LOCATION);
				locationFinder.requestLocationUpdates();
			}

		}

	}

	private void displayAnnoucement() {
		if (announcementMessage != null && announcementMessage.length() > 0 && !isDisplayed) {
			AlertDialog.Builder builder = new AlertDialog.Builder(context);
			builder.setMessage(announcementMessage)
					.setTitle("Announcement")
					.setCancelable(false)
					.setPositiveButton("Ok",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int id) {
									dialog.cancel();
								}
							});

			AlertDialog alert = builder.create();
			try {
				alert.show();
				isDisplayed = true;
				saveLastUpdateTime();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	private String getResponseFromServer() throws Exception {

		String requestUri = HttpHelper.BASE_ADDRESS + "Announcements";
		LogHelper.d("2359", "Calling server:" + requestUri);
		ArrayList<NameValuePair> params = null;
		long last_update;
		if ((last_update = pref.getLong(
				Const.LAST_ANNOUNCEMENTS_RETRIEVAL_TIME, 0L)) > 0) {
			params = new ArrayList<NameValuePair>();
			NameValuePair nvp = new BasicNameValuePair(
					Const.LAST_RETRIEVAL_TIME, last_update + "");
			params.add(nvp);
			LogHelper.d("2359", "added parameter:" + last_update);
		}

		String result = HttpHelper.sendRequestUsingGet(requestUri, params);
		LogHelper.d("2359", "Server Response:" + result);
		return result;
	}

	private void saveLastUpdateTime() {
		SharedPreferences.Editor editor = pref.edit();
		LogHelper.d("2359", "Saving last access time:" + accessingTime);
		editor.putLong(Const.LAST_ANNOUNCEMENTS_RETRIEVAL_TIME, accessingTime);
		editor.commit();
	}

}
