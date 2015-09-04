package com.mysentosa.android.sg.request;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;

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
import com.google.gson.reflect.TypeToken;
import com.mysentosa.android.sg.models.GetMyClaimedDealsRequestModel;
import com.mysentosa.android.sg.models.Promotion;
import com.mysentosa.android.sg.utils.Const;
import com.mysentosa.android.sg.utils.HttpHelper;
import com.mysentosa.android.sg.utils.LogHelper;
import com.mysentosa.android.sg.utils.SentosaUtils;

public class GetMyClaimedDealsRequest extends Request<GetMyClaimedDealsRequestModel> {

    private static int method = Request.Method.GET;
    private static String GET_DEALS_URL = HttpHelper.BASE_ADDRESSV2 + "islanders/claimlist?islanderID=%d&accessToken=%s&page=%d";
    private final int REQUEST_SUCCESS = 0;
    private final Listener<GetMyClaimedDealsRequestModel> listener;
    Context mContext;

    public GetMyClaimedDealsRequest(Context context, int memberID, String accessToken, int page, Listener<GetMyClaimedDealsRequestModel> listener,
            ErrorListener errorListener) {
        super(method, String.format(GET_DEALS_URL, memberID, accessToken, page), errorListener);
        this.mContext = context;
        this.listener = listener;

    }

    @Override
    protected void deliverResponse(GetMyClaimedDealsRequestModel result) {
        listener.onResponse(result);
    }

    @Override
    protected Response<GetMyClaimedDealsRequestModel> parseNetworkResponse(NetworkResponse response) {
        try {
            GetMyClaimedDealsRequestModel requestResult = new GetMyClaimedDealsRequestModel();
            
            String json = new String(response.data, HttpHeaderParser.parseCharset(response.headers));
            LogHelper.d("2359", "data my claimed: " + json);
            int status = new JSONObject(json).optInt(Const.API_ISLANDER_STATUS_CODE, -1);
            if (status == REQUEST_SUCCESS) {
                requestResult.pageSize = new JSONObject(json).getJSONObject(Const.API_ISLANDER_DATA).getInt("PageCount");
                String jsonPromotion = new JSONObject(json).getJSONObject(Const.API_ISLANDER_DATA)
                        .getJSONArray(Const.API_ISLANDER_PROMOTIONS).toString();
                Gson gson = new Gson();
                Type collectionType = new TypeToken<Collection<Promotion>>(){}.getType();
                ArrayList<Promotion> promotionList = gson.fromJson(jsonPromotion, collectionType);
                requestResult.listPromotions = promotionList;
                return Response.success(requestResult, HttpHeaderParser.parseCacheHeaders(response));
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
