package com.mysentosa.android.sg.custom_views;

import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.NodeList;

import android.content.Context;
import android.os.AsyncTask;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.mysentosa.android.sg.R;
import com.mysentosa.android.sg.utils.LogHelper;


public class CurrentWeatherView extends LinearLayout {
	//We use Yahoo Weather APIs
	private final int SingaporeStationCode = 24703053;
	private final String weatherUrlString = "http://weather.yahooapis.com/forecastrss?w="+SingaporeStationCode+"&u=c";
	
	private int iconResID = 0, temperature = 999; //junk default values
	private long timeOfReading = -1; //-1 indicates reading is not set
	private ImageView weatherIcon;
	private TextView weatherText;
	private final int MIN_WEATHER_REFRESH_TIME = 0;//1800000; //half an hour
	
	private boolean isWeatherPullTaskRunning = false;
	
	public CurrentWeatherView(Context context) {
		super(context);
		initializeView();
	}

	public CurrentWeatherView(Context context, AttributeSet attrs) {
		super(context, attrs);
		initializeView();
	}

	public CurrentWeatherView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		initializeView();
	}
	
	public void initializeView() {
		LayoutInflater inflater = (LayoutInflater) getContext()
		.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		inflater.inflate(R.layout.current_weather, this);
		
		weatherText = (TextView) this.findViewById(R.id.tv_home_screen_weather);
		weatherIcon = (ImageView) this.findViewById(R.id.iv_home_screen_weather_icon);
	}
	
	///////////////////////////TO UPDATE THE WEATHER////////////////////////////////////////////////////////
	public void updateCurrentWeatherValues() {
		if(!isWeatherPullTaskRunning && (timeOfReading==-1 || (System.currentTimeMillis()-timeOfReading)>MIN_WEATHER_REFRESH_TIME)) {
			//if async task not already running and (if no reading has been taken OR if last reading was half an hour ago)
			new WeatherInfoPullTask().execute();
		} else if(timeOfReading!=-1 && (System.currentTimeMillis()-timeOfReading)<=MIN_WEATHER_REFRESH_TIME) {
			displayWeatherViews(true); //display the weather views if timeOfReading is sufficiently recent
		}
	}
	
	///////////////////////////TO SHOW/HIDE WEATHER VIEWS///////////////////////////////////////////////////
	public void displayWeatherViews(boolean shouldShow) {
		if(shouldShow) {
			weatherText.setText(temperature+"\u2103");
			weatherIcon.setImageResource(iconResID);
			CurrentWeatherView.this.findViewById(R.id.ll_current_weather_container).setVisibility(View.VISIBLE);
		} else {
			resetWeatherData();
			CurrentWeatherView.this.findViewById(R.id.ll_current_weather_container).setVisibility(View.GONE);
		}
	}
	
	///////////////////////////HELPER METHODS////////////////////////////////////////////////////////
	private void resetWeatherData() {
		iconResID = 0;
		temperature = 999;
		timeOfReading = -1;
	}
	
	private Integer getImageResID(int code) {
		if((code>=19 && code<=30) || code==44)
			return R.drawable.weather_cloudy;
		else if(code>=31 && code<=36) {
			return R.drawable.weather_sunny;
		}
		return R.drawable.weather_rainy;
	}
	
	///////////////////////////GETTERS/SETTERS FOR WEATHER DATA///////////////////////////////////////////
	public Integer getIconResID() {
		return iconResID;
	}

	public Integer getTemperature() {
		return temperature;
	}

	public long getTimeOfReading() {
		return timeOfReading;
	}

	public void setCurrentWeatherData(long timeOfReading, int temperature, int iconResID) {
		this.iconResID = iconResID;
		this.timeOfReading = timeOfReading;
		this.temperature = temperature;
	}
	
	///////////////////////////TO DOWNLOAD DATA/////////////////////////////////////////////////////////////
	public class WeatherInfoPullTask extends AsyncTask<Void, Void, Boolean> {
		@Override
		protected void onPreExecute() {
			isWeatherPullTaskRunning = true;
		}
		
		@Override
		protected void onPostExecute(Boolean success) {
			displayWeatherViews(success);
			isWeatherPullTaskRunning = false;
		}
		
		@Override
		protected Boolean doInBackground(Void... params) {
			Document dom;
			
			try {
				URL weatherUrl = new URL(weatherUrlString);

				URLConnection con = weatherUrl.openConnection();
				con.setReadTimeout(60000); 
				InputStream is = con.getInputStream();
				
				DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
	    		DocumentBuilder builder;
				builder = factory.newDocumentBuilder();
				dom = builder.parse(is);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return false;
			}

			Element root = dom.getDocumentElement(); //rss element
			if (root.getTagName() == null || root.getTagName().equals("error")) return false;
			
			NodeList yWeatherCondList = root.getElementsByTagName("yweather:condition");
			if (yWeatherCondList == null || yWeatherCondList.getLength()==0) return false;
			
			NamedNodeMap yWeatherCond = yWeatherCondList.item(0).getAttributes();
			
			iconResID = getImageResID(Integer.parseInt(yWeatherCond.getNamedItem("code").getNodeValue()));
			temperature = Integer.parseInt(yWeatherCond.getNamedItem("temp").getNodeValue());
			timeOfReading = System.currentTimeMillis();
			
			LogHelper.d("test","test iconResID: "+iconResID+" temperature: "+temperature+" timeOfReading: "+timeOfReading);
			return true;
		}
	}
	
}