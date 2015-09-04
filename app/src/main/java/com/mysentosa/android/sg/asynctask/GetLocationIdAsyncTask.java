package com.mysentosa.android.sg.asynctask;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.AsyncTask;

import com.mysentosa.android.sg.utils.Const;
import com.urbanairship.push.PushManager;

public class GetLocationIdAsyncTask extends AsyncTask<Void, Void, Void> {

	SharedPreferences sp;
	int value;
	String title;
	int locationId;
	private Context mContext;
	public static final String TAG_PREFS = "com.mysentosa.android.sg.tag.prefs";

	public GetLocationIdAsyncTask(Context context, String title) {
		mContext = context;	
		sp = context.getSharedPreferences(TAG_PREFS, context.MODE_PRIVATE);
		value = sp.getInt(title, 0);
		this.title = title;
	}

	@Override
	protected void onPreExecute() {
		super.onPreExecute();
//		Toast.makeText(mContext, "APID: " + PushManager.shared().getAPID(),
//				Toast.LENGTH_LONG).show();
	}

	@Override
	protected Void doInBackground(Void... arg0) {

		if (value == 0) {
			Editor ed = sp.edit();
			ed.putInt(title, 1);
			ed.commit();
		} else if (value == 1) {
			for (int i = 0; i < Const.mLocationItem.size(); i++) {
				if (Const.mLocationItem.get(i).getTitle().equals(title)) {
					locationId = Const.mLocationItem.get(i).getId();
					break;
				}
			}
			//LogHelper.i("TAG", "locationId: " + locationId);

			Editor ed = sp.edit();
			ed.putInt(title, 2);
			ed.putInt(title + "_2", locationId);
			ed.commit();

			Set<String> tags = new HashSet<String>();
			//tags.add("location_" + locationId);
			Map<String, ?> keys = sp.getAll();
			for (Map.Entry<String, ?> entry : keys.entrySet()) {
				if (entry.getValue().toString().equalsIgnoreCase("2")) {
					tags.add("location_" + sp.getInt(entry.getKey() + "_2", 0));
				}
			}

			PushManager.shared().setTags(tags);
		}
		return null;
	}

	@Override
	protected void onPostExecute(Void result) {
		super.onPostExecute(result);		
	}

}
