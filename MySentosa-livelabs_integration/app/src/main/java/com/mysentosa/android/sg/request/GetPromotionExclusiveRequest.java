package com.mysentosa.android.sg.request;

import android.content.Context;

import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.mysentosa.android.sg.models.Promotion;
import com.mysentosa.android.sg.utils.HttpHelper;

import java.util.ArrayList;

public class GetPromotionExclusiveRequest extends GetPromotionRequest {

    public GetPromotionExclusiveRequest(Context context, Listener<ArrayList<Promotion>> listener, ErrorListener errorListener) {
        super(URL_PROMOTION_EXCLUSIVE, listener, errorListener);
    }
}