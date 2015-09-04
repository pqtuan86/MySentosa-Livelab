// Created by plusminus on 21:46:22 - 25.09.2008
package com.mysentosa.android.sg.utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.util.Log;

import com.mysentosa.android.sg.map.models.Edge;
import com.mysentosa.android.sg.provider.utils.SentosaDatabaseStructure;

public class PreloadedDatabaseBuilder  extends AsyncTask<Void, Void, Boolean> {
	//This class is basically meant to parse all the json files for nodes, node details, edges, itineraries
	//Towards the end, before release, the json data can be removed from assets and substituted by the database directly
	private Context c;
	private SQLiteDatabase db;

	public PreloadedDatabaseBuilder(Context c, SQLiteDatabase db) {
		this.c = c;
		this.db = db;
	}

	@Override
	protected Boolean doInBackground(Void... params) {
		try {
			parseNodesJSON();
			parseEdgesJSON();
			parseNodeDetailsJSON();
			parseThingsToDoJSON();
//			Log.d(" testing"," testing preload db creatation done");
//			SentosaContentProvider.copyDatabase(c);

		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return true;
	}

//	private void copyDB() {
//		try {
//			File sd = Environment.getExternalStorageDirectory();
//			File data = Environment.getDataDirectory();
//
//			if (sd.canWrite()) {
//				String currentDBPath = "\\data\\com.mysentosa.android.sg\\databases\\sentosadb.db";
//				String backupDBPath = "sentosadb.db";
//				File currentDB = new File(data, currentDBPath);
//				File backupDB = new File(sd, backupDBPath);
//
//				if (currentDB.exists()) {
//					FileChannel src = new FileInputStream(currentDB).getChannel();
//					FileChannel dst = new FileOutputStream(backupDB).getChannel();
//					dst.transferFrom(src, 0, src.size());
//					src.close();
//					dst.close();
//				}
//			}
//		} catch (Exception e) {
//		}
//	}

	private void parseNodesJSON() throws JSONException {
		JSONArray nodesData = new JSONArray(SentosaUtils.getDataFromFileInAssetsToString("nodes.json",c));
		for(int i=0; i<nodesData.length(); i++){
			JSONObject nodeObj = nodesData.optJSONObject(i);
			int ID_COL = nodeObj.optInt("nodeId");  
			Double LATITUDE_COL = nodeObj.optDouble("lat");
			Double LONGITUDE_COL = nodeObj.optDouble("lon");
			String TITLE_COL = nodeObj.optString("title");

			db.execSQL("INSERT INTO "+ SentosaDatabaseStructure.TABLE_NODES +
					" VALUES("+ID_COL+", "+LATITUDE_COL+","+LONGITUDE_COL+", \""+ TITLE_COL +"\");");
		}
	}

	private void parseEdgesJSON() throws JSONException {
		JSONArray edgesData = new JSONArray(SentosaUtils.getDataFromFileInAssetsToString("edges.json",c));
		for(int i=0; i<edgesData.length(); i++){
			JSONObject edgeObj = edgesData.optJSONObject(i);
			int ID_COL = edgeObj.optInt("edgeId");  
			int FROM_NODE_COL = edgeObj.optInt("fromNode");
			int TO_NODE_COL = edgeObj.optInt("toNode");
			int TIME_COL = edgeObj.optInt("time");
			String type = edgeObj.optString("type");
			String line_color = "";
			if(!type.equals("WALK"))
				line_color = edgeObj.optString("lineColor");
			int TYPE_COL = this.getEdgeType(type, line_color);

			int BIDIRECTIONAL_COL = edgeObj.optBoolean("biDirectional")?1:0;

			db.execSQL("INSERT INTO "+ SentosaDatabaseStructure.TABLE_EDGES +
					" VALUES("+ID_COL+", "+FROM_NODE_COL+", "+ TO_NODE_COL +
					", "+TIME_COL+", \""+TYPE_COL+"\", "+BIDIRECTIONAL_COL+");");


			if(TYPE_COL!=Edge.TYPE_WALK && TYPE_COL!=Edge.TYPE_WAIT) {
				try {
					JSONArray mapOverlay = edgeObj.getJSONArray("mapOverlays");
					for(int j=0;j<mapOverlay.length();j++) {
						JSONArray geoPt = mapOverlay.getJSONArray(j);
						double lat = geoPt.getDouble(0);
						double lon = geoPt.getDouble(1);
						db.execSQL("INSERT INTO "+ SentosaDatabaseStructure.TABLE_EDGE_OVERLAYS +
								" VALUES(NULL, "+ID_COL+", "+ lat +
								", "+lon+");");
					}

				} catch(Exception ex) {
					//do nothing. this means there are no mapOverlays
				}
			}
		}
	}

	private void parseNodeDetailsJSON() throws JSONException {
		
		String detail = SentosaUtils.getDataFromFileInAssetsToString("node_details.json",c);//.replace('\'', '\\');
		Log.d(" testing"," testing node detail "+detail);
		
		JSONArray nodeDetailsData = new JSONArray(detail);
		for(int i=0; i<nodeDetailsData.length(); i++){
			JSONObject nodeDetailObj = nodeDetailsData.optJSONObject(i);
			int ID_COL = nodeDetailObj.optInt("detailNodeId");  
			int NODE_ID_COL = nodeDetailObj.optInt("nodeId");
			String TITLE_COL = nodeDetailObj.optString("title");
			String CATEGORY_COL = nodeDetailObj.optString("category");
			String IMAGE_NAME_COL = nodeDetailObj.optString("imageName");
			String VIDEO_URL_COL = nodeDetailObj.optString("videoUrl");			
			String DESCRIPTION_COL = nodeDetailObj.optString("descriptionText"); 
			String ADMISSION_COL = nodeDetailObj.optString("admission"); 
			String OTHER_DETAILS_COL = nodeDetailObj.optString("otherDetails"); 
			String SECTION_COL = nodeDetailObj.optString("section"); 
			String OPENING_TIMES_COL = nodeDetailObj.optString("openingTimes"); 
			String CONTACT_NO_COL = nodeDetailObj.optString("contactNumber"); 
			String EMAIL_COL = nodeDetailObj.optString("email"); 
			String WEBSITE_COL = nodeDetailObj.optString("website"); 
			db.execSQL("INSERT INTO "+ SentosaDatabaseStructure.TABLE_NODE_DETAILS +
					" VALUES("+ID_COL+", "+NODE_ID_COL+", \""+ TITLE_COL +
					"\", \""+CATEGORY_COL+"\", \""+IMAGE_NAME_COL+  
					"\", \""+VIDEO_URL_COL+"\", \""+DESCRIPTION_COL+"\", \""+ADMISSION_COL+
					"\", \""+OTHER_DETAILS_COL+"\", \""+SECTION_COL+
					"\", \""+OPENING_TIMES_COL+"\", \""+CONTACT_NO_COL+
					"\", \""+EMAIL_COL+"\", \""+WEBSITE_COL+"\", 0);");
		}
	}

	private void parseThingsToDoJSON() throws JSONException {
		//NOTE: PLEASE COPY ONLY THE ARRAY PORTION OF THE THINGS TO DO JSON
		JSONArray thingsToDoData = new JSONArray(SentosaUtils.getDataFromFileInAssetsToString("things_to_do.json",c));
		int ctr = 1;
		for(int i=0; i<thingsToDoData.length(); i++){
			JSONObject thingsToDoObj = thingsToDoData.optJSONObject(i);
			String NAME_COL = thingsToDoObj.optString("Name");
			String ICON_ID_COL = thingsToDoObj.optString("IconId");
			String DESCRIPTION_COL = thingsToDoObj.optString("Description");

			JSONArray nodeDetailIds = thingsToDoObj.getJSONArray("LocationDetailNodeIds");
			for(int j=0; j<nodeDetailIds.length();j++) {
				int DETAIL_NODE_ID_COL = Integer.parseInt(nodeDetailIds.optString(j));
				db.execSQL("INSERT INTO "+ SentosaDatabaseStructure.TABLE_THINGS_TO_DO +
						" VALUES("+ctr+", "+DETAIL_NODE_ID_COL+", \""+NAME_COL+"\", \""+ICON_ID_COL+"\", \""+DESCRIPTION_COL+"\");");
				ctr++;
			}
		}
	}

	private int getEdgeType(String type, String color) {
		if(type.equals("WALK"))
			return Edge.TYPE_WALK;
		else if(type.equals("WAIT"))
			return Edge.TYPE_WAIT;
		else if(type.equals("RAIL"))
			return Edge.TYPE_TRAIN;
		else if(type.equals("BUS")) {
			if(color.equals("BLUE"))
				return Edge.TYPE_BUS1;
			else if(color.equals("RED"))
				return Edge.TYPE_BUS2;
			else if(color.equals("YELLOW"))
				return Edge.TYPE_BUS3;
		}
		else if(type.equals("TRAM")) {
			if(color.equals("PINK"))
				return Edge.TYPE_TRAM1;
			else if(color.equals("ORANGE"))
				return Edge.TYPE_TRAM2;
		}
		return Edge.TYPE_WALK;
	}

}
