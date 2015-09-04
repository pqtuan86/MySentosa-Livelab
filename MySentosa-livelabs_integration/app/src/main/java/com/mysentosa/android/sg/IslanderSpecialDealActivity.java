package com.mysentosa.android.sg;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.VolleyError;
import com.google.gson.Gson;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.mysentosa.android.sg.custom_views.AspectRatioImageView;
import com.mysentosa.android.sg.helper.AlertDialogHelper;
import com.mysentosa.android.sg.helper.ProgressDialogHelper;
import com.mysentosa.android.sg.helper.VolleyErrorHelper;
import com.mysentosa.android.sg.imageloader.ImageFetcher;
import com.mysentosa.android.sg.models.Promotion;
import com.mysentosa.android.sg.request.ClaimSpecialDealRequest;
import com.mysentosa.android.sg.utils.HttpHelper;
import com.mysentosa.android.sg.utils.SentosaUtils;

public class IslanderSpecialDealActivity extends Activity {

    //This activity is used for FREE-REWARD and ON-SITE deal type
    public static final String CURRENT_DEAL = "current_deal";
    private static final int SCAN_QR_CODE = 1;
    public static final String SCAN_QR_RESULT = "qr_code_data";
    private AspectRatioImageView imageView;
    private TextView mTxtTilte, mTxtDetail;
    private Button btnClaim;
    private Promotion currentDeal;
    private ImageFetcher mImageWorker;
    private ProgressBar mProgress;
    private Dialog dialog;
    private ProgressDialogHelper mPBLoading;
    private AlertDialogHelper alertDialog;

    //only use for ON-SITE deal
    private TextView tvRewardLeft;

    private String claimText = "";
    private String claimedText = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tickets_detail);
        mImageWorker = ((SentosaApplication) this.getApplication()).mImageFetcher;
        String claimedDealJSON = getIntent().getStringExtra(CURRENT_DEAL);
        if (SentosaUtils.isValidString(claimedDealJSON)) {
            currentDeal = new Gson().fromJson(claimedDealJSON, Promotion.class);
            initializeViews();
        } else {
            finish();
        }
    }

    private void initializeViews() {

        mPBLoading = new ProgressDialogHelper(this);
        LinearLayout titleLayout = (LinearLayout) findViewById(R.id.layout_title_exclusive);
        titleLayout.setVisibility(LinearLayout.VISIBLE);
        TextView tvIslanderTitle = (TextView) findViewById(R.id.tv_islander_exclusive);
        tvIslanderTitle.setTypeface(((SentosaApplication) this.getApplication()).myridTypeFace);

        mProgress = (ProgressBar) findViewById(R.id.pb_loading);
        imageView = (AspectRatioImageView) findViewById(R.id.iv_detail_item);

        String imageUrl = currentDeal.getImageURL();
        if (SentosaUtils.isValidString(imageUrl)) {
            mImageWorker.loadImage(HttpHelper.BASE_HOST + imageUrl, imageView, mProgress,
                    R.drawable.bg_gradient_img, false, null);
        }


        mTxtTilte = (TextView) findViewById(R.id.tv_title);
        mTxtTilte.setText(currentDeal.getTitle());

        mTxtDetail = (TextView) findViewById(R.id.tv_description);
        mTxtDetail.setText(SentosaUtils.returnText(currentDeal.getDescription()) + "\n\n" + SentosaUtils.returnText(currentDeal.getDetail()));
        mTxtDetail.setMovementMethod(new ScrollingMovementMethod());

        btnClaim = (Button) findViewById(R.id.btn_purchase);

        if (currentDeal.isFreeRewardType()) {
            claimText = getString(R.string.islander_participate);
            claimedText = getString(R.string.islander_participated);
        } else if (currentDeal.isOnSiteType()) {
            claimText = getString(R.string.islander_scan);
            claimedText = getString(R.string.islander_claimed);
            if (currentDeal.isRewardDisplayMessaged()) {
                tvRewardLeft = (TextView) findViewById(R.id.tv_onsite_reward_left);
                tvRewardLeft.setVisibility(TextView.VISIBLE);
                tvRewardLeft
                        .setText((currentDeal.getRewardQuantity() - currentDeal.getRewardQuantityCount()) + " " + getString(R.string.islander_reward_left_unclaimed));
            }
        }
        btnClaim.setText(claimText);

        btnClaim.setTypeface(((SentosaApplication) this.getApplication()).myridTypeFace);
        btnClaim.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (currentDeal.isFreeRewardType()) {
                    claimFreeDeal();
                } else if (currentDeal.isOnSiteType()) {
                    IntentIntegrator integrator = new IntentIntegrator(IslanderSpecialDealActivity.this);
                    integrator.initiateScan();
                }
            }
        });
        setUpClaimSuccessDialog();

        setClaimButtonStatus(true);
        if (SentosaApplication.mClaimedDeals != null) {
            for (Promotion claimedDeal : SentosaApplication.mClaimedDeals) {
                if (currentDeal.getId() == claimedDeal.getId()) {
                    setClaimButtonStatus(false);
                    break;
                }
            }
        } else {
            finish();
        }
    }

    private void claimFreeDeal() {
        mPBLoading.show();
        ClaimSpecialDealRequest request = new ClaimSpecialDealRequest(this, SentosaUtils.getMemberID(this),
                SentosaUtils.getAccessToken(this), "", currentDeal.getId(), mResponseListener, mErrorListener);
        SentosaApplication.mRequestQueue.add(request);
    }

    private void setUpClaimSuccessDialog() {
        dialog = new Dialog(IslanderSpecialDealActivity.this, R.style.CustomDialog);
        dialog.setContentView(R.layout.dialog_claim_success);

        TextView tvSuccess = (TextView) dialog.findViewById(R.id.tv_title);
        tvSuccess.setTypeface(((SentosaApplication) this.getApplication()).myridTypeFace);
        Button btOK = (Button) dialog.findViewById(R.id.bt_ok);
        btOK.setTypeface(((SentosaApplication) this.getApplication()).myridTypeFace);
        btOK.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
    }

    Listener<Boolean> mResponseListener = new Listener<Boolean>() {

        @Override
        public void onResponse(Boolean result) {
            mPBLoading.dismiss();
            if (result) {
                // Reset MyClaimed array
                SentosaApplication.mClaimedDeals = null;
                if (currentDeal.isOnSiteType()) {
                    dialog.findViewById(R.id.tv_title_detail).setVisibility(TextView.GONE);
                    if (currentDeal.isRewardDisplayMessaged()) {
                        tvRewardLeft
                                .setText((currentDeal.getRewardQuantity() - currentDeal.getRewardQuantityCount() - 1) + " " + getString(R.string.islander_reward_left_unclaimed));
                    }
                } else {
                    dialog.findViewById(R.id.tv_title_detail).setVisibility(TextView.VISIBLE);
                }
                dialog.show();
                setClaimButtonStatus(false);
            } else {
                showErrorDialog(SentosaUtils.errorMessage);
            }
        }
    };

    ErrorListener mErrorListener = new ErrorListener() {

        @Override
        public void onErrorResponse(VolleyError error) {
            dismissProgressDialog();
            String errorText = "";
            if (SentosaUtils.isValidString(SentosaUtils.getErrorMessageFromErrorResponse(error.networkResponse))) {
                errorText = SentosaUtils.getErrorMessageFromErrorResponse(error.networkResponse);
            } else {
                errorText = VolleyErrorHelper.handleError(error);
            }

            if (SentosaUtils.isValidString(errorText)) {
                showErrorDialog(errorText);
            }
        }
    };

    void showErrorDialog(String message) {
        alertDialog = new AlertDialogHelper(this, "Error", message, "OK");
        alertDialog.show();
    }

    void dismissProgressDialog() {
        if (mPBLoading != null) {
            mPBLoading.dismiss();
        }
    }

    private void setClaimButtonStatus(boolean isEnable) {
        if (isEnable) {
            btnClaim.setText(claimText);
            btnClaim.setBackgroundResource(R.drawable.standard_button_yellow);
            btnClaim.setEnabled(true);
        } else {
            btnClaim.setText(claimedText);
            btnClaim.setBackgroundResource(R.drawable.standard_button_yellow_darker);
            btnClaim.setEnabled(false);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        IntentResult scanResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (scanResult != null) {
            String qrCode = scanResult.getContents();
            Log.d("2359", "Scan Result: " + qrCode);
            mPBLoading.show();
            ClaimSpecialDealRequest request = new ClaimSpecialDealRequest(IslanderSpecialDealActivity.this,
                    SentosaUtils.getMemberID(IslanderSpecialDealActivity.this),
                    SentosaUtils.getAccessToken(IslanderSpecialDealActivity.this), qrCode, currentDeal.getId(), mResponseListener,
                    mErrorListener);
            SentosaApplication.mRequestQueue.add(request);
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }
}
