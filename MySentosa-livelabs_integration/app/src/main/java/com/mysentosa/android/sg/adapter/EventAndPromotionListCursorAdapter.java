package com.mysentosa.android.sg.adapter;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.mysentosa.android.sg.BaseActivity;
import com.mysentosa.android.sg.R;
import com.mysentosa.android.sg.imageloader.ImageFetcher;
import com.mysentosa.android.sg.provider.utils.SentosaDatabaseStructure.EventsPromotionsBase;
import com.mysentosa.android.sg.utils.HttpHelper;
import com.mysentosa.android.sg.utils.SentosaUtils;

public class EventAndPromotionListCursorAdapter extends CursorAdapter {
	private final LayoutInflater mLayoutInflater;
	private ImageFetcher mImageWorker;
	public static final int TYPE_EVENT = 1;
	public static final int TYPE_PROMOTION = 2;
	private int TITLE_COL_INDEX = -1, IMAGE_URL_COL_INDEX = -1;
	
	public EventAndPromotionListCursorAdapter(Context context, Cursor c) {
		super(context, c, 0);
		mLayoutInflater = (LayoutInflater)
		context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		mImageWorker = ((BaseActivity)context).mImageFetcher;
	}
	
	public class ViewHolder {;
		public TextView tvDescription;
		public ImageView ivThumbnail;
		public long id;
	}


	@Override
	public void bindView(View view, Context context, Cursor cursor) {
		ViewHolder holder = (ViewHolder) view.getTag();
		String imageUrl = null;
		String description = cursor.getString(TITLE_COL_INDEX);
		holder.tvDescription.setText(description);

		imageUrl = cursor.getString(IMAGE_URL_COL_INDEX);
		holder.ivThumbnail.setImageResource(R.drawable.stub_thumb);
		if(SentosaUtils.isValidString(imageUrl)) 
			//imageLoader.DisplayImage(HttpHelper.BASE_HOST+image_url, holder.iv_thumbnail);
			mImageWorker.loadImage(HttpHelper.BASE_HOST+imageUrl, holder.ivThumbnail, null, R.drawable.stub_thumb, true,null);
		holder.id = cursor.getLong(cursor.getColumnIndex(EventsPromotionsBase.ID_COL));
	}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
		TITLE_COL_INDEX = cursor.getColumnIndex(EventsPromotionsBase.TITLE_COL);
		IMAGE_URL_COL_INDEX = cursor.getColumnIndex(EventsPromotionsBase.IMAGE_URL_COL);
		View convertView =  mLayoutInflater.inflate(R.layout.item_event_promo_list_large, null);
		ViewHolder holder =new ViewHolder();
        holder.tvDescription=(TextView) convertView.findViewById(R.id.tv_event_promo_item_large_description);
        holder.ivThumbnail=(ImageView) convertView.findViewById(R.id.iv_event_promo_item_large_thumbnail);                
        convertView.setTag(holder);
		return convertView;
	}

}
