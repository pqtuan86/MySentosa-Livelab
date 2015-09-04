package sg.edu.smu.livelabs.integration.promotion;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Point;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

import sg.edu.smu.livelabs.integration.LiveLabsApi;
import sg.edu.smu.livelabs.integration.R;
import sg.edu.smu.livelabs.integration.model.Promotion;


/**
 *
 *  This Fragment handles the details of the promotion selected from PromotionActivity.
 *  Created by Le Gia Hai on 18/5/2015.
 *  Edited by John on 1 July 2015
 */
public class PromotionDialogFragment extends DialogFragment {
    public static final String TAG = "LIVELABS";
    private static final String PROMOTION_KEY = "promotion";
    private ImageView logoView;
    private ImageView photoView;
    private TextView headerView;
    private TextView titleView;
    private TextView merchantView;
    private TextView descriptionView;
    private TextView merchantNameView;
    private TextView merchantOperatingHourView;
    private TextView merchantLocationView;
    private TextView merchantPhoneView;
    private TextView merchantEmailView;
    private TextView merchantWebView;
    private TextView validDateView;

    private TextView merchantOperatingHourTitleView;
    private TextView merchantLocationTitleView;
    private TextView merchantPhoneTitleView;
    private TextView merchantEmailTitleView;
    private TextView merchantWebVTitleiew;

    private TextView descriptionTitleView;
    private TextView merchanteTitleView;
    private TextView validDateTitleView;

    private LinearLayout merchantLayout;
    private LinearLayout descriptionLayout;
    private LinearLayout validDateLayout;

    private LinearLayout merchantOperatingHourLayoutView;
    private LinearLayout merchantLocationLayoutView;
    private LinearLayout merchantPhoneLayoutView;
    private LinearLayout merchantEmailLayoutView;
    private LinearLayout merchantWebLayoutView ;

    private ImageView merchantButton;
    private ImageView descriptionButton;
    private ImageView validDateButton;

    private Button redeemButton;

    private boolean merchantStatus;
    private boolean descriptionStatus;
    private boolean validDateStatus;

    Context mContext;

    public static PromotionDialogFragment newInstance(Promotion promotion) {
        PromotionDialogFragment f = new PromotionDialogFragment();
        Bundle args = new Bundle();
        args.putSerializable(PROMOTION_KEY, promotion);
        f.setArguments(args);
        return f;
    }

    private Promotion promotion;

    public PromotionDialogFragment() {
        setStyle(STYLE_NORMAL, android.R.style.Theme_Holo_Light_DarkActionBar);
        mContext = getActivity();
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        promotion = (Promotion) getArguments().getSerializable(PROMOTION_KEY);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.promotion_detail_view, container, false);
        getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);

        DisplayMetrics metrics = getResources().getDisplayMetrics();
        int densityDpi = (int)(metrics.density * 160f);

        Typeface tfSemiBold = Typeface.createFromAsset(getActivity().getAssets(), "font/MyriadPro-Semibold.otf");
        Typeface tfRegular = Typeface.createFromAsset(getActivity().getAssets(), "font/MyriadPro-Regular.otf");


       // logoView = (ImageView) view.findViewById(R.id.logo_view);
        photoView = (ImageView) view.findViewById(R.id.photo_view);
        //campaignNameView = (TextView) view.findViewById(R.id.campaignName_txt);
        headerView = (TextView) view.findViewById(R.id.title);
        titleView = (TextView) view.findViewById(R.id.title_txt);
        merchantView = (TextView) view.findViewById(R.id.merchant_txt);
        descriptionView = (TextView) view.findViewById(R.id.description_txt);
        validDateView = (TextView) view.findViewById(R.id.valid_date_txt);

        merchantNameView = (TextView) view.findViewById(R.id.merchant_name);
        merchantOperatingHourView = (TextView) view.findViewById(R.id.merchant_operating_hour);
        merchantLocationView = (TextView) view.findViewById(R.id.merchant_location);
        merchantPhoneView = (TextView) view.findViewById(R.id.merchant_phone);
        merchantEmailView = (TextView) view.findViewById(R.id.merchant_email);
        merchantWebView = (TextView) view.findViewById(R.id.merchant_web);

        merchantOperatingHourTitleView = (TextView) view.findViewById(R.id.merchant_operating_title);
        merchantLocationTitleView = (TextView) view.findViewById(R.id.merchant_location_title);
        merchantPhoneTitleView = (TextView) view.findViewById(R.id.merchant_phone_title);
        merchantEmailTitleView = (TextView) view.findViewById(R.id.merchant_email_title);
        merchantWebVTitleiew = (TextView) view.findViewById(R.id.merchant_web_title);


        merchantOperatingHourLayoutView = (LinearLayout) view.findViewById(R.id.merchant_operating_layout);
        merchantLocationLayoutView = (LinearLayout) view.findViewById(R.id.merchant_location_layout);
        merchantPhoneLayoutView = (LinearLayout) view.findViewById(R.id.merchant_phone_layout);
        merchantEmailLayoutView = (LinearLayout) view.findViewById(R.id.merchant_email_layout);
        merchantWebLayoutView = (LinearLayout) view.findViewById(R.id.merchant_web_layout);


        descriptionTitleView  = (TextView) view.findViewById(R.id.description_header);
        merchanteTitleView  = (TextView) view.findViewById(R.id.merchant_header);
        validDateTitleView  = (TextView) view.findViewById(R.id.valid_date_header);

        headerView.setTypeface(tfSemiBold);
        titleView.setTypeface(tfSemiBold);
        descriptionTitleView.setTypeface(tfSemiBold);
        merchanteTitleView.setTypeface(tfSemiBold);
        validDateTitleView.setTypeface(tfSemiBold);

        merchantOperatingHourTitleView.setTypeface(tfSemiBold);
        merchantLocationTitleView.setTypeface(tfSemiBold);
        merchantPhoneTitleView.setTypeface(tfSemiBold);
        merchantEmailTitleView.setTypeface(tfSemiBold);
        merchantWebVTitleiew.setTypeface(tfSemiBold);
        merchantNameView.setTypeface(tfSemiBold);

        descriptionView.setTypeface(tfRegular);
        validDateView.setTypeface(tfRegular);
        merchantView.setTypeface(tfRegular);

        merchantOperatingHourView.setTypeface(tfRegular);
        merchantLocationView.setTypeface(tfRegular);
        merchantPhoneView.setTypeface(tfRegular);
        merchantEmailView.setTypeface(tfRegular);
        merchantWebView.setTypeface(tfRegular);


        merchantLayout = (LinearLayout) view.findViewById(R.id.merchant_layout);
        descriptionLayout = (LinearLayout) view.findViewById(R.id.description_layout);
        validDateLayout = (LinearLayout) view.findViewById(R.id.valid_date_layout);

        //image button
        merchantButton = (ImageView) view.findViewById(R.id.merchant_button);
        descriptionButton = (ImageView) view.findViewById(R.id.description_button);
        validDateButton = (ImageView) view.findViewById(R.id.valid_date_button);

        redeemButton = (Button) view.findViewById(R.id.redeemButton);
        redeemButton.setTypeface(tfRegular);

        /**
         * The status will determine the appearance of the redeem button.
         * There are 2 status, "sent" will allow the redeem button to be displayed, while "redeemed" will have the button removed
         */
        if(promotion.getStatus().toLowerCase().equals("redeemed")){
            //redeemButton.setVisibility(View.GONE);
            redeemButton.setBackgroundColor(0xFF666666);
            redeemButton.setText("This item have been redeemed.");
            redeemButton.setEnabled(false);
            redeemButton.setTextColor(0xffffffff);
        }

        //initialize default status, false to hidden the information
        merchantStatus = false;
        descriptionStatus = true;
        validDateStatus = false;

        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Picasso.with(getActivity()).load(promotion.getImage().toString()).into(photoView);
        //Picasso.with(getActivity()).load(promotion.getPhoto().toString()).into(photoView);

        //campaignNameView.setText(promotion.getCampaignName());
        titleView.setText(promotion.getTitle());
        merchantView.setText(promotion.getDetails());
        descriptionView.setText(promotion.getDescription());

        merchantNameView.setText(promotion.getMerchantName());
        merchantOperatingHourView.setText(promotion.getWorkingHours());
        merchantLocationView.setText(promotion.getMerchantLocation());
        merchantPhoneView.setText(promotion.getMerchantPhone());
        merchantEmailView.setText(promotion.getMerchantEmail());
        merchantWebView.setText(promotion.getMerchantWeb());

        SimpleDateFormat df = new SimpleDateFormat("MM/dd/yyyy HH:mm");
        SimpleDateFormat yf = new SimpleDateFormat("yyyy");

        String validDate = df.format(promotion.getStartTime()) + " to " + df.format(promotion.getEndTime());
        String year = yf.format(promotion.getStartTime());
        System.out.println("Year:>>>>>> " + year);

        if(Integer.parseInt(year) <= 1970){
            validDateView.setText("Anytime");
        }
        else{
            validDateView.setText(validDate);
        }




        //Onclick functions
        redeemButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //result callback to PromotionActivity
                Display display = getActivity().getWindowManager().getDefaultDisplay();
                Point size = new Point();
                display.getSize(size);

                IntentIntegrator integrator = new IntentIntegrator(getActivity());
                integrator.setScanningRectangle(size.y, size.x);
                integrator.initiateScan();

            }
        });

        merchantLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!merchantStatus) {
                    merchantButton.setImageResource(R.drawable.expandbutton);
                    merchantView.setVisibility(View.VISIBLE);

                    merchantNameView.setVisibility(View.VISIBLE);
                    merchantLocationLayoutView.setVisibility(View.VISIBLE);

                    if (!merchantOperatingHourView.getText().toString().matches("")) {
                        merchantOperatingHourLayoutView.setVisibility(View.VISIBLE);
                    }

                    if (!merchantPhoneView.getText().toString().matches("")) {
                        merchantPhoneLayoutView.setVisibility(View.VISIBLE);
                    }

                    if (!merchantEmailView.getText().toString().matches("")) {
                        merchantEmailLayoutView.setVisibility(View.VISIBLE);
                    }

                    if (!merchantWebView.getText().toString().matches("")) {
                        merchantWebLayoutView.setVisibility(View.VISIBLE);
                    }

                    merchantStatus = true;
                } else {
                    merchantButton.setImageResource(R.drawable.collapsebutton);
                    merchantView.setVisibility(View.GONE);
                    merchantNameView.setVisibility(View.GONE);
                    merchantLocationLayoutView.setVisibility(View.GONE);
                    merchantOperatingHourLayoutView.setVisibility(View.GONE);
                    merchantPhoneLayoutView.setVisibility(View.GONE);
                    merchantEmailLayoutView.setVisibility(View.GONE);
                    merchantWebLayoutView.setVisibility(View.GONE);
                    merchantStatus = false;
                }
            }
        });

        descriptionLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!descriptionStatus) {
                    descriptionButton.setImageResource(R.drawable.expandbutton);
                    descriptionView.setVisibility(View.VISIBLE);
                    descriptionStatus = true;
                }
                else{
                    descriptionButton.setImageResource(R.drawable.collapsebutton);
                    descriptionView.setVisibility(View.GONE);
                    descriptionStatus = false;
                }
            }
        });

        validDateLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!validDateStatus) {
                    validDateButton.setImageResource(R.drawable.expandbutton);
                    validDateView.setVisibility(View.VISIBLE);
                    validDateStatus = true;
                } else {
                    validDateButton.setImageResource(R.drawable.collapsebutton);
                    validDateView.setVisibility(View.GONE);
                    validDateStatus = false;
                }
            }
        });
    }


}
