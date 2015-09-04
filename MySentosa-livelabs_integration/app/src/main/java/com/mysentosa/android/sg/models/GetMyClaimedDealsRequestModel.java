package com.mysentosa.android.sg.models;

import java.util.ArrayList;

public class GetMyClaimedDealsRequestModel {
    public int pageSize;
    public ArrayList<Promotion> listPromotions;
    
    public GetMyClaimedDealsRequestModel() {
        super();
        pageSize = 0;
        listPromotions = new ArrayList<Promotion>();
    }
    
}
