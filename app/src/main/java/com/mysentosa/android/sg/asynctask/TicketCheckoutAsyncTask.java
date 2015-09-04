package com.mysentosa.android.sg.asynctask;

import java.util.ArrayList;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.view.Window;

import com.mysentosa.android.sg.models.FailedItems;
import com.mysentosa.android.sg.provider.utils.SentosaDatabaseStructure.CartData;
import com.mysentosa.android.sg.provider.utils.SentosaDatabaseStructure.ContentURIs;
import com.mysentosa.android.sg.provider.utils.SentosaDatabaseStructure.Queries;
import com.mysentosa.android.sg.utils.Const;
import com.mysentosa.android.sg.utils.CustomCallback;
import com.mysentosa.android.sg.utils.HttpHelper;
import com.mysentosa.android.sg.utils.JSONParser;
import com.mysentosa.android.sg.utils.SHAUtils;
import com.mysentosa.android.sg.utils.SentosaUtils;

public class TicketCheckoutAsyncTask extends AsyncTask<Void, Void, Boolean> {

	private static final String CODE_JSON = "StatusCode";
	private static final String DATA_JSON = "Data";
	private static final String ITEM_JSON = "items";
	private static final String FAILED_JSON = "FailedIds";
	private static final String CHECKOUT_TICKETS = "ShoppingCart/CheckOut";
	CustomCallback mListener;
	SharedPreferences sharePref;
	String tokenHash;
	Context mContext;
	ProgressDialog pd;
	String timeStamp;

	public TicketCheckoutAsyncTask(Context context, CustomCallback mListener) {
		this.mListener = mListener;
		sharePref = PreferenceManager.getDefaultSharedPreferences(context);
		mContext = context;
		pd = new ProgressDialog(context);
		pd.setCancelable(false);
		pd.requestWindowFeature(Window.FEATURE_NO_TITLE);
		pd.setMessage("Checking out..");
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

	        if (SentosaUtils.isUserLogined(mContext)) {
	            nameValue.add(new BasicNameValuePair("islanderID", SentosaUtils.getMemberID(mContext)+""));
	            nameValue.add(new BasicNameValuePair("accessToken", SentosaUtils.getAccessToken(mContext)));
	        }
	            
			Cursor cursor = mContext.getContentResolver().query(
					ContentURIs.CART_URI, null,
					Queries.GET_SHOPPING_CART_ITEM_QUERY, null, Const.MANUAL);
			if (cursor.getCount() > 0) {
				int i = 0;
				while (cursor.moveToNext()) {
					nameValue.add(new BasicNameValuePair(ITEM_JSON + "[" + i
							+ "][Id]", cursor.getString(cursor
							.getColumnIndex(CartData.CART_DETAIL_ID_COL))));
					nameValue.add(new BasicNameValuePair(ITEM_JSON + "[" + i
							+ "][TicketType]", getTicketType(cursor
							.getInt(cursor
									.getColumnIndex(CartData.CART_TYPE_COL)))));
					nameValue.add(new BasicNameValuePair(ITEM_JSON + "[" + i
							+ "][Quantity]", cursor.getString(cursor
							.getColumnIndex(CartData.CART_QTY_COL))));
					i++;
				}
			}
			cursor.close();

			String result = HttpHelper.sendCustomRequestUsingPost(
					HttpHelper.BASE_ADDRESS + CHECKOUT_TICKETS, nameValue);

			JSONObject rootObject = new JSONObject(result);
			JSONObject dataObject = rootObject.optJSONObject(DATA_JSON);
			if (rootObject.getInt(CODE_JSON) == 0) {
				updatePackage(dataObject);
				return true;
			} else {
				updateFailedPackage(dataObject);
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

	private String getTicketType(int type) {
		String ticketType = null;

		switch (type) {
		case 0:
			ticketType = "Package";
			break;
		case 1:
			ticketType = "Attraction";
			break;
		case 2:
			ticketType = "Event";
			break;

		default:
			break;
		}
		return ticketType;
	}

	private void updatePackage(JSONObject rootObject) throws Exception {
		Editor edit = sharePref.edit();
		edit.putString("SessionId", rootObject.getString("SessionId"));
		edit.putString("RefId", rootObject.getString("RefId"));
		edit.putString("PaymentUrl", rootObject.getString("PaymentUrl"));
		edit.putString("timeStamp", timeStamp);
		edit.putString("tokenHash", tokenHash);
		edit.commit();
	}

	private void updateFailedPackage(JSONObject rootObject) throws Exception {

		FailedItems[] ticketsResponse = JSONParser.getResponse(
				FailedItems[].class,
				new String(rootObject.getJSONArray(FAILED_JSON).toString()));
		if (ticketsResponse != null) {
			for (FailedItems tickets : ticketsResponse) {
				Const.mFailedTicketsCheckoutItems.add(tickets);
			}
		}
	}

}
