package com.mysentosa.android.sg.asynctask;

import java.lang.ref.WeakReference;

import org.json.JSONObject;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.view.Window;

import com.mysentosa.android.sg.R;
import com.mysentosa.android.sg.models.TicketEventDetailItem;
import com.mysentosa.android.sg.utils.AlertHelper;
import com.mysentosa.android.sg.utils.Const;
import com.mysentosa.android.sg.utils.CustomCallback;
import com.mysentosa.android.sg.utils.HttpHelper;
import com.mysentosa.android.sg.utils.JSONParser;

public class GetTicketEventsDetailAsyncTask extends
		AsyncTask<Void, Void, Boolean> {

	private static final String DATA_JSON = "Data";
	private static final String CODE_JSON = "StatusCode";

	private static final String EVENT_TICKETS = "EventTickets/Detail/";
//	private static final String EVENT_JSON = "EventDetails";
//	private static final String EVENT_ITEM_JSON = "Items";

	ProgressDialog pd;
	CustomCallback mListener;
	int code;
	int position;
	
	private int mStatusCode;
	private WeakReference<Activity> mActivityRef;

	public GetTicketEventsDetailAsyncTask(Activity context,
			CustomCallback mListener, int code, int position) {
		this.mListener = mListener;
		this.code = code;
		this.position = position;
		pd = new ProgressDialog(context);
		pd.setCancelable(false);
		pd.requestWindowFeature(Window.FEATURE_NO_TITLE);
		pd.setMessage("Loading..");
		
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
			String result = HttpHelper.sendRequestUsingGet(
					HttpHelper.BASE_ADDRESS + EVENT_TICKETS + position, null);			
			JSONObject rootObject = new JSONObject(result);
			mStatusCode = rootObject.getInt(CODE_JSON);
			if (mStatusCode == 0) {
				JSONObject dataObject = rootObject.optJSONObject(DATA_JSON);
				updatePackage(dataObject);
			} else {
				return false;
			}
			return true;
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

		TicketEventDetailItem ticketsResponse = JSONParser.getResponse(
				TicketEventDetailItem.class, new String(rootObject.toString()));
		Const.mTicketsEventdetailItems.add(ticketsResponse);

	}
}
