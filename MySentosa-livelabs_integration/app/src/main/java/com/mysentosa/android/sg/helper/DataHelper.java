package com.mysentosa.android.sg.helper;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * Created by randiwaranugraha on 3/27/15.
 */
public class DataHelper {

    private static final String TAG = DataHelper.class.getSimpleName();

    private static SharedPreferences sharedPreferences;
    private static SharedPreferences.Editor editor;

    public static void init(Context context) {
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        editor = sharedPreferences.edit();
    }

    public static void saveData(String key, String value) {
        editor.putString(key, value);
        editor.commit();
    }

    public static void saveData(String key, long value) {
        editor.putLong(key, value);
        editor.commit();
    }

    public static void saveData(String key, boolean value) {
        editor.putBoolean(key, value);
        editor.commit();
    }

    public static String getString(String key) {
        String value = sharedPreferences.getString(key, null);
        return value;
    }

    public static long getLong(String key) {
        long value = sharedPreferences.getLong(key, 0);
        return value;
    }

    public static boolean getBoolean(String key) {
        boolean value = sharedPreferences.getBoolean(key, false);
        return value;
    }
}