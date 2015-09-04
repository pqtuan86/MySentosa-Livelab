package com.mysentosa.android.sg.utils;

import android.util.Log;

public class LogHelper {
	//if staging, set true
	//if production, set false to hide all log.
	private static Boolean canLog = true;
	
	public static void i (String tag, String msg) {
		if (canLog) {
			Log.i(tag, msg);
		}
	}
	
	public static void d (String tag, String msg) {
		if (canLog) {
			Log.d(tag, msg);
		}
	}
	
	public static void e (String tag, String msg) {
		if (canLog) {
			Log.e(tag, msg);
		}
	}
	
	public static void v (String tag, String msg) {
		if (canLog) {
			Log.v(tag, msg);
		}
	}
	
	public static void w (String tag, String msg) {
		if (canLog) {
			Log.w(tag, msg);
		}
	}
}
