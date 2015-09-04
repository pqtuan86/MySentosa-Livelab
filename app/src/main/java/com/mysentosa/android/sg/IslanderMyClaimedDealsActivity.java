package com.mysentosa.android.sg;

import java.util.ArrayList;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.TextView;

import com.android.volley.Response.Listener;
import com.flurry.android.FlurryAgent;
import com.google.gson.Gson;
import com.mysentosa.android.sg.adapter.ClaimedPromotionAdapter;
import com.mysentosa.android.sg.models.GetMyClaimedDealsRequestModel;
import com.mysentosa.android.sg.models.Promotion;
import com.mysentosa.android.sg.provider.utils.SentosaDatabaseStructure.EventsPromotionsBase;
import com.mysentosa.android.sg.request.GetMyClaimedDealsRequest;
import com.mysentosa.android.sg.utils.Const.FlurryStrings;
import com.mysentosa.android.sg.utils.SentosaUtils;

public class IslanderMyClaimedDealsActivity extends BaseActivity {

    private ListView lvListOfDeals;
    private TextView tvTitle;
    private ArrayList<Promotion> promotionList;
    private int currentPage = 1;

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.events_promo_list_screen);
        initializeViews();
    }

    private void initializeViews() {
        tvTitle = (TextView) findViewById(R.id.header_title);
        tvTitle.setText(R.string.islander_my_claimed_deals);

        promotionList = new ArrayList<Promotion>();
        lvListOfDeals = (ListView) findViewById(R.id.list);
        SentosaUtils.addListViewFooter(this, lvListOfDeals, true);
        getClaimedDeals();
        lvListOfDeals.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> arg0, View view, int arg2, long arg3) {
                Intent intent = new Intent();
                if (promotionList.get(arg2).isFreeRewardType() || promotionList.get(arg2).isOnSiteType()) {
                    intent.setClass(IslanderMyClaimedDealsActivity.this, IslanderSpecialDealActivity.class);
                    intent.putExtra(IslanderSpecialDealActivity.CURRENT_DEAL,
                            new Gson().toJson(promotionList.get(arg2)));
                } else if (promotionList.get(arg2).isDiscountType()){
                    intent.setClass(IslanderMyClaimedDealsActivity.this, TicketsActivity.class);
                    intent.putExtra(TicketsActivity.TICKET_TYPE, promotionList.get(arg2).getDicountedTicketEntityType());
                    intent.putExtra(TicketsActivity.START_FROM_DEAL_SCREEN, true);
                    intent.putExtra(NavigationManagerActivity.ACTIVITY_TO_START_ID, promotionList.get(arg2).getDiscountedTicketEntityId());
                } else {
                    intent.setClass(IslanderMyClaimedDealsActivity.this, EventsAndPromotionsDetailActivity.class);
                    FlurryAgent.logEvent(FlurryStrings.PromotionsDetailsPromotions);
                    intent.putExtra(EventsAndPromotionsDetailActivity.CURRENT_TYPE,
                            EventsAndPromotionsDetailActivity.TYPE_PROMOTION);
                    intent.putExtra(EventsPromotionsBase.ID_COL, promotionList.get(arg2).getId());
                    intent.putExtra(EventsAndPromotionsDetailActivity.ISLANDER_CLAIMED_DEAL,
                            new Gson().toJson(promotionList.get(arg2)));
                }
                startActivity(intent);
            }
        });
    }

    private void getClaimedDeals() {
        mPBLoading.show();
        GetMyClaimedDealsRequest request = new GetMyClaimedDealsRequest(this, SentosaUtils.getMemberID(this),
                SentosaUtils.getAccessToken(this), currentPage, mResponseListener, mErrorListener);
        SentosaApplication.mRequestQueue.add(request);
    }

    Listener<GetMyClaimedDealsRequestModel> mResponseListener = new Listener<GetMyClaimedDealsRequestModel>() {

        @Override
        public void onResponse(GetMyClaimedDealsRequestModel result) {
            promotionList.addAll(result.listPromotions);
            if (currentPage < result.pageSize) {
                currentPage +=1;
                getClaimedDeals();
            } else {
                dismissProgressDialog();
                ClaimedPromotionAdapter adapter = new ClaimedPromotionAdapter(IslanderMyClaimedDealsActivity.this,
                        promotionList);
                SentosaApplication.mClaimedDeals = promotionList;
                lvListOfDeals.setAdapter(adapter);
                currentPage = 1;
            }
            
        }
    };
}
