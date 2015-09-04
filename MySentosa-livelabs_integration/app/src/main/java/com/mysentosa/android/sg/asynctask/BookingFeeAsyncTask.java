package com.mysentosa.android.sg.asynctask;

import org.json.JSONObject;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.view.Window;

import com.mysentosa.android.sg.utils.Const;
import com.mysentosa.android.sg.utils.CustomCallback;
import com.mysentosa.android.sg.utils.HttpHelper;

public class BookingFeeAsyncTask extends AsyncTask<Void, Void, Boolean> {

	private static final String DATA_JSON = "Data";
	private static final String BOOKING_JSON = "BookingFee";
	private static final String CODE_JSON = "StatusCode";
	private static final String BOOKING = "BookingFee";
	CustomCallback mListener;
	ProgressDialog pd;
	
	public BookingFeeAsyncTask(Context context, CustomCallback mListener) {
		this.mListener = mListener;
		pd = new ProgressDialog(context);
		pd.setCancelable(false);
		pd.requestWindowFeature(Window.FEATURE_NO_TITLE);
		pd.setMessage("Retrieving Booking Fee");
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
					HttpHelper.BASE_ADDRESS + BOOKING, null);			
			JSONObject rootObject = new JSONObject(result);
			if (rootObject.getInt(CODE_JSON) == 0) {
				JSONObject dataObject = rootObject.optJSONObject(DATA_JSON);
				updatePackage(dataObject);
				return true;
			} else {
				Const.mBookingFees = -1;
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

	private void updatePackage(JSONObject rootObject) throws Exception {	
		Const.mBookingFees = rootObject.getLong(BOOKING_JSON);	}
}
