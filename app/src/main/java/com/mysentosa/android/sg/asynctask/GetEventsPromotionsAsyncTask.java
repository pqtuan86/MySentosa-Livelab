package com.mysentosa.android.sg.asynctask;

import org.json.JSONObject;

import android.content.ContentResolver;
import android.content.Context;
import android.os.AsyncTask;

import com.mysentosa.android.sg.provider.SentosaContentProvider;
import com.mysentosa.android.sg.provider.utils.JSONParseUtil;
import com.mysentosa.android.sg.provider.utils.SentosaDatabaseStructure;
import com.mysentosa.android.sg.provider.utils.SentosaDatabaseStructure.ContentURIs;
import com.mysentosa.android.sg.utils.HttpHelper;

public class GetEventsPromotionsAsyncTask extends AsyncTask<Void, Void, Boolean> {

	private static final String EVENTS_PROMOTIONS_BANNERS = "EventsPromotionsBanners";	
	private static final String DATA_JSON = "Data";	
	private static final String EVENTS_JSON = "Events";
	private static final String PROMOTIONS_JSON = "Promotions";	

	private ContentResolver mResolver;
	private JSONParseUtil jsonParser;

	public GetEventsPromotionsAsyncTask(Context context){
		mResolver = context.getContentResolver();
		jsonParser  = new JSONParseUtil(context);
	}

	@Override
	protected void onPreExecute() {
		super.onPreExecute();
	}


	@Override
	protected Boolean doInBackground(Void... arg0) {
		try {
			String result = HttpHelper.sendRequestUsingGet(HttpHelper.BASE_ADDRESSV2 +  EVENTS_PROMOTIONS_BANNERS, null);
			JSONObject rootObject = new JSONObject(result);
			JSONObject dataObject = rootObject.optJSONObject(DATA_JSON);
			updateEvents(dataObject);
			updatePromotions(dataObject);
			//saveLastUpdateTime(rootObject);
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}		
	}

	@Override
	protected void onPostExecute(Boolean result) {
		super.onPostExecute(result);
	}

	private void updateEvents(JSONObject rootObject) throws Exception{
		//deleteStaleEvent (rootObject);
		deleteCachedEvents();
		jsonParser.parsingEventPromoJsonData(rootObject.optJSONArray(EVENTS_JSON),true);
	}

	private void updatePromotions(JSONObject rootObject) throws Exception{
		//deleteStalePromotion (rootObject);
		deleteCachedPromotions();
		jsonParser.parsingEventPromoJsonData(rootObject.optJSONArray(PROMOTIONS_JSON),false);
	}
	
	private void deleteCachedEvents() {
		//SentosaApplication.appInstance.mImageCache.clearCaches();
		SentosaContentProvider cp = (SentosaContentProvider)mResolver.acquireContentProviderClient(ContentURIs.EVENTS_URI).getLocalContentProvider();
		cp.getDBHandle().delete(SentosaDatabaseStructure.TABLE_EVENTS, null, null);
	}
	
	private void deleteCachedPromotions() {
		SentosaContentProvider cp = (SentosaContentProvider)mResolver.acquireContentProviderClient(ContentURIs.PROMOTIONS_URI).getLocalContentProvider();
		cp.getDBHandle().delete(SentosaDatabaseStructure.TABLE_PROMOTIONS, null, null);
	}

}
