package com.mysentosa.android.sg.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;

import android.util.Log;

import com.mysentosa.android.sg.BuildConfig;

public class HttpHelper {

	public static final String BASE_ADDRESS;
	public static final String BASE_ADDRESSV2;
	public static final String BASE_HOST;
	//public static final String CALLBACK_URL = "http://sentosa-staging.2359media.net/api/ShoppingCart/success";
	public static final String SHOPPINGCART = "ShoppingCart/success";
	private static HttpClient myCustomclient = new DefaultHttpClient();
	private static HttpPost myCustompost;
	

	static {
		if (BuildConfig.DEBUG) {
			BASE_ADDRESS = "http://sentosa-staging.2359media.net/api/";
			BASE_ADDRESSV2 = "http://sentosa-staging.2359media.net/APIv2/";
			BASE_HOST = "http://sentosa-staging.2359media.net";
//			BASE_ADDRESS = "http://sentosa.2359media.net:8000/api/";
//			BASE_ADDRESSV2 = "http://sentosa.2359media.net:8000/APIv2/";
//			BASE_HOST = "http://sentosa.2359media.net:8000";
		} else {
			BASE_ADDRESS = "http://sentosa.2359media.net:8000/api/";
			BASE_ADDRESSV2 = "http://sentosa.2359media.net:8000/APIv2/";
			BASE_HOST = "http://sentosa.2359media.net:8000";
		}
	}

	// STAGING:-
	// public static final String
	// BASE_ADDRESS="http://sentosa-staging.2359media.net/api/";
	// public static final String
	// BASE_HOST="http://sentosa-staging.2359media.net";

	// PRODUCTION:-
	// public static final String
	// BASE_ADDRESS="http://sentosa.2359media.net:8000/api/";
	// public static final String BASE_HOST="http://sentosa.2359media.net:8000";

	public static final String SECRET_API_KEY = "2b63ead0-98d5-11e1-a8b0-0800200c9a66";

	private static final String TAG = " testing";

	public static String sendRequestUsingGet(String uri,
			ArrayList<NameValuePair> params) throws ClientProtocolException,
			IOException {
		HttpGet httpGet = null;
		// -------------Settings for
		// httpClient----------------------------------------//
		//DefaultHttpClient httpClient = new DefaultHttpClient();
		HttpParams httpParams = myCustomclient.getParams();
		HttpConnectionParams.setConnectionTimeout(httpParams, 10000);
		HttpConnectionParams.setSoTimeout(httpParams, 15000);
		// ----------------------------------------------------------------------------//
		String result = "";
		StringBuilder sb = new StringBuilder(uri);

		ResponseHandler<String> handler = new ResponseHandler<String>() {
			@Override
			public String handleResponse(HttpResponse response)
					throws ClientProtocolException, IOException {
				HttpEntity entity = response.getEntity();
				if (entity != null) {
					return EntityUtils.toString(entity);
				} else {
					return null;
				}
			}
		};

		if (params != null) {
			sb.append("?");
			for (int i = 0; i < params.size(); i++) {
				if (i > 0)
					sb.append("&");
				sb.append(URLEncoder.encode(params.get(i).getName(), "UTF-8"))
						.append("=")
						.append(URLEncoder.encode(params.get(i).getValue(),
								"UTF-8"));
			}
		}
		String requestUri = sb.toString();
		Log.d(TAG, requestUri);
		httpGet = new HttpGet(requestUri);
		result = myCustomclient.execute(httpGet, handler);
		Log.d(TAG, "get: " + result);
		return result;
	}

	public static String sendRequestUsingPut(String uri,
			ArrayList<NameValuePair> params) throws ClientProtocolException,
			IOException {
		HttpPut httpPut = null;
		// -------------Settings for
		// httpClient----------------------------------------//
		DefaultHttpClient httpClient = new DefaultHttpClient();
		HttpParams httpParams = httpClient.getParams();
		HttpConnectionParams.setConnectionTimeout(httpParams, 10000);
		HttpConnectionParams.setSoTimeout(httpParams, 15000);
		// ----------------------------------------------------------------------------//
		String result = "";
		StringBuilder sb = new StringBuilder(uri);

		ResponseHandler<String> handler = new ResponseHandler<String>() {
			@Override
			public String handleResponse(HttpResponse response)
					throws ClientProtocolException, IOException {
				HttpEntity entity = response.getEntity();
				if (entity != null) {
					return EntityUtils.toString(entity);
				} else {
					return null;
				}
			}
		};

		if (params != null) {
			sb.append("?");
			for (int i = 0; i < params.size(); i++) {
				if (i > 0)
					sb.append("&");
				sb.append(URLEncoder.encode(params.get(i).getName(), "UTF-8"))
						.append("=")
						.append(URLEncoder.encode(params.get(i).getValue(),
								"UTF-8"));
			}
		}
		String requestUri = sb.toString();
		Log.d(TAG, requestUri);
		httpPut = new HttpPut(requestUri);
		result = httpClient.execute(httpPut, handler);
		Log.d(TAG, "put: " + result);
		return result;
	}

	public static String sendRequestUsingPost(String uri,
			ArrayList<NameValuePair> params) throws ClientProtocolException,
			IOException {
		HttpPost httpPost = null;
		// -------------Settings for
		// httpClient----------------------------------------//
		DefaultHttpClient httpClient = new DefaultHttpClient();
		HttpParams httpParams = httpClient.getParams();
		HttpConnectionParams.setConnectionTimeout(httpParams, 10000);
		HttpConnectionParams.setSoTimeout(httpParams, 15000);
		// ----------------------------------------------------------------------------//
		String result = "";

		ResponseHandler<String> handler = new ResponseHandler<String>() {
			@Override
			public String handleResponse(HttpResponse response)
					throws ClientProtocolException, IOException {
				HttpEntity entity = response.getEntity();
				if (entity != null) {
					return EntityUtils.toString(entity);
				} else {
					return null;
				}
			}
		};

		Log.d(TAG, uri);

		httpPost = new HttpPost(uri);
		httpPost.setEntity(new UrlEncodedFormEntity(params));
		result = httpClient.execute(httpPost, handler);
		Log.d(TAG, "put: " + result);
		return result;
	}

	private static final int MODIFIED = 200;
	private static final int NOT_MODIFIED = 304;

	public static String sendCustomRequestUsingGet(String uri,
			String lastModified) throws ClientProtocolException, IOException {
		HttpGet httpGet = null;
		// -------------Settings for
		// httpClient----------------------------------------//
		DefaultHttpClient httpClient = new DefaultHttpClient();
		HttpParams httpParams = httpClient.getParams();
		HttpConnectionParams.setConnectionTimeout(httpParams, 10000);
		HttpConnectionParams.setSoTimeout(httpParams, 15000);
		// ----------------------------------------------------------------------------//
		String result = "";
		StringBuilder sb = new StringBuilder(uri);

		ResponseHandler<String> handler = new ResponseHandler<String>() {
			@Override
			public String handleResponse(HttpResponse response)
					throws ClientProtocolException, IOException {
				Log.i(TAG, "status code "
						+ response.getStatusLine().getStatusCode());
				if (response.getStatusLine().getStatusCode() == NOT_MODIFIED)
					return null;
				else if (response.getStatusLine().getStatusCode() == MODIFIED) {
					HttpEntity entity = response.getEntity();
					if (entity != null) {
						return EntityUtils.toString(entity);
					} else {
						return null;
					}
				}
				return null;
			}
		};

		String requestUri = sb.toString();
		Log.d(TAG, requestUri);
		httpGet = new HttpGet(requestUri);
		httpGet.setHeader("If-Modified-Since", lastModified);
		result = httpClient.execute(httpGet, handler);
		Log.d(TAG, "get: " + result);
		return result;
	}

	public static InputStream getImageInputStream(String uri,
			String lastModified) throws ClientProtocolException, IOException {
		HttpGet httpGet = null;
		// -------------Settings for
		// httpClient----------------------------------------//
		DefaultHttpClient httpClient = new DefaultHttpClient();
		HttpParams httpParams = httpClient.getParams();
		HttpConnectionParams.setConnectionTimeout(httpParams, 10000);
		HttpConnectionParams.setSoTimeout(httpParams, 15000);
		// ----------------------------------------------------------------------------//
		InputStream is = null;
		StringBuilder sb = new StringBuilder(uri);

		ResponseHandler<InputStream> handler = new ResponseHandler<InputStream>() {
			@Override
			public InputStream handleResponse(HttpResponse response)
					throws ClientProtocolException, IOException {
				Log.d(TAG, " testing status code "
						+ response.getStatusLine().getStatusCode());
				if (response.getStatusLine().getStatusCode() == NOT_MODIFIED)
					return null;
				else if (response.getStatusLine().getStatusCode() == MODIFIED) {
					HttpEntity entity = response.getEntity();
					if (entity != null) {
						return entity.getContent();
					} else {
						return null;
					}
				}
				return null;
			}
		};

		String requestUri = sb.toString();
		Log.d(TAG, requestUri);
		httpGet = new HttpGet(requestUri);
		httpGet.setHeader("If-Modified-Since", lastModified);
		is = httpClient.execute(httpGet, handler);
		// Log.d(TAG,"get: "+result);
		return is;
	}

	public static String sendCustomRequestUsingPost(String url,
			ArrayList<NameValuePair> params) {
		StringBuilder sb = new StringBuilder();
		
		//DefaultHttpClient httpClient = new DefaultHttpClient();
		HttpParams httpParams = myCustomclient.getParams();
		HttpConnectionParams.setConnectionTimeout(httpParams, 20000);
		HttpConnectionParams.setSoTimeout(httpParams, 30000);
				// ----------------------------------------------------------------------------//
				
		try {
			myCustompost = new HttpPost(url);
			myCustompost.setEntity(new UrlEncodedFormEntity(params));
			HttpResponse response = myCustomclient.execute(myCustompost);
			BufferedReader rd = new BufferedReader(new InputStreamReader(
					response.getEntity().getContent()));
			String line = "";

			while ((line = rd.readLine()) != null) {
				System.out.println(line);
				sb.append(line);
			}
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return sb.toString();
	}
}
