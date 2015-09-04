package com.mysentosa.android.sg.custom_views;

import java.util.ArrayList;
import java.util.Collections;

import org.json.JSONArray;
import org.json.JSONObject;

import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ViewFlipper;

import com.mysentosa.android.sg.BaseActivity;
import com.mysentosa.android.sg.EventsAndPromotionsDetailActivity;
import com.mysentosa.android.sg.R;
import com.mysentosa.android.sg.imageloader.ImageFetcher;
import com.mysentosa.android.sg.imageloader.ImageWorker.Callback;
import com.mysentosa.android.sg.provider.utils.SentosaDatabaseStructure.EventsPromotionsBase;
import com.mysentosa.android.sg.utils.HttpHelper;
import com.mysentosa.android.sg.utils.LogHelper;
import com.mysentosa.android.sg.utils.SentosaUtils;


public class BannerView extends FrameLayout{
	
	private Context mContext;
	private ViewFlipper bannerImgContainer;
	private ImageView iv_1, iv_2, iv_3;
	private boolean mIsShowing;
	private boolean isClosable;
	private ArrayList<Banner> banners = new ArrayList<Banner>();
	private Handler mHandler;
	int currentDuration = 0;
	int counter = 0;
	private ImageFetcher mImageWorker; 
	
	public BannerView(Context context) {
		this(context,null,0);
	}

	public BannerView(Context context, AttributeSet attrs) {
		this(context, attrs,0);
	}

	public BannerView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
        if(attrs != null) {
        	TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.BannerView);
        	isClosable = a.getBoolean(R.styleable.BannerView_isClosable, true);	
        }
        else isClosable = false;
        mContext = context;
        mHandler = new Handler();
        mImageWorker = ((BaseActivity)mContext).mImageFetcher;
        initializeView();        
	}
	
	private void initializeView() {
		inflateLayout();
		
		bannerImgContainer = (ViewFlipper) findViewById(R.id.vf_banner_img_container);
		bannerImgContainer.setInAnimation(mContext, R.anim.push_down_in);
		bannerImgContainer.setOutAnimation(mContext, R.anim.push_down_out);
			
		iv_1 = (ImageView) findViewById(R.id.iv_banner_img1);
		iv_2 = (ImageView) findViewById(R.id.iv_banner_img2);
		iv_3 = (ImageView) findViewById(R.id.iv_banner_img3);
		
		OnClickListener onClickListener = new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Banner banner = (Banner)v.getTag();
				if(banner != null) {
					Intent intent;
					switch(banner.getType()) {
					case Banner.TYPE_EVENT:
						intent = new Intent(mContext,EventsAndPromotionsDetailActivity.class);
						intent.putExtra(EventsPromotionsBase.ID_COL, banner.getEventId());
						intent.putExtra(EventsAndPromotionsDetailActivity.CURRENT_TYPE, EventsAndPromotionsDetailActivity.TYPE_EVENT);
						mContext.startActivity(intent);
						break;
					case Banner.TYPE_PROMOTION:
						intent = new Intent(mContext,EventsAndPromotionsDetailActivity.class);
						intent.putExtra(EventsPromotionsBase.ID_COL, banner.getPromotionId());
						intent.putExtra(EventsAndPromotionsDetailActivity.CURRENT_TYPE, EventsAndPromotionsDetailActivity.TYPE_PROMOTION);
						mContext.startActivity(intent);
						break;					
					case Banner.TYPE_EXTERNAL:
						intent = new Intent(Intent.ACTION_VIEW);
						String url = banner.getExternalURL();
						if(!url.startsWith("http://")) url = "http://"+url;
						Uri uri = Uri.parse(url);
						try {
							intent.setData(uri);
							mContext.startActivity(intent);
						} catch (Exception e) {
							e.printStackTrace();
						}
						break;						
					}

				}
			}
		};
		iv_1.setOnClickListener(onClickListener);
		iv_2.setOnClickListener(onClickListener);
		iv_3.setOnClickListener(onClickListener);
				
		View close = findViewById(R.id.iv_banner_close);
		if(isClosable)
			close.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					hide();				
				}
			});
		else 
			close.setVisibility(View.GONE);
	}
	
	public void updateBanner(){
        new UpdateBannerAsyncTask().execute();
	}
	
	public boolean isClosable() {
		return isClosable;
	}

	public void setClosable(boolean isClosable) {
		this.isClosable = isClosable;
	}

	public boolean isShowing() {
		return mIsShowing;
	}
	
	public void displayBanners() {
		if(banners != null && !banners.isEmpty()) {
			ImageView nextChild ;
			Banner nextBanner = banners.get(counter);
			//LogHelper.i("nextBanner",nextBanner.toString());
			if(nextBanner != null) {
				int i = bannerImgContainer.getDisplayedChild();
				if(i==0)
					nextChild = (ImageView)bannerImgContainer.getChildAt(1);
				else if(i==1)
					nextChild = (ImageView)bannerImgContainer.getChildAt(2);
				else 
					nextChild = (ImageView)bannerImgContainer.getChildAt(0);					
				nextChild.setTag(nextBanner);
				mImageWorker.loadImage(HttpHelper.BASE_HOST+nextBanner.getImageURL(), nextChild, null, R.drawable.stub_banner,false,null);
				mHandler.postDelayed(displayRunnable, currentDuration);
				//LogHelper.i("currentDuration",currentDuration+"");
				currentDuration = nextBanner.getDisplayDuration()*1000;
			}
			counter++;
			if(counter == banners.size()) {
				counter = 0;
			}
		}
	}
	
	
	Runnable displayRunnable = new Runnable() {
			
			@Override
			public void run() {
				bannerImgContainer.showNext();
				displayBanners();
			}
	};
	
	
	public void hide() {
		this.setVisibility(View.GONE);
		clear();
	}
	
	public void clear() {
		banners.clear();
		mIsShowing = false;
		mHandler.removeCallbacks(displayRunnable);
	}
		
	private void inflateLayout() {
		LayoutInflater inflater = (LayoutInflater) getContext()
		.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		inflater.inflate(R.layout.banner_layout, this);
	}
	
	public class UpdateBannerAsyncTask extends AsyncTask<Void, Void, Boolean> {
		private static final String DATA_JSON = "Data";	
		private static final String BANNERS_JSON = "Banners";
		
				
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			for(int i=0;i<bannerImgContainer.getChildCount();i++) {
				ImageView iv = (ImageView)bannerImgContainer.getChildAt(i);
				iv.setTag(null);
				iv.setImageResource(R.drawable.stub_banner);
			}
			bannerImgContainer.setDisplayedChild(0);
			BannerView.this.setVisibility(View.GONE);
			counter = 0;
			currentDuration = 0;
		}

		@Override
		protected Boolean doInBackground(Void... params) {
			String requestUri = HttpHelper.BASE_ADDRESS + BANNERS_JSON ;
			try {
				String result =  HttpHelper.sendRequestUsingGet(requestUri, null);
				JSONObject rootObject = new JSONObject(result).optJSONObject(DATA_JSON);
				JSONArray bannersArray = rootObject.optJSONArray(BANNERS_JSON);
				if(bannersArray == null) return false;
				LogHelper.i("banners_length",bannersArray.length()+"");
				for(int i = 0; i< bannersArray.length(); i++) {
					Banner banner = Banner.getBanner(bannersArray.optJSONObject(i));
					if(banner != null)
						banners.add(banner);
				}
				Collections.sort(banners);
				//LogHelper.i("SORTED_BANNER",banners.toString());
				return true;
			} catch (Exception e) {
				e.printStackTrace();
				return false;
			}	
				
		}

		@Override
		protected void onPostExecute(Boolean result) {
			super.onPostExecute(result);

			if(result && banners != null && banners.size() > 0) {
				mIsShowing = true;
				Banner firstBanner = banners.get(counter);
				counter++;
				if(counter == banners.size()) {
					counter = 0;
				}
				ImageView iv = (ImageView)bannerImgContainer.getChildAt(0);
				if(firstBanner != null) {
					iv.setTag(firstBanner);
					currentDuration = firstBanner.getDisplayDuration()*1000;
					mImageWorker.loadImage(HttpHelper.BASE_HOST+firstBanner.getImageURL(), iv, null, R.drawable.stub_banner,false,new Callback() {
					
						@Override
						public void onFinish() {
							BannerView.this.setVisibility(View.VISIBLE);
							BannerView.this.startAnimation(AnimationUtils.loadAnimation(mContext, R.anim.push_down_in));
							displayBanners();
						}});
				}
			}
		}		
	}
	
	public static class Banner implements Comparable<Banner>{
		
		public static final String ID_JSON = "Id";
		public static final String IMAGE_URL_JSON = "ImageURL";
		public static final String END_DATE_JSON = "EndDate";
		public static final String DISPLAY_DURATION_JSON = "DisplayDuration";
		public static final String EXTERNAL_URL_JSON = "ExternalURL";
		public static final String EVENT_ID_JSON = "EventId";
		public static final String PROMOTION_ID_JSON = "PromotionId";
		public static final String ORDER_NUMBER_JSON = "OrderNumber";
		
		public static final int TYPE_EXTERNAL = 1;
		public static final int TYPE_EVENT = 2;
		public static final int TYPE_PROMOTION = 3;
		
		private int ID;
		private int orderNumber;
		private long eventId;
		private long promotionId;
		private int displayDuration;
		private String externalURL;
		private String imageURL;
		private int type;
		

		public Banner(int ID, int orderNumber, long id, int displayDuration, String imageURL, int type) {
			super();
			this.ID = ID;
			this.orderNumber = orderNumber;
			if(type == 2)
				eventId = id;
			else 
				promotionId = id;
			this.displayDuration = displayDuration;
			this.imageURL = imageURL;
			this.type = type;
		}
		
	

		public Banner(int ID, int orderNumber, String externalURL, int displayDuration, String imageURL, int type) {
			super();
			this.ID = ID;
			this.orderNumber = orderNumber;
			this.externalURL = externalURL;
			this.displayDuration = displayDuration;
			this.imageURL = imageURL;
			this.type = type;
		}

		public int getId() {
			return ID;
		}
		
		public int getType() {
			return type;
		}

		public void setType(int type) {
			this.type = type;
		}

		public int getOrderNumber() {
			return orderNumber;
		}


		public void setOrderNumber(int orderNumber) {
			this.orderNumber = orderNumber;
		}


		public long getEventId() {
			return eventId;
		}


		public void setEventId(long eventId) {
			this.eventId = eventId;
		}


		public long getPromotionId() {
			return promotionId;
		}


		public void setPromotionId(long promotionId) {
			this.promotionId = promotionId;
		}


		public int getDisplayDuration() {
			return displayDuration;
		}


		public void setDisplayDuration(int displayDuration) {
			this.displayDuration = displayDuration;
		}


		public String getExternalURL() {
			return externalURL;
		}


		public void setExternalURL(String externalURL) {
			this.externalURL = externalURL;
		}


		public String getImageURL() {
			return imageURL;
		}


		public void setImageURL(String imageURL) {
			this.imageURL = imageURL;
		}
		
		public static Banner getBanner(JSONObject data) {
			String imgURL = data.optString(IMAGE_URL_JSON);
			int duration = data.optInt(DISPLAY_DURATION_JSON);
			int ID = data.optInt(ID_JSON);
			int orderNumber = data.optInt(ORDER_NUMBER_JSON);
			long eventId = data.optLong(EVENT_ID_JSON);
			long promotionId = data.optLong(PROMOTION_ID_JSON);
			String externalURL = data.optString(EXTERNAL_URL_JSON);
			if(eventId != 0)
				return new Banner(ID,orderNumber, eventId, duration, imgURL, TYPE_EVENT);
			if(promotionId != 0)
				return new Banner(ID,orderNumber, promotionId, duration, imgURL, TYPE_PROMOTION);
			if(SentosaUtils.isValidString(externalURL))	
				return new Banner(ID,orderNumber, externalURL, duration, imgURL, TYPE_EXTERNAL);
			else return null;
		}

		@Override
		public int compareTo(Banner another) {
			int order = getOrderNumber()-another.getOrderNumber();
			if(order==0) order = another.getId() - getId();
			return order;
		}


		@Override
		public String toString() {
			return "Banner [orderNumber=" + orderNumber + ", eventId="
					+ eventId + ", promotionId=" + promotionId
					+ ", displayDuration=" + displayDuration + ", externalURL="
					+ externalURL + ", imageURL=" + imageURL + ", type=" + type
					+ "]";
		}
				
	}

}
