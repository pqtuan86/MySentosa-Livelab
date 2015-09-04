package com.mysentosa.android.sg.asynctask;

import java.text.SimpleDateFormat;
import java.util.Date;

import android.content.ContentResolver;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Log;

import com.mysentosa.android.sg.provider.SentosaContentProvider;
import com.mysentosa.android.sg.provider.utils.JSONParseUtil;
import com.mysentosa.android.sg.provider.utils.SentosaDatabaseStructure.ContentURIs;
import com.mysentosa.android.sg.utils.HttpHelper;
import com.mysentosa.android.sg.utils.LogHelper;
import com.mysentosa.android.sg.utils.SentosaUtils;

public class GetNodesEdgesAsyncTask extends AsyncTask<Void, Void, Boolean> {

	private static final String NODES_JSON = "/Content/Settings/Android/v1/nodes.json";	
	private static final String EDGES_JSON = "/Content/Settings/Android/v1/edges.json";
	private static final String DETAILS_JSON = "/Content/Settings/RAW/v1/details.json";	
	private static final String THINGS_TO_DO_JSON = "/API/GroupCategories";	
	private ContentResolver mResolver;
	private Context context;
	private SharedPreferences pref;
	private SharedPreferences.Editor editor;

	private JSONParseUtil jsonParser;
	private SimpleDateFormat sdf;
	private static final String NODES_LAST_MODIFIED_TIME = "NODES_LAST_MODIFIED_TIME_JSON", EDGES_LAST_MODIFIED_TIME = "EDGES_LAST_MODIFIED_TIME", THINGS_TO_DO_LAST_CHECKED = "THINGS_TO_DO_LAST_CHECKED", DETAILS_LAST_MODIFIED_TIME = "DETAILS_LAST_MODIFIED_TIME";

	private long nodesLastModifiedTime = 0l;
	private long edgesLastModifiedTime = 0l;
	private long detailsLastModifiedTime = 0l;

	public GetNodesEdgesAsyncTask(Context context){
		mResolver = context.getContentResolver();
		jsonParser = new JSONParseUtil(context);
		pref = context.getSharedPreferences(SentosaContentProvider.DB_PREFS, Context.MODE_PRIVATE);
		sdf = SentosaUtils.getLastModifiedFormatter();
		editor = pref.edit();
		this.context = context;
	}

	@Override
	protected void onPreExecute() {
		super.onPreExecute();
	}


	@Override
	protected Boolean doInBackground(Void... arg0) {
		boolean retVal = true;
		SentosaContentProvider cp = (SentosaContentProvider)mResolver.acquireContentProviderClient(ContentURIs.SENTOSA_URI).getLocalContentProvider();
		String data = null;
		try {
			data = this.getResponseFromServer(NODES_JSON,NODES_LAST_MODIFIED_TIME);
			if(data != null) {
				if(jsonParser.parseNodesJSON(data) != 0) {
					if(cp.updateTableNodes()) {
						editor.putLong(NODES_LAST_MODIFIED_TIME, nodesLastModifiedTime);
						editor.commit();
					}
				}
			} 
		} catch (Exception e) {
			e.printStackTrace();
			retVal = false;
		}

		try {
			data = this.getResponseFromServer(EDGES_JSON,EDGES_LAST_MODIFIED_TIME);
			if(data != null) {
				if(jsonParser.parseEdgesJSON(data) != 0) {
					if(cp.updateTableEdges()) {
						editor.putLong(EDGES_LAST_MODIFIED_TIME, edgesLastModifiedTime);
						editor.commit();
					}
				}
			}
			LogHelper.d(" testing","testing edges json finished processing");
		} catch (Exception e) {
			LogHelper.e(" testing","testing edges json had error");
			e.printStackTrace();
			retVal = false;
		}

		try {
			data = this.getResponseFromServer(DETAILS_JSON,DETAILS_LAST_MODIFIED_TIME);
			if(data != null) {
				if(jsonParser.parseNodeDetailsJSON(data) != 0) {
					if(cp.updateTableDetails()) {
						editor.putLong(DETAILS_LAST_MODIFIED_TIME, detailsLastModifiedTime);
						editor.commit();
					}
				}
			} 
		} catch (Exception e) {
			e.printStackTrace();
			retVal = false;
		}

		try {
			data = this.getResponseFromServer(THINGS_TO_DO_JSON,THINGS_TO_DO_LAST_CHECKED);
			if(data != null) {
				if(jsonParser.parseThingsToDoJSON(data) != 0) {
					editor.putLong(THINGS_TO_DO_LAST_CHECKED, System.currentTimeMillis());
					editor.commit();
					LogHelper.e(" testing","testing things to do updated at 3 "+ System.currentTimeMillis());
				}
			} else 
				LogHelper.e(" testing","testing things to do data is null 4");
		} catch (Exception e) {
			LogHelper.e(" testing","testing things to do data is exception 5");
			e.printStackTrace();
			return false;
		}
		//			SentosaContentProvider.copyDatabase(context);
		LogHelper.e(" testing","testing things to do data post");
		return retVal;
	}


	private String getResponseFromServer(String suffix,String type) throws Exception {
		String result = null;
		if(type == THINGS_TO_DO_LAST_CHECKED) {
			long lastModified = pref.getLong(type, 0);
			long currentTimeMillis = System.currentTimeMillis();
			if(((currentTimeMillis-lastModified)/3600000)>=24) {
				String requestUri = HttpHelper.BASE_HOST + suffix ;
				result =  HttpHelper.sendCustomRequestUsingGet(requestUri, sdf.format(new Date(lastModified)));
			}
		} else {
			long lastModified = pref.getLong(type, 0);
			String requestUri = HttpHelper.BASE_HOST + suffix ;
			result =  HttpHelper.sendCustomRequestUsingGet(requestUri, sdf.format(new Date(lastModified)));
			if(result != null) {
				if(type == NODES_LAST_MODIFIED_TIME)
					nodesLastModifiedTime = System.currentTimeMillis();
				else if (type == EDGES_LAST_MODIFIED_TIME)
					edgesLastModifiedTime = System.currentTimeMillis();
				else if (type == DETAILS_LAST_MODIFIED_TIME)
					detailsLastModifiedTime = System.currentTimeMillis();			
			}	
		}
		return result;
	}

}
