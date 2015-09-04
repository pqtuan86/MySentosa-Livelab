package com.mysentosa.android.sg.request;

import java.util.HashMap;
import java.util.Map;

import org.json.JSONObject;

import android.content.Context;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.toolbox.HttpHeaderParser;
import com.google.gson.Gson;
import com.mysentosa.android.sg.models.IslanderUser;
import com.mysentosa.android.sg.utils.Const;
import com.mysentosa.android.sg.utils.HttpHelper;
import com.mysentosa.android.sg.utils.LogHelper;
import com.mysentosa.android.sg.utils.SentosaUtils;

public class LoginRequest extends Request<IslanderUser> {

    private static int method = Request.Method.POST;
    private static String LOGIN_URL = HttpHelper.BASE_ADDRESSV2 + "islanders/login";
    private final int LOGIN_SUCCESS = 0;
    private final Listener<IslanderUser> listener;
    private Map<String, String> mParams;
    Context mContext;

    public LoginRequest(Context context, String memberID, String dob, Listener<IslanderUser> listener,
            ErrorListener errorListener) {
        super(method, LOGIN_URL, errorListener);
        this.mContext = context;
        this.listener = listener;
        mParams = new HashMap<String, String>();
        mParams.put(Const.API_ISLANDER_ACCOUNT_NUMBER, memberID);
        mParams.put(Const.API_ISLANDER_BIRTHDAY, dob);

    }

    @Override
    protected Map<String, String> getParams() throws AuthFailureError {
        return mParams;
    }

    @Override
    protected void deliverResponse(IslanderUser result) {
        listener.onResponse(result);
    }

    @Override
    protected Response<IslanderUser> parseNetworkResponse(NetworkResponse response) {
        try {
            String json = new String(response.data, HttpHeaderParser.parseCharset(response.headers));
            LogHelper.d("2359", "data: " + json);
            int status = new JSONObject(json).optInt(Const.API_ISLANDER_STATUS_CODE, -1);
            if (status == LOGIN_SUCCESS) {
                String jsonUser = new JSONObject(json).getJSONObject(Const.API_ISLANDER_DATA).toString();
                Gson gson = new Gson();
                IslanderUser user = gson.fromJson(jsonUser, IslanderUser.class);
                return Response.success(user, HttpHeaderParser.parseCacheHeaders(response));
            } else {
                SentosaUtils.errorMessage = new JSONObject(json).getJSONObject(Const.API_ISLANDER_ERROR).optString(
                        Const.API_ISLANDER_MESSAGE, "");
                return Response.success(null, HttpHeaderParser.parseCacheHeaders(response));
            }
        } catch (Exception e) {
            return Response.error(new ParseError(e));
        }
    }
}
