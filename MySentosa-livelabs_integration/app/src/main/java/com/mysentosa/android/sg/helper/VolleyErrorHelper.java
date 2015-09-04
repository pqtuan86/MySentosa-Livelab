package com.mysentosa.android.sg.helper;

import com.android.volley.NetworkError;
import com.android.volley.NoConnectionError;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;

public class VolleyErrorHelper {

	public static String handleError(VolleyError error) {
		if (error instanceof TimeoutError) 
		{
			return ("Request timed out. Please try again later.");
		} 
		else if (isNetworkProblem(error)) {
			return("Please make sure you are connected to Internet and try again.");
		}
		return "";
	}
	
	private static boolean isNetworkProblem(VolleyError error) {
		return (error instanceof NetworkError)
				|| (error instanceof NoConnectionError);
	}
}
