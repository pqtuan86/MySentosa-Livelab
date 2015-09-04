package com.mysentosa.android.sg.helper;

import android.content.Context;
import android.content.SharedPreferences;

import com.mysentosa.android.sg.SentosaApplication;

/**
 * Created by randiwaranugraha on 5/11/15.
 */
public class WeatherData {

    private static final String TAG = WeatherData.class.getSimpleName();

    private static final String WEATHER_PREFS = "com.mysentosa.android.sg.weather.prefs";
    private static final String TIME_OF_READING = "TIME_OF_READING";
    private static final String TEMPERATURE = "TEMPERATURE";
    private static final String ICON_RES_ID = "ICON_RES_ID";

    private static SharedPreferences sharedPreferences;
    private static int iconResID;
    private static int temperature;
    private static long timeOfReading;

    public static void setupData() {
        if(sharedPreferences == null) {
            sharedPreferences = SentosaApplication.appInstance.getSharedPreferences(WEATHER_PREFS, Context.MODE_PRIVATE);
            iconResID = sharedPreferences.getInt(ICON_RES_ID, 0);
            temperature = sharedPreferences.getInt(TEMPERATURE, 999);
            timeOfReading = sharedPreferences.getLong(TIME_OF_READING, -1);
        }
    }

    public static int getIconResID() {
        setupData();
        return iconResID;
    }

    public static int getTemperature() {
        setupData();
        return temperature;
    }

    public static long getTimeOfReading() {
        setupData();
        return timeOfReading;
    }

    public static void save(int iconResId, int temperature, long timeOfReading) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(ICON_RES_ID, iconResId);
        editor.putInt(TEMPERATURE, temperature);
        editor.putLong(TIME_OF_READING, timeOfReading);
        editor.commit();
    }
}