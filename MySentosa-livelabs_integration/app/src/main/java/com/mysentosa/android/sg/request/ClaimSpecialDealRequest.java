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
import com.mysentosa.android.sg.utils.Const;
import com.mysentosa.android.sg.utils.HttpHelper;
import com.mysentosa.android.sg.utils.LogHelper;
import com.mysentosa.android.sg.utils.SentosaUtils;

public class ClaimSpecialDealRequest extends Request<Boolean> {

    private static int method = Request.Method.POST;
    private static String GET_DEALS_URL = HttpHelper.BASE_ADDRESSV2 + "islanderpromotions/claim";
    private final int REQUEST_SUCCESS = 0;
    private final Listener<Boolean> listener;
    private Map<String, String> mParams;
    Context mContext;

    public ClaimSpecialDealRequest(Context context, int memberID, String accessToken, String qrCode, long promotionID,
            Listener<Boolean> listener, ErrorListener errorListener) {
        super(method, String.format(GET_DEALS_URL, memberID, accessToken), errorListener);
        this.mContext = context;
        this.listener = listener;
        mParams = new HashMap<String, String>();
        mParams.put(Const.API_ISLANDER_ISLANDER_ID, String.valueOf(memberID));
        mParams.put(Const.API_ISLANDER_ACCESS_TOKEN, accessToken);
        if (SentosaUtils.isValidString(qrCode)) {
            mParams.put(Const.API_ISLANDER_QRCODE, qrCode);
        }
        mParams.put(Const.API_ISLANDER_PROMOTION_ID, String.valueOf(promotionID));
    }

    @Override
    protected Map<String, String> getParams() throws AuthFailureError {
        return mParams;
    }

    @Override
    protected void deliverResponse(Boolean result) {
        listener.onResponse(result);
    }

    @Override
    protected Response<Boolean> parseNetworkResponse(NetworkResponse response) {
        try {
            String json = new String(response.data, HttpHeaderParser.parseCharset(response.headers));
            LogHelper.d("2359", "data: " + json);
            int status = new JSONObject(json).optInt(Const.API_ISLANDER_STATUS_CODE, -1);
            if (status == REQUEST_SUCCESS) {
                boolean claimed = new JSONObject(json).getJSONObject(Const.API_ISLANDER_DATA).getBoolean(
                        Const.API_ISLANDER_CLAIMED);
                return Response.success(claimed, HttpHeaderParser.parseCacheHeaders(response));
            } else {
                SentosaUtils.errorMessage = new JSONObject(json).getJSONObject(Const.API_ISLANDER_ERROR).optString(
                        Const.API_ISLANDER_MESSAGE, "");
                return Response.success(false, HttpHeaderParser.parseCacheHeaders(response));
            }
        } catch (Exception e) {
            return Response.error(new ParseError(e));
        }
    }
}
