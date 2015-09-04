package com.mysentosa.android.sg.asynctask;

import java.lang.ref.WeakReference;

import org.json.JSONObject;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.util.Log;
import android.view.Window;

import com.mysentosa.android.sg.R;
import com.mysentosa.android.sg.TicketSelectionActivity;
import com.mysentosa.android.sg.TicketSelectionEventActivity;
import com.mysentosa.android.sg.models.TicketItem;
import com.mysentosa.android.sg.utils.AlertHelper;
import com.mysentosa.android.sg.utils.Const;
import com.mysentosa.android.sg.utils.CustomCallback;
import com.mysentosa.android.sg.utils.HttpHelper;
import com.mysentosa.android.sg.utils.JSONParser;
import com.mysentosa.android.sg.utils.LogHelper;

public class GetPackagesAsyncTask extends AsyncTask<Void, Void, Boolean> {

	private static final String DATA_JSON = "Data";
	private static final String CODE_JSON = "StatusCode";

	private static final String PACKAGE_TICKETS = "PackageTickets";
	private static final String PACKAGE_JSON = "Packages";

	private static final String EVENT_TICKETS = "EventTickets";
	private static final String EVENT_JSON = "Events";

	private static final String ATTRACTION_TICKETS = "AttractionTickets";
	private static final String ATTRACTION_JSON = "Attractions";

	ProgressDialog pd;
	CustomCallback mListener;
	int code;
	
	private int mStatusCode;
//	private AlertDialog mKillSwitchAlert;
	private WeakReference<Activity> mActivityRef;

	public GetPackagesAsyncTask(Activity context, CustomCallback mListener,
			int code) {
		this.mListener = mListener;
		this.code = code;
		pd = new ProgressDialog(context);
		pd.setCancelable(false);
		pd.requestWindowFeature(Window.FEATURE_NO_TITLE);
		pd.setMessage("Loading..");
		
		mActivityRef = new WeakReference<Activity>(context);
//		mKillSwitchAlert = AlertHelper.createKillSwitchAlert(context, R.string.error_ticketing_kill_switch);
	}

	@Override
	protected void onPreExecute() {
		super.onPreExecute();
		if (pd != null && isActivityAlive())
			pd.show();
	}

	@Override
	protected Boolean doInBackground(Void... arg0) {
		try {
			if (code == Const.PURCHASE_TICKET_TYPE_CODE) {
				return true;
			} else {
				String result = HttpHelper.sendRequestUsingGet(
						HttpHelper.BASE_ADDRESS + getAPIName(code), null);
				LogHelper.i("tructran", HttpHelper.BASE_ADDRESS + getAPIName(code));
				JSONObject rootObject = new JSONObject(result);
				mStatusCode = rootObject.getInt(CODE_JSON);
				if (mStatusCode == 0) {
					JSONObject dataObject = rootObject.optJSONObject(DATA_JSON);
					updatePackage(dataObject);
				} else {
					return false;
				}
				return true;
			}
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	@Override
	protected void onPostExecute(Boolean result) {
		super.onPostExecute(result);
		if (pd != null && pd.isShowing() && isActivityAlive()) {
			pd.dismiss();
		}
		mListener.isFnished(result);
		
		if (mStatusCode == -1 && mActivityRef.get() != null)
			AlertHelper.showKillSwitchAlert(mActivityRef.get(), R.string.error_ticketing_kill_switch);
	}

	private void updatePackage(JSONObject rootObject) throws Exception {
		Const.mTicketsItems.clear();
		TicketItem[] ticketsResponse = JSONParser.getResponse(
				TicketItem[].class,
				new String(rootObject.getJSONArray(getJsonArrayName(code))
						.toString()));
		if (ticketsResponse != null) {
			for (TicketItem tickets : ticketsResponse) {
				Const.mTicketsItems.add(tickets);
			}
		}
	}

	public String getJsonArrayName(int code) {
		switch (code) {
		case 0:
			return PACKAGE_JSON;
		case 1:
			return ATTRACTION_JSON;
		case 2:
			return EVENT_JSON;
		default:
			return "";
		}
	}

	public String getAPIName(int code) {
		switch (code) {
		case 0:
			return PACKAGE_TICKETS;
		case 1:
			return ATTRACTION_TICKETS;
		case 2:
			return EVENT_TICKETS;
		default:
			return "";
		}
	}
	
	public Boolean isActivityAlive() {
		Activity a = mActivityRef.get();
		return 	(a != null) && 
				(a instanceof TicketSelectionEventActivity) && 
				((TicketSelectionActivity) a).isAlive();
	}
}
