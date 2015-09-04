package com.mysentosa.android.sg.asynctask;

import org.json.JSONObject;

import android.content.Context;
import android.os.AsyncTask;

import com.mysentosa.android.sg.utils.Const;
import com.mysentosa.android.sg.utils.CustomCallback;
import com.mysentosa.android.sg.utils.HttpHelper;

public class TermAndConditionAsyncTask extends AsyncTask<Void, Void, Boolean> {

	private static final String DATA_JSON = "Data";
	private static final String HTML_JSON = "HTML";
	private static final String CODE_JSON = "StatusCode";
	private static final String TERMS = "TermsCondition";
	CustomCallback mListener;

	public TermAndConditionAsyncTask(Context context, CustomCallback mListener) {
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
					HttpHelper.BASE_ADDRESS + TERMS, null);					
			JSONObject rootObject = new JSONObject(result);
			if (rootObject.getInt(CODE_JSON) == 0) {
				JSONObject dataObject = rootObject.optJSONObject(DATA_JSON);
				updatePackage(dataObject);
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

	private void updatePackage(JSONObject rootObject) throws Exception {		
		Const.sbTerms = new StringBuilder();
		Const.sbTerms.append(rootObject.getString(HTML_JSON));
	}
}
