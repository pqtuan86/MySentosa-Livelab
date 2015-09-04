package com.mysentosa.android.sg.request;

import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.toolbox.HttpHeaderParser;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.mysentosa.android.sg.models.Promotion;
import com.mysentosa.android.sg.utils.Const;
import com.mysentosa.android.sg.utils.HttpHelper;
import com.mysentosa.android.sg.utils.LogHelper;
import com.mysentosa.android.sg.utils.SentosaUtils;

import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;

/**
 * Created by randiwaranugraha on 7/23/15.
 */
public class GetPromotionRequest extends Request<ArrayList<Promotion>> {

    public static final String TAG = GetPromotionRequest.class.getSimpleName();

    public static final String URL_PROMOTION_EXCLUSIVE = HttpHelper.BASE_ADDRESSV2 + "promotions/exclusive";
    public static final String URL_PROMOTION_MASTERCARD = HttpHelper.BASE_ADDRESSV2 + "promotions/mastercard";

    private final Response.Listener<ArrayList<Promotion>> listener;

    public GetPromotionRequest(String url, Response.Listener<ArrayList<Promotion>> listener, Response.ErrorListener errorListener) {
        super(Method.GET, url, errorListener);
        this.listener = listener;
    }

    @Override
    protected Response<ArrayList<Promotion>> parseNetworkResponse(NetworkResponse response) {
        try {
            String json = new String(response.data, HttpHeaderParser.parseCharset(response.headers));
            LogHelper.d(TAG, "promotions: " + json);

            int status = new JSONObject(json).optInt(Const.API_ISLANDER_STATUS_CODE, -1);
            if (status == 0) {
                String jsonPromotion = new JSONObject(json).getJSONObject(Const.API_ISLANDER_DATA).getJSONArray(Const.API_ISLANDER_PROMOTIONS).toString();

                Gson gson = new Gson();
                Type collectionType = new TypeToken<Collection<Promotion>>() {
                }.getType();
                ArrayList<Promotion> promotionList = gson.fromJson(jsonPromotion, collectionType);

                return Response.success(promotionList, HttpHeaderParser.parseCacheHeaders(response));
            } else {
                SentosaUtils.errorMessage = new JSONObject(json).getJSONObject(Const.API_ISLANDER_ERROR).optString(Const.API_ISLANDER_MESSAGE, "");
                return Response.success(null, HttpHeaderParser.parseCacheHeaders(response));
            }
        } catch (Exception e) {
            return Response.error(new ParseError(e));
        }
    }

    @Override
    protected void deliverResponse(ArrayList<Promotion> response) {
        listener.onResponse(response);
    }
}