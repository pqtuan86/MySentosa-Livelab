package com.mysentosa.android.sg;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.mysentosa.android.sg.custom_views.AspectRatioImageView;
import com.mysentosa.android.sg.custom_views.ToggleGroup;
import com.mysentosa.android.sg.custom_views.ToggleGroup.OnCheckedChangeListener;
import com.mysentosa.android.sg.utils.LogHelper;
import com.mysentosa.android.sg.utils.SentosaUtils;

public class TrafficUpdatesActivity extends BaseActivity {

	private ToggleGroup tg;
	private static final String URL_TWD_TELOK = "http://www.onemotoring.com.sg/trafficsmart/images/4798.html";
	private static final String URL_TWD_SENTOSA = "http://www.onemotoring.com.sg/trafficsmart/images/4799.html";
	
	private Map<String, Drawable> drawableMap = new HashMap<String, Drawable>();
	private AspectRatioImageView ivPhoto;
	private ProgressBar pbLoading;
	private ViewGroup trafficUpdatesContainer;
	private TextView tvTime;
	private long currentCheckId = R.id.tb_twd_sentosa;
	private String time;
	
	
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.traffic_updates_screen);
    	initializeViews();	  
    }


	private void initializeViews() {
		((TextView) findViewById(R.id.header_title)).setText("Traffic Updates");
		ivPhoto = (AspectRatioImageView) this.findViewById(R.id.iv_traffic_update_img);
		pbLoading = (ProgressBar) this.findViewById(R.id.pb_loading);		
		trafficUpdatesContainer = (ViewGroup)this.findViewById(R.id.traffic_update_container);
		tvTime = (TextView)this.findViewById(R.id.tv_traffic_update_time);
		setImage();
		tg = (ToggleGroup) findViewById(R.id.tg_traffic_updates);
			tg.setOnCheckedChangeListener(new OnCheckedChangeListener() {
				@Override
				public void onCheckedChanged(ToggleGroup group, int checkedId) {
					currentCheckId = checkedId;
					setImage();
				}	
			});
	}
	
	
	private void setImage() {
		String url = null;
		if(currentCheckId==R.id.tb_twd_sentosa) {
			url = URL_TWD_SENTOSA;
		}
		else {
    		url = URL_TWD_TELOK;
		}
		if(SentosaUtils.isOnline(TrafficUpdatesActivity.this)) {
			trafficUpdatesContainer.setVisibility(View.GONE);
			pbLoading.setVisibility(View.VISIBLE);
			fetchDrawableOnThread(url);
		}			
		else
			Toast.makeText(TrafficUpdatesActivity.this,"Connection error, kindly check your mobile network availability.",Toast.LENGTH_SHORT).show();
	}
	
	
	private String grabImageUrl(final String url) {
		String imgUrl = null;
		try {
			URL address = new URL(url);
			InputStreamReader pageInput = new InputStreamReader(
					address.openStream());

			BufferedReader source = new BufferedReader(pageInput);
			String sourceLine;
			StringBuilder content = new StringBuilder();
			while ((sourceLine = source.readLine()) != null)
				content.append(sourceLine);
			String regex = "<img\\b[^>]*src\\s*=\\s*\"http://[^>\"]*\"";
			Pattern pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
			Matcher matcher = pattern.matcher(content);
			while (matcher.find()) {
				String find = matcher.group();
				LogHelper.i("findString", find);
				imgUrl = find.substring(find.indexOf("\"") + 1,
						find.lastIndexOf("\""));
				LogHelper.i("imgUrl", imgUrl);
			}
			String regex1 = "(0?[1-9]|[12][0-9]|3[01])/(0?[1-9]|1[012])/((19|20)\\d\\d)\\s([01]?[0-9]|2[0-3]):[0-5][0-9]\\shrs";
			
			Pattern pattern1 = Pattern.compile(regex1, Pattern.CASE_INSENSITIVE);
			Matcher matcher1 = pattern1.matcher(content);
			while (matcher1.find()) {
				String find = matcher1.group();
				LogHelper.i("time", find);
				time = find;
			}
			return imgUrl;
		} catch (Exception e) {
			return null;
		}

	}

	private void fetchDrawableOnThread(final String url) {
		final Handler handler = new Handler() {
			@Override
			public void handleMessage(Message message) {
				pbLoading.setVisibility(View.GONE);
				if (message.obj != null) {
					//iv_small.setVisibility(View.GONE);
					trafficUpdatesContainer.setVisibility(View.VISIBLE);
					ivPhoto.setImageDrawable((Drawable) message.obj);
					if(SentosaUtils.isValidString(time))
						tvTime.setText(time);
					else tvTime.setVisibility(View.GONE);
				}
				else {
					Toast.makeText(TrafficUpdatesActivity.this,"Connection error, kindly check your mobile network availability.",Toast.LENGTH_SHORT).show();
				}
			}
		};

		new Thread() {
			@Override
			public void run() {
				// TODO : set imageView to a "pending" image
				Drawable drawable = null;
				final String imgUrl = grabImageUrl(url);
				if (imgUrl == null || drawableMap.containsKey(imgUrl)) {

					handler.post(new Runnable() {

						@Override
						public void run() {
							if (imgUrl == null) {
								pbLoading.setVisibility(View.GONE);							
								//no url
								Toast.makeText(TrafficUpdatesActivity.this,"Connection error, kindly check your mobile network availability.",Toast.LENGTH_SHORT).show();
							}
							else if (drawableMap.containsKey(imgUrl)) {
								pbLoading.setVisibility(View.GONE);
								//iv_small.setVisibility(View.GONE);
								trafficUpdatesContainer.setVisibility(View.VISIBLE);
								ivPhoto.setImageDrawable(drawableMap
										.get(imgUrl));
								if(SentosaUtils.isValidString(time))
									tvTime.setText(time);
								else tvTime.setVisibility(View.GONE);
							}
						}
					});
					return;
				}

				try {
					DefaultHttpClient httpClient = new DefaultHttpClient();
					HttpGet request = new HttpGet(imgUrl);
					HttpResponse response = httpClient.execute(request);
					InputStream is = response.getEntity().getContent();
					drawable = Drawable.createFromStream(is, "src");
					if (drawable != null)
						drawableMap.put(imgUrl, drawable);
					else
						Log.w("DrawableManager", "could not get thumbnail");
				} catch (Exception e) {

				}
				Message message = handler.obtainMessage(1, drawable);
				handler.sendMessage(message);
			}
		}.start();
	}
    
    
    
   
}