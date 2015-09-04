package com.mysentosa.android.sg.adapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.osmdroid.util.GeoPoint;

import android.content.Context;
import android.database.Cursor;
import android.location.Location;
import android.support.v4.widget.CursorAdapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.SectionIndexer;
import android.widget.TextView;

import com.mysentosa.android.sg.BaseActivity;
import com.mysentosa.android.sg.R;
import com.mysentosa.android.sg.imageloader.ImageFetcher;
import com.mysentosa.android.sg.location.LocationFinder;
import com.mysentosa.android.sg.location.LocationNotifier;
import com.mysentosa.android.sg.location.PlacesConstants;
import com.mysentosa.android.sg.provider.utils.SentosaDatabaseStructure.NodeData;
import com.mysentosa.android.sg.provider.utils.SentosaDatabaseStructure.NodeDetailsData;
import com.mysentosa.android.sg.utils.Const;
import com.mysentosa.android.sg.utils.HttpHelper;
import com.mysentosa.android.sg.utils.LogHelper;
import com.mysentosa.android.sg.utils.RepoTools;
import com.mysentosa.android.sg.utils.SentosaUtils;

import butterknife.ButterKnife;
import butterknife.InjectView;


public class ThingsToDoList_Bookmarks_CursorAdapter extends CursorAdapter {

	private final LayoutInflater inflater;
	private int titleCol = -1, nodeIdCol = -1, categoryCol = -1, imgNodeIdCol = -1, latitudeCol = -1, longitudeCol = -1; 
	private Context context;
	private ImageFetcher mImageWorker;
	private ZoneSectionIndexer zoneSectionIndexer;
	private static final int TYPE_HEADER = 1;
	private static final int TYPE_NORMAL = 0;
	private Cursor zoneCursor, childCursor;
	private GeoPoint currentLocation = null;
	
	public ThingsToDoList_Bookmarks_CursorAdapter(Context context, Cursor c, Cursor zoneCursor, int flags) {
		super(context, c, flags);
		this.context = context;
		this.zoneCursor = zoneCursor;
		this.childCursor = c;
		inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		zoneSectionIndexer = new ZoneSectionIndexer();
		mImageWorker = ((BaseActivity)context).mImageFetcher;
		detectLocation();
	}


    public int getNodeId(int position) {
        int itemPosition = position - zoneSectionIndexer.getSectionForPosition(position) - 1;
        Cursor cursor = getCursor();
        if(cursor.moveToPosition(itemPosition)) {
            int node_id = RepoTools.getInt(cursor, NodeDetailsData.NODE_ID_COL);
            return node_id;
        }

        return 0;
    }

    @Override
	public void bindView(View view, Context context, Cursor c) {
        ViewHolder holder = (ViewHolder) view.getTag();

        String title = RepoTools.getString(c, NodeDetailsData.TITLE_COL);
        int node_detail_id = RepoTools.getInt(c, NodeDetailsData.ID_COL);
        String category = RepoTools.getString(c, NodeDetailsData.CATEGORY_COL);
        String imageUrl = RepoTools.getString(c, NodeDetailsData.IMAGE_NAME_COL);
        double latitude = RepoTools.getDouble(c, NodeData.LATITUDE_COL);
        double longitude = RepoTools.getDouble(c, NodeData.LONGITUDE_COL);

        holder.description.setText(title);

        imageUrl = getImageUrl(imageUrl, category, node_detail_id);
		if(SentosaUtils.isValidString(imageUrl)) {
			mImageWorker.loadImage(imageUrl, holder.thumbnail, null, R.drawable.stub_thumb, true,null);
		}

		if(currentLocation!=null) {
			GeoPoint nodeLocation = new GeoPoint(latitude, longitude);
			int distance = currentLocation.distanceTo(nodeLocation);
			float distanceInKm = Math.round(distance/100.0)/10.0f;
			
			if(distance>=0) {
                holder.distance.setText(distanceInKm + "km away");
			}
		}
	}

	@Override
	public View newView(Context context, Cursor c, ViewGroup vg) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_thingstodo_bookmarks_list, vg, false);

        ViewHolder holder = new ViewHolder(view);
        view.setTag(holder);

        return view;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		final int type = getItemViewType(position);
		if(type == TYPE_HEADER) {
			if (convertView == null){
				convertView = inflater.inflate(R.layout.item_thingstodo_bookmarks_section_header, parent, false);
			}
			((TextView)convertView.findViewById(R.id.tv_listitem_description)).setText((String)zoneSectionIndexer.getSections()
					[zoneSectionIndexer.getSectionForPosition(position)]);
			return convertView;
		}
		else
			return super.getView(position -zoneSectionIndexer.getSectionForPosition(position)-1, convertView, parent);
	}

	@Override
	public int getCount() {
		return super.getCount()+(zoneCursor==null?0:zoneCursor.getCount());
	}

	@Override
	public boolean areAllItemsEnabled() {
		return false;
	}

	@Override
	public int getItemViewType(int position ) {
		if(position == zoneSectionIndexer.getPositionForSection(zoneSectionIndexer.getSectionForPosition(position)))
			return TYPE_HEADER;
		else 
			return TYPE_NORMAL;
	}

	@Override
	public int getViewTypeCount() {
		return 2;
	}

	@Override
	public boolean isEnabled(int position) {
		if (getItemViewType(position) == TYPE_HEADER){
			return false;
		}
		return true;
	}

	public void swapCursor(Cursor childCursor, Cursor zoneCursor) {
		this.childCursor = childCursor;
		this.zoneCursor = zoneCursor;
		zoneSectionIndexer = new ZoneSectionIndexer();
		super.swapCursor(childCursor);
	}

    private String getImageUrl(String imagePath, String categoryName, int nodeDetailsId) {
        String imgUrl;
        if(imagePath != null && imagePath.length() > 0) {
            imgUrl = HttpHelper.BASE_HOST + imagePath;
        } else {
            String IMAGE_PATH_SUFFIX = "/Content/Photos/AssetsAttractionAndroid/";

            if (categoryName.equals(Const.ATTRACTION)
                    || categoryName.equals(Const.FNB)
                    || categoryName.equals(Const.HOTEL_AND_SPA)
                    || categoryName.equals(Const.SHOPPING)
                    || categoryName.equals(Const.BUS)
                    || categoryName.equals(Const.TRAIN)
                    || categoryName.equals(Const.TRAM)) {
                imgUrl = HttpHelper.BASE_HOST + IMAGE_PATH_SUFFIX + nodeDetailsId + ".jpg";
            } else {
                imgUrl = HttpHelper.BASE_HOST + IMAGE_PATH_SUFFIX + "category_" + categoryName.toLowerCase().replace("'", "").replace(' ', '_').trim() + ".png";
            }
        }

        return imgUrl;
    }

	private LocationFinder locationFinder = null;

	private void detectLocation() {
		locationFinder = new LocationFinder(context,
				new LocationNotifier() {
			@Override
			public void updatedLocation(Location location) {
				LogHelper.d("test","test location lat: "+location.getLatitude()+"long: "+location.getLongitude());
				if(location != null) {
					currentLocation = new GeoPoint(location.getLatitude(), location.getLongitude());
					if(location.hasAccuracy() && location.getAccuracy()<40)
						locationFinder.disableLocationUpdates();
					ThingsToDoList_Bookmarks_CursorAdapter.this.notifyDataSetChanged();
					LogHelper.d("test","test location 2 lat: "+location.getLatitude()+"long: "+location.getLongitude());
				}
			}

			@Override
			public void listenersDisabled() { 
			}
		}, PlacesConstants.MAX_TIME_TO_GET_LOCATION);
		locationFinder.requestLocationUpdates();
	}

	private class ZoneSectionIndexer implements SectionIndexer {

		public Map<Integer,Integer> sectionToPosition;
		public List<String> zones;

		public ZoneSectionIndexer() {
			zones = new ArrayList<String> ();
			sectionToPosition = new HashMap<Integer,Integer>();
			while(zoneCursor != null && zoneCursor.moveToNext()) {
				zones.add(zoneCursor.getString(zoneCursor.getColumnIndex(NodeDetailsData.SECTION_COL)));
			}
			int cursorPosition = 0;
			int sectionPosition = 0;
			sectionToPosition.put(sectionPosition, cursorPosition+sectionPosition);
			while(childCursor != null && childCursor.moveToNext()&& sectionPosition < zones.size()) {
				if(!zones.get(sectionPosition).equals(childCursor.getString(childCursor.getColumnIndex(NodeDetailsData.SECTION_COL)))) {
					sectionPosition++;
					if(sectionPosition < zones.size())
						sectionToPosition.put(sectionPosition, cursorPosition+sectionPosition);
				}
				cursorPosition++;
			}	    	
		}


		@Override
		public int getPositionForSection(int section) {
			return sectionToPosition.get(section);
		}

		@Override
		public int getSectionForPosition(int position) {
			for(int i=0; i<zones.size()-1 ;i++) {
				if(sectionToPosition.get(i) <= position && position < sectionToPosition.get(i+1))
					return i;			
			}
			return zones.size()-1;
		}

		@Override
		public Object[] getSections() {
			return zones.toArray();
		}

	}

    static class ViewHolder {

        @InjectView(R.id.iv_thumbnail) ImageView thumbnail;
        @InjectView(R.id.tv_description) TextView description;
        @InjectView(R.id.tv_distance) TextView distance;

        public ViewHolder(View view) {
            ButterKnife.inject(this, view);
        }
    }
}