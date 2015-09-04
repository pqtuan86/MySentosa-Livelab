package com.mysentosa.android.sg.request;

import org.json.JSONObject;

import android.content.Context;
import android.util.Log;

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

public class GetIslanderUserDetailRequest extends Request<IslanderUser> {

    private static int method = Request.Method.GET;
    private static String GET_DETAIL_URL = HttpHelper.BASE_ADDRESSV2 + "islanders/detail?islanderID=%d&accessToken=%s";
    private final int REQUEST_SUCCESS = 0;
    private final Listener<IslanderUser> listener;
    Context mContext;

    public GetIslanderUserDetailRequest(Context context, int memberID, String accessToken, Listener<IslanderUser> listener,
            ErrorListener errorListener) {
        super(method, String.format(GET_DETAIL_URL, memberID, accessToken), errorListener);
        this.mContext = context;
        this.listener = listener;

    }

    @Override
    protected void deliverResponse(IslanderUser result) {
        listener.onResponse(result);
    }

    @Override
    protected Response<IslanderUser> parseNetworkResponse(NetworkResponse response) {
        try {
            String json = new String(response.data, HttpHeaderParser.parseCharset(response.headers));
            LogHelper.d("2359", "data user: " + json);
            int status = new JSONObject(json).optInt(Const.API_ISLANDER_STATUS_CODE, -1);
            if (status == REQUEST_SUCCESS) {
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
