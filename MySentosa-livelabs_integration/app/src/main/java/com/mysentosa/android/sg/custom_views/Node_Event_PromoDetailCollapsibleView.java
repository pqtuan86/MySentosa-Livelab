package com.mysentosa.android.sg.custom_views;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Typeface;
import android.text.util.Linkify;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.flurry.android.FlurryAgent;
import com.mysentosa.android.sg.EventsAndPromotionsDetailActivity;
import com.mysentosa.android.sg.NodeDetailActivity;
import com.mysentosa.android.sg.R;
import com.mysentosa.android.sg.provider.utils.SentosaDatabaseStructure.EventsPromotionsBase;
import com.mysentosa.android.sg.provider.utils.SentosaDatabaseStructure.NodeDetailsData;
import com.mysentosa.android.sg.utils.Const;
import com.mysentosa.android.sg.utils.Const.FlurryStrings;


public class Node_Event_PromoDetailCollapsibleView extends LinearLayout{
	private Context mContext;
	private String title, detail;
	private final int COLLAPSED = 0, EXPANDED = 1;
	private int type = -1;
	private boolean isEventPromotion = false;
	public static final int DETAIL_TEXT_VIEW = 25232523, EVENT = 0, PROMOTION = 1, LOCATION = 2;
	private final String[] titleList = {"Related Events","Related Promotions","Related Attractions"};
	public static final String DESCRIPTION = "Description";
	
	public Node_Event_PromoDetailCollapsibleView(Context context, String title, String detail) {
		super(context,null);
		this.mContext = context;
		this.title = title;
		this.detail = detail;
		initializeView(null);
	}
	
	public Node_Event_PromoDetailCollapsibleView(Context context, int type, Cursor c) {
		super(context,null);
		this.mContext = context;
		this.type = type;
		this.title = titleList[type];
		this.isEventPromotion = true;
		initializeView(c);
	}

	public Node_Event_PromoDetailCollapsibleView(Context context, AttributeSet attrs) {
		super(context, attrs,0);
	}

	public Node_Event_PromoDetailCollapsibleView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	private void initializeView(Cursor c) {
		setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
		this.setOrientation(LinearLayout.VERTICAL);
		float multiplier = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 1, getResources().getDisplayMetrics());
		int v = (int)Math.round(6*multiplier);
		this.setPadding(v, 2*v, v, 2*v);
		
		final TextView tvTitle = getTextView(true, false);
		tvTitle.setTag(COLLAPSED);
		this.addView(tvTitle);
		
		final View toggleVisibilityView;
		if(isEventPromotion) {
			View eventPromoList = getEventsPromosList(c);
			this.addView(eventPromoList);
			toggleVisibilityView = eventPromoList;
		} else {
			TextView tvDetail = getTextView(false, false);
			this.addView(tvDetail);
			toggleVisibilityView = tvDetail;
		}
		
		
		tvTitle.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				int state = (Integer) v.getTag();
				tvTitle.setCompoundDrawablesWithIntrinsicBounds(null, null, mContext.getResources().getDrawable(state==COLLAPSED?R.drawable.bt_arrow_up:R.drawable.bt_arrow_down), null);
				toggleVisibilityView.setVisibility(state==COLLAPSED?View.VISIBLE:View.GONE);
				v.setTag(state==COLLAPSED?EXPANDED:COLLAPSED);
			}
		});
		
		if(this.title.equals(DESCRIPTION)) tvTitle.performClick();
		
	}


	private TextView getTextView(boolean isTitle, boolean showArrow) {
		TextView tv = new TextView(mContext);
		LinearLayout.LayoutParams llp = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
		llp.gravity = Gravity.CENTER_VERTICAL|Gravity.LEFT;
		tv.setLayoutParams(llp);
		tv.setBackgroundDrawable(mContext.getResources().getDrawable(R.drawable.bg_event_promo_list));
		tv.setTextColor(Color.BLACK);
		tv.setTypeface(Typeface.SANS_SERIF);
		tv.setTextSize(isTitle?24:18);
		tv.setClickable(true);
		tv.setGravity(Gravity.LEFT|Gravity.CENTER_VERTICAL);
		tv.setText(isTitle?title:detail);
		if(!isTitle) {
			tv.setId(DETAIL_TEXT_VIEW);
			llp.rightMargin = llp.leftMargin = (int) Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 10, getResources().getDisplayMetrics()));
			llp.topMargin = llp.bottomMargin = (int) Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 5, getResources().getDisplayMetrics()));
			if(!isEventPromotion)
				tv.setVisibility(GONE);
			if(showArrow)
				tv.setCompoundDrawablesWithIntrinsicBounds(null, null, mContext.getResources().getDrawable(R.drawable.list_arrow), null);
			Linkify.addLinks(tv, Linkify.ALL);
		} else {
			tv.setCompoundDrawablesWithIntrinsicBounds(null, null, mContext.getResources().getDrawable(R.drawable.bt_arrow_down), null);
		}
		return tv;
	}
	
	@SuppressLint({ "NewApi", "NewApi" })
	private View getEventsPromosList(Cursor cursor) {
		LinearLayout ll = new LinearLayout(mContext);
		LinearLayout.LayoutParams llp = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
		ll.setLayoutParams(llp);
		ll.setOrientation(VERTICAL);
		ll.setVisibility(View.GONE);
		try {
			ll.setDividerDrawable(mContext.getResources().getDrawable(R.drawable.menu_main_list_divider));
			ll.setShowDividers(SHOW_DIVIDER_MIDDLE);
		} catch(Throwable e) {
			// catch in case 2 APIs above not compatible with lower platform.
		}
		while(cursor.moveToNext()) {
			this.detail = cursor.getString(cursor.getColumnIndex(type!=LOCATION?EventsPromotionsBase.TITLE_COL:NodeDetailsData.TITLE_COL));
			TextView tvDetail= getTextView(false, true);
			long id = cursor.getLong(cursor.getColumnIndex(type!=LOCATION?EventsPromotionsBase.ID_COL:NodeDetailsData.NODE_ID_COL));
			tvDetail.setTag(id);
			tvDetail.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					if(type!=LOCATION) {
						final int curType = type==EVENT?EventsAndPromotionsDetailActivity.TYPE_EVENT:EventsAndPromotionsDetailActivity.TYPE_PROMOTION;
						final String flurryData = type==EVENT?FlurryStrings.EventDetailsLocationDetails:FlurryStrings.PromotionDetailsLocationDetails;
						FlurryAgent.logEvent(flurryData);
						Intent intent = new Intent((NodeDetailActivity)mContext,EventsAndPromotionsDetailActivity.class);
						intent.putExtra(EventsPromotionsBase.ID_COL, (Long)v.getTag());
						intent.putExtra(EventsAndPromotionsDetailActivity.CURRENT_TYPE, curType);
						mContext.startActivity(intent);
					} else {
						Intent intent = new Intent((EventsAndPromotionsDetailActivity)mContext,NodeDetailActivity.class);
						intent.putExtra(Const.NODE_ID, ((Long) v.getTag()).intValue());
						intent.putExtra(NodeDetailActivity.SOURCE_ACTIVITY,NodeDetailActivity.ACTIVITY_EVENT_PROMO);
						intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
						mContext.startActivity(intent);
					}
				}
			});
			
			LinearLayout.LayoutParams llp2 = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
			llp2.rightMargin = llp2.leftMargin = (int) Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 10, getResources().getDisplayMetrics()));
			llp2.topMargin = llp2.bottomMargin = (int) Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 10, getResources().getDisplayMetrics()));
			tvDetail.setLayoutParams(llp2);
			ll.addView(tvDetail);
		}
		
		return ll;
	}
	
}
