package com.mysentosa.android.sg.adapter;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONObject;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Parcelable;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.mysentosa.android.sg.BaseActivity;
import com.mysentosa.android.sg.HomeActivity;
import com.mysentosa.android.sg.NodeDetailActivity;
import com.mysentosa.android.sg.R;
import com.mysentosa.android.sg.custom_views.AspectRatioImageView;
import com.mysentosa.android.sg.custom_views.CustomViewPager;
import com.mysentosa.android.sg.imageloader.ImageFetcher;
import com.mysentosa.android.sg.models.HomeItem;
import com.mysentosa.android.sg.provider.SentosaContentProvider;
import com.mysentosa.android.sg.provider.utils.SentosaDatabaseStructure;
import com.mysentosa.android.sg.provider.utils.SentosaDatabaseStructure.ContentURIs;
import com.mysentosa.android.sg.utils.Const;
import com.mysentosa.android.sg.utils.HttpHelper;
import com.mysentosa.android.sg.utils.LogHelper;
import com.mysentosa.android.sg.utils.SentosaUtils;

public class HomePagerAdapter extends CustomPagerAdapter {

	private ArrayList<HomeItem> homeItemsList;
	private Context mContext;
	private ImageFetcher mImageWorker;
	private String nodeIdList;
	private static final String TOP_ATTRACTIONS_NODE_ID_LIST = "TOP_ATTRACTIONS_NODE_ID_LIST", DEFAULT_LIST = "20,16,9,22,23,17,13,7,8,42";

	public HomePagerAdapter(Context c) {
		this.mContext = c;
		this.setHomeItemsList();
		mImageWorker = ((BaseActivity)c).mImageFetcher;
		new UpdateHomeItemsAsyncTask().execute();
	}
	
	@Override
	public int getCount() {
		return this.homeItemsList.size();
	}

	@Override
	public int getItemPosition(Object object) {
	    return POSITION_NONE;
	}
	
	@Override
	public Object instantiateItem(View container, int position) {
		HomeItem homeItem=homeItemsList.get(position);
		View layout = View.inflate(mContext,R.layout.item_home, null);
		
		TextView itemTitle = (TextView) layout.findViewById(R.id.tv_home_item_title);
		itemTitle.setText(homeItem.getTitle());
		
		TextView descTitle = (TextView) layout.findViewById(R.id.tv_home_item_description);
		descTitle.setText(homeItem.getDescription());

		AspectRatioImageView imageView = (AspectRatioImageView) layout.findViewById(R.id.iv_home_item);
//		if(SentosaUtils.isOnline(mContext))
		LogHelper.i("TAG","im: "+homeItem.getImgUrl());
		LogHelper.i("TAG","defa: "+SentosaUtils.getResourceId(mContext, "default_node_"+homeItem.getID()));
			mImageWorker.loadImage(homeItem.getImgUrl(), imageView, null,SentosaUtils.getResourceId(mContext, "default_node_"+homeItem.getID()),false,null);
//		else {
//			LogHelper.d(" testing"," testing id" +"default_node_"+homeItem.getNodeID());
//			imageView.setImageResource();
//		}
		layout.setTag(homeItem);
 
	    ((CustomViewPager) container).addView(layout);
	    
	    return layout;
	}

	@Override
	public void destroyItem(View container, int position, Object object) {
		((CustomViewPager) container).removeView((View) object);		
	}

	@Override
	public boolean isViewFromObject(View view, Object object) {
		return view==object;
	}

	private void setHomeItemsList() {
		homeItemsList = new ArrayList<HomeItem>();
		
		this.nodeIdList = ((HomeActivity)mContext).getPreferences(Context.MODE_PRIVATE).getString(TOP_ATTRACTIONS_NODE_ID_LIST, DEFAULT_LIST);
		String[] nodeIds = nodeIdList.split(",");
		LogHelper.d(" testing"," testing nodeIdList" +nodeIdList);
		for(int i=0; i<nodeIds.length;i++) {
			LogHelper.d(" testing"," testing nodeIds" +nodeIds[i]);
		}
		
		int title_col = -1;
		int category_col = -1;
		int description_col = -1;
		int node_id_col = -1;
		int id_col = -1;
		int nodeID;
		int ID;
		String title = null;
		String description = null;
		String imgUrl = null;
		String categoryName = null;
		
		for(int i=0; i<nodeIds.length;i++) {
			Cursor c = mContext.getContentResolver().query(ContentURIs.SENTOSA_URI, null, "SELECT * FROM "+SentosaDatabaseStructure.TABLE_NODE_DETAILS+" WHERE "+SentosaContentProvider.NodeDetailsData.ID_COL+" = "+nodeIds[i].trim(), null, null);//currently we use a temp query. change it once api is made available.
			if(c.moveToFirst()){
				if(title_col == -1) {
						title_col = c.getColumnIndex(SentosaContentProvider.NodeDetailsData.TITLE_COL);
						category_col = c.getColumnIndex(SentosaContentProvider.NodeDetailsData.CATEGORY_COL);
						description_col = c.getColumnIndex(SentosaContentProvider.NodeDetailsData.DESCRIPTION_COL);
						node_id_col = c.getColumnIndex(SentosaContentProvider.NodeDetailsData.NODE_ID_COL);
						id_col = c.getColumnIndex(SentosaContentProvider.NodeDetailsData.ID_COL);
			}				
					nodeID = c.getInt(node_id_col);
					ID = c.getInt(id_col);
				    title = c.getString(title_col);
				    description = c.getString(description_col);
				    if(description==null) description = "";			    
				    categoryName = c.getString(category_col);
				    if(categoryName.equals(Const.ATTRACTION) || categoryName.equals(Const.FNB) || categoryName.equals(Const.HOTEL_AND_SPA)
							|| categoryName.equals(Const.SHOPPING) || categoryName.equals(Const.BUS) || categoryName.equals(Const.TRAIN) 
							|| categoryName.equals(Const.TRAM)) {
						imgUrl = HttpHelper.BASE_HOST+NodeDetailActivity.IMAGE_PATH_SUFFIX+ID+".jpg";
				    }
				    else { 
				    	imgUrl = HttpHelper.BASE_HOST+NodeDetailActivity.IMAGE_PATH_SUFFIX+"category_"+categoryName.toLowerCase().replace("'", "").replace(' ', '_').trim()+".png";
					}
				    
				    homeItemsList.add(new HomeItem(ID,nodeID, imgUrl, title, description)); 
			}
			c.close();
		}
	}
	
	/**
	 * Called when the a change in the shown pages has been completed.  At this
	 * point you must ensure that all of the pages have actually been added or
	 * removed from the container as appropriate.
	 * @param container The containing View which is displaying this adapter's
	 * page views.
	 */
	@Override
	public void finishUpdate(View arg0) {}


	@Override
	public void restoreState(Parcelable arg0, ClassLoader arg1) {}

	@Override
	public Parcelable saveState() {
		return null;
	}

	@Override
	public void startUpdate(View arg0) {}
	
	
	public ArrayList<HomeItem> getHomeItemsList() {
		return homeItemsList;
	}	
	
	public class UpdateHomeItemsAsyncTask extends AsyncTask<Void, Void, Boolean> {
		private static final String DATA_JSON = "Data", LOCATION_TOP_LIST_JSON = "LocationTopList", DETAIL_NODE_ID_JSON = "DetailNodeIds";	
		private static final String GROUP_CATEGORIES_API = "GroupCategories/";
		private String includeString = "";
				
		@Override
		protected Boolean doInBackground(Void... params) {
			String requestUri = HttpHelper.BASE_ADDRESS + GROUP_CATEGORIES_API;
			try {
				String result =  HttpHelper.sendRequestUsingGet(requestUri, null);
				JSONArray nodeIDs = (new JSONObject(result)).optJSONObject(DATA_JSON).optJSONObject(LOCATION_TOP_LIST_JSON).optJSONArray(DETAIL_NODE_ID_JSON);
				if(nodeIDs == null) return false;
				
				for(int i = 0; i< nodeIDs.length(); i++) {
					if(i>0){includeString+=", ";}
					includeString+=nodeIDs.getString(i);
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
			if(result && !includeString.equals("")) {
				SharedPreferences mPrefs = ((HomeActivity)mContext).getPreferences(Context.MODE_PRIVATE);
				final SharedPreferences.Editor edit = mPrefs.edit();
				edit.putString(TOP_ATTRACTIONS_NODE_ID_LIST,includeString);
				edit.commit();
				nodeIdList = includeString;
				setHomeItemsList();
				notifyDataSetChanged();
				((HomeActivity) mContext).mPageIndicator.invalidate();
			}
		}		
	}
	
}