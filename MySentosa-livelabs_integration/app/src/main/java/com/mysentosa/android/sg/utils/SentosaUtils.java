package com.mysentosa.android.sg.utils;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Set;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONObject;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Environment;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.text.format.Time;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.ListView;
import android.widget.Toast;

import com.android.volley.NetworkResponse;
import com.android.volley.toolbox.HttpHeaderParser;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.mysentosa.android.sg.R;
import com.mysentosa.android.sg.SentosaApplication;
import com.mysentosa.android.sg.models.Promotion;
import com.urbanairship.push.PushManager;

public class SentosaUtils {

	public synchronized static String getDataFromFileInAssetsToString(
			String fileName, Context ctx) {
		try {
			InputStream instream = ctx.getAssets().open(fileName);
			BufferedInputStream bif = new BufferedInputStream(instream);
			byte[] tmp = new byte[instream.available()];
			bif.read(tmp);
			instream.close();
			bif.close();
			String result = new String(tmp);
			return result;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "";
	}

	/*
	 * public static void CopyStream(InputStream is, OutputStream os) { final
	 * int buffer_size = 1024; try { byte[] bytes = new byte[buffer_size]; for
	 * (;;) { int count = is.read(bytes, 0, buffer_size); if (count == -1)
	 * break; os.write(bytes, 0, count); } } catch (Exception ex) { } }
	 */

	public static Time getCurrentTime() {
		Time currentTime = new Time();
		currentTime.setToNow();
		return currentTime;
	}

	public static long getCurrentTimeMillis() {
		return getCurrentTime().toMillis(false);
	}

	public static boolean isValidString(String s) {
		if (s != null)
			s.trim();
		if (s == null || s.equals("") || s.toLowerCase().equals("null"))
			return false;
		else
			return true;
	}

	public static boolean isOnline(Context context) {
		ConnectivityManager cm = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo netInfo = cm.getActiveNetworkInfo();
		if (netInfo != null && netInfo.isConnectedOrConnecting()) {
			return true;
		}
		return false;
	}

	public static void startToCall(Context context, String contact_number) {
		Intent callIntent = new Intent(Intent.ACTION_CALL);
		callIntent.setData(Uri.parse("tel:" + contact_number));
		context.startActivity(callIntent);
	}

	public static int getResourceId(Context context, String fileName) {
		Resources mResource = context.getResources();
		return mResource.getIdentifier(fileName, "drawable",
				context.getPackageName());
	}

	public static String removeNewLine(String description) {
		return description.replaceAll("\\\\n", "\\\n");
	}

	public static boolean validateString(String validateString,
			String regexString) {
		Pattern pattern = Pattern.compile(regexString);
		Matcher matcher = pattern.matcher(validateString);
		return matcher.matches();
	}

	public static void addListViewFooter(Context c, ListView lv,
			boolean setTransparentBackground) {
		View v = View.inflate(c, R.layout.item_thingstodo_bookmarks_list, null);
		v.setClickable(false);
		if (setTransparentBackground) {
			v.setBackgroundColor(Color.TRANSPARENT);
			lv.setCacheColorHint(Color.TRANSPARENT);
		}
		v.findViewById(R.id.iv_right_arrow);
		if (v != null)
			v.setVisibility(View.GONE);

		float px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 75, c
				.getResources().getDisplayMetrics());
		v.setLayoutParams(new android.widget.AbsListView.LayoutParams(
				LayoutParams.FILL_PARENT, (int) px));
		lv.addFooterView(v, null, false);
		lv.setFooterDividersEnabled(false);
	}

	public static View addListViewFooter(Context c, int layoutId, ListView lv,
			boolean setTransparentBackground) {
		View v = View.inflate(c, layoutId, null);
		v.setClickable(false);
		if (setTransparentBackground) {
			v.setBackgroundColor(Color.TRANSPARENT);
			lv.setCacheColorHint(Color.TRANSPARENT);
		}
		if (v != null)
			v.setVisibility(View.GONE);

		lv.addFooterView(v, null, false);
		lv.setFooterDividersEnabled(false);
		return v;
	}

	public static View returnBlankFooterView(Context c, boolean setTransparentBackground) {
	    View v = View.inflate(c, R.layout.item_thingstodo_bookmarks_list, null);
        v.setClickable(false);
        if (setTransparentBackground) {
            v.setBackgroundColor(Color.TRANSPARENT);
        }
        v.findViewById(R.id.iv_right_arrow);
        if (v != null)
            v.setVisibility(View.GONE);

        float px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 75, c
                .getResources().getDisplayMetrics());
        v.setLayoutParams(new android.widget.AbsListView.LayoutParams(
                LayoutParams.FILL_PARENT, (int) px));
        return v;
	}
	
	public static SimpleDateFormat getLastModifiedFormatter() {

		SimpleDateFormat sdf = new SimpleDateFormat(
				"E, dd MMM yyyy HH:mm:ss zzz");
		sdf.setTimeZone(TimeZone.getTimeZone("GMT-0"));

		return sdf;
	}

	public static void copyDatabase(Context c) {
		String databasePath = c.getDatabasePath("sentosadb.db").getPath();
		File f = new File(databasePath);
		OutputStream myOutput = null;
		InputStream myInput = null;

		if (f.exists()) {
			try {

				File directory = new File(Environment
						.getExternalStorageDirectory().getPath()
						+ "/SENTOSA_DB_DEBUG");
				if (!directory.exists())
					directory.mkdir();

				myOutput = new FileOutputStream(directory.getAbsolutePath()
						+ "/" + "sentosadb.db");
				myInput = new FileInputStream(databasePath);

				byte[] buffer = new byte[1024];
				int length;
				while ((length = myInput.read(buffer)) > 0) {
					myOutput.write(buffer, 0, length);
				}

				myOutput.flush();
			} catch (Exception e) {
			} finally {
				try {
					if (myOutput != null) {
						myOutput.close();
						myOutput = null;
					}
					if (myInput != null) {
						myInput.close();
						myInput = null;
					}
				} catch (Exception e) {
				}
			}
		}
	}

	public static String getDoubleNumber(String number) {
		if (Integer.parseInt(number) < 10)
			return "0" + number;
		else
			return number;
	}

	public static BigDecimal DoFormat(String myNumber) {
		BigDecimal TotalAmount = new BigDecimal(myNumber);
		return TotalAmount.divide(new BigDecimal(1), 2, RoundingMode.HALF_UP);
	}

	// return 00.00 foramt
	private static BigDecimal getDecimalValues(float amount) {
		return DoFormat(String.valueOf(amount));
	}

	// Format S$624.00
	public static String getFormatValues(float amount) {
		return SentosaApplication.appInstance.getResources().getString(
				R.string.ticket_cart_total_value, getDecimalValues(amount));
	}

	// Format 3x Adult tickets - S$ 65.00	
	public static String getHeadingAllFormatValues(String value, String name , float amount) {
		return SentosaApplication.appInstance.getResources().getString(
				R.string.ticket_cart_format, value, name, getDecimalValues(amount));
	}
	
	//Format 3x Local (Adult) ticket - S$ 65.00
	public static String getHeadingAllFormatValues(String count, String content, String type , float amount) {
		return SentosaApplication.appInstance.getResources().getString(
				R.string.ticket_cart_format_2, count, content, type, getDecimalValues(amount));
	}
	

	// Format S$ 624.00
	public static String getBookingFormatValues(float amount) {
		return SentosaApplication.appInstance.getResources().getString(
				R.string.ticket_selection_event_select_ticket,
				getDecimalValues(amount));
	}

	// Format 1 x name S$ 26.00
	public static String getShopingFormatValues(float amount, String name,
			int qty) {
		return SentosaApplication.appInstance.getResources().getString(
				R.string.ticket_shopingcart_format, qty, name,
				getDecimalValues(amount));
	}

	public static String getDeviceID() {
		String device_id = null;
		TelephonyManager telephonyManager = (TelephonyManager) SentosaApplication.appInstance
				.getSystemService(Context.TELEPHONY_SERVICE);
		device_id = telephonyManager.getDeviceId();
		if (device_id == null) {
			device_id = Settings.Secure.getString(
					SentosaApplication.appInstance.getContentResolver(),
					Settings.Secure.ANDROID_ID);
		}
		return device_id;
	}
	
	public static long getTimeStamp() {
		return new Date().getTime() / 1000L;
	}
	
	public static Date getDate(String timestamp) {
		long dv = Long.valueOf(timestamp) * 1000;// its need to be in milisecond
		Date df = new java.util.Date(dv);
		//return Const.sdf.format(df);
		return df;
	}
	
	public static String errorMessage = "";
    public static int getErrorCodeFromErrorResponse(NetworkResponse response) {
        try {
            if (response != null) {
                String json = new String(response.data, HttpHeaderParser.parseCharset(response.headers));
                Log.d("2359", "data error: " + json);
                JSONObject obj = new JSONObject(json);
                return obj.optInt(Const.API_ISLANDER_STATUS_CODE, -1);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return -1;
    }
    
    public static String getErrorMessageFromErrorResponse(NetworkResponse response) {
        try {
            if (response != null) {
                String json = new String(response.data, HttpHeaderParser.parseCharset(response.headers));
                Log.d("2359", "data error: " + json);
                JSONObject obj = new JSONObject(json);
                String errorMess = obj.getJSONObject(Const.API_ISLANDER_ERROR).optString(Const.API_ISLANDER_MESSAGE, "");
                return errorMess;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }
    
    public static void saveMemberID(Context context, int memberID) {
        SharedPreferences prefs = context.getSharedPreferences(Const.SHARED_PREFERENCE, Context.MODE_PRIVATE);
        prefs.edit().putInt(Const.MEMBERSHIP_ID, memberID).commit();
    }
    
    public static int getMemberID(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(Const.SHARED_PREFERENCE, Context.MODE_PRIVATE);
        int memberID = prefs.getInt(Const.MEMBERSHIP_ID, -1);
        return memberID;
    }
    
    public static void saveAccessToken(Context context, String accessToken) {
        SharedPreferences prefs = context.getSharedPreferences(Const.SHARED_PREFERENCE, Context.MODE_PRIVATE);
        prefs.edit().putString(Const.ACCESS_TOKEN, accessToken).commit();
    }
    
    public static String getAccessToken(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(Const.SHARED_PREFERENCE, Context.MODE_PRIVATE);
        String accessToken = prefs.getString(Const.ACCESS_TOKEN, "");
        return accessToken;
    }
    
    public static void saveUserDOB(Context context, String dob) {
        SharedPreferences prefs = context.getSharedPreferences(Const.SHARED_PREFERENCE, Context.MODE_PRIVATE);
        prefs.edit().putString(Const.DATE_OF_BIRTH, dob).commit();
    }
    
    public static String getUserDOB(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(Const.SHARED_PREFERENCE, Context.MODE_PRIVATE);
        String dob = prefs.getString(Const.DATE_OF_BIRTH, "");
        return dob;
    }
    
    public static void resetUserAccount(Context context) {
        saveAccessToken(context, "");
        saveMemberID(context, 0);
        SentosaApplication.mClaimedDeals = null;
        SentosaApplication.mCurrentIslanderUser = null;
    }
    
    public static boolean isUserLogined(Context context) {
        String accessToken = SentosaUtils.getAccessToken(context);
        if (SentosaUtils.isValidString(accessToken)) {
            return true;
        }
        return false;
    }
    
    public static String returnText (String inputString) {
        if (inputString == null || inputString.equals("null")) {
            return "";
        }
        return inputString;
    }
    
    public static void saveListOfBoughtTicketID(Context context, ArrayList<Integer> listOfTicket) {
        SharedPreferences prefs = context.getSharedPreferences(Const.SHARED_PREFERENCE, Context.MODE_PRIVATE);
        prefs.edit().putString(Const.BOUGHT_TICKET, new Gson().toJson(listOfTicket).toString()).commit();
    }
    
    public static ArrayList<Integer> getListOfBoughtTicketID(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(Const.SHARED_PREFERENCE, Context.MODE_PRIVATE);
        String json = prefs.getString(Const.BOUGHT_TICKET, "");
        Type collectionType = new TypeToken<Collection<Integer>>(){}.getType();
        return (ArrayList<Integer>) new Gson().fromJson(json, collectionType);
    }
    
    public static void saveListOfClaimedTicketIDFailed(Context context, ArrayList<Integer> listOfTicket) {
        SharedPreferences prefs = context.getSharedPreferences(Const.SHARED_PREFERENCE, Context.MODE_PRIVATE);
        prefs.edit().putString(Const.CLAIMED_TICKET_FAILED, new Gson().toJson(listOfTicket).toString()).commit();
    }
    
    public static ArrayList<Integer> getListOfClaimedTicketIDFailed(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(Const.SHARED_PREFERENCE, Context.MODE_PRIVATE);
        String json = prefs.getString(Const.CLAIMED_TICKET_FAILED, "");
        Type collectionType = new TypeToken<Collection<Integer>>(){}.getType();
        return (ArrayList<Integer>) new Gson().fromJson(json, collectionType);
    }
    
    public static void addIslanderTag() {
        Set<String> setTags =  PushManager.shared().getTags();
        setTags.add(Const.UA_ISLANDER_TAG);
        PushManager.shared().setTags(setTags);
    }
    
    public static void removeIslanderTag() {
        Set<String> setTags =  PushManager.shared().getTags();
        setTags.remove(Const.UA_ISLANDER_TAG);
        PushManager.shared().setTags(setTags);
    }
    
    public static void showToast(Context context, String msg) {
    	Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
    }
}
