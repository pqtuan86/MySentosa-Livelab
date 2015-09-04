package com.mysentosa.android.sg;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.android.volley.Response.Listener;
import com.flurry.android.FlurryAgent;
import com.mysentosa.android.sg.custom_views.AspectRatioImageView;
import com.mysentosa.android.sg.custom_views.CustomMenu;
import com.mysentosa.android.sg.models.IslanderUser;
import com.mysentosa.android.sg.request.GetIslanderUserDetailRequest;
import com.mysentosa.android.sg.utils.Const.FlurryStrings;
import com.mysentosa.android.sg.utils.SentosaUtils;


public class IslanderUserProfileActivity extends BaseActivity implements OnClickListener {

    private IslanderUser islanderUser;
    private AspectRatioImageView imAvatar;
    private TextView tvTitle, tvName, tvMemberID, tvExpiry, tvPersonalDetails, tvRenewMembership, tvMyClaimedDeals, tvIslanderDeals,
            btLogout;
    private ScrollView svBottom;
    
    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.islander_user_profile);
        initializeViews();
    }

    private void initializeViews() {
        LinearLayout bottomLayout = (LinearLayout) findViewById(R.id.more_detail_part);
        bottomLayout.addView(SentosaUtils.returnBlankFooterView(this, true));
        
        svBottom = (ScrollView) findViewById(R.id.sv_bottom);
        
        tvTitle = (TextView) findViewById(R.id.header_title);
        tvTitle.setText(R.string.islander_profile);

        btLogout = (TextView) findViewById(R.id.bt_text_right);
        btLogout.setVisibility(TextView.VISIBLE);
        btLogout.setText(R.string.islander_logout);

        tvName = (TextView) findViewById(R.id.tv_user_name);
        tvName.setTypeface(((SentosaApplication) this.getApplication()).myridTypeFace);

        tvMemberID = (TextView) findViewById(R.id.tv_user_member_id);
        tvExpiry = (TextView) findViewById(R.id.tv_user_expire_day);
        tvPersonalDetails = (TextView) findViewById(R.id.tv_personal);
        tvRenewMembership = (TextView) findViewById(R.id.tv_renew);
        tvMyClaimedDeals = (TextView) findViewById(R.id.tv_my_claim);
        tvIslanderDeals = (TextView) findViewById(R.id.tv_islander_deals);

        btLogout.setOnClickListener(this);
        tvPersonalDetails.setOnClickListener(this);
        tvRenewMembership.setOnClickListener(this);
        tvMyClaimedDeals.setOnClickListener(this);
        tvIslanderDeals.setOnClickListener(this);
        
    }

    @Override
    public void onClick(View v) {
        Intent intent;
        switch (v.getId()) {
        case R.id.bt_text_right:
            // Log out
            SentosaUtils.resetUserAccount(IslanderUserProfileActivity.this);
            
            //Remove the Islander tag
            SentosaUtils.removeIslanderTag();
            
            startActivity(CustomMenu.createNavigatingIntent(this, HomeActivity.class.getName()));
            finish();
            break;
        case R.id.tv_personal:
            intent = new Intent(this, IslanderUserDetailActivity.class);
            startActivity(intent);
            break;
            
        case R.id.tv_renew:
            FlurryAgent.logEvent(FlurryStrings.IslanderRenew);
            intent = new Intent(this, WebPageActivity.class);
            intent.putExtra(WebPageActivity.PAGE_TYPE, WebPageActivity.PAGE_TYPE_ISLANDER_RENEW);
            startActivity(intent);
            break;
            
        case R.id.tv_my_claim:
            intent = new Intent(this, IslanderMyClaimedDealsActivity.class);
            startActivity(intent);
            break;
            
        case R.id.tv_islander_deals:
            intent = new Intent(this, IslanderDealsActivity.class);
            startActivity(intent);
            break;
        default:
            break;
        }

    }

    private void initData() {
        if (SentosaApplication.mCurrentIslanderUser != null) {
            islanderUser = SentosaApplication.mCurrentIslanderUser;
            fillData();
        } else {
            mPBLoading.show();
            String accessToken = SentosaUtils.getAccessToken(this);
            int memberID = SentosaUtils.getMemberID(this);
            
            if (SentosaUtils.isUserLogined(IslanderUserProfileActivity.this)) {
                GetIslanderUserDetailRequest request = new GetIslanderUserDetailRequest(this,
                        memberID, accessToken, mResponseListener,
                        mErrorListener);
                SentosaApplication.mRequestQueue.add(request);
            } else {
                finish();
            }
        }
    }
    
    private void fillData() {
        tvName.setText(islanderUser.getFullName());
        tvMemberID.setText("Member ID: "+islanderUser.getAccountNumber());
        tvExpiry.setText("Expiry: "+islanderUser.getMembershipExpiry());
    }

    Listener<IslanderUser> mResponseListener = new Listener<IslanderUser>() {

        @Override
        public void onResponse(IslanderUser user) {
            dismissProgressDialog();
            if (user != null) {
                SentosaApplication.mCurrentIslanderUser = user;
                islanderUser = user;
                fillData();
            } else {
                //If fail to get detail's user, reset SharedPre and move to Islander home screen to re-login
                SentosaUtils.resetUserAccount(IslanderUserProfileActivity.this);
                
                Intent intent = new Intent(IslanderUserProfileActivity.this, IslanderActivity.class);
                startActivity(intent);
                finish();
            }

        }
    };
    
    @Override
    protected void onResume() {
        super.onResume();
        initData();
    }
    
}
