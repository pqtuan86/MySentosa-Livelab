package sg.edu.smu.livelabs.integration.promotion;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Point;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.hardware.Camera;
import android.hardware.Camera.AutoFocusCallback;
import android.hardware.Camera.PreviewCallback;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.text.Html;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import net.sourceforge.zbar.Config;
import net.sourceforge.zbar.Image;
import net.sourceforge.zbar.ImageScanner;
import net.sourceforge.zbar.Symbol;
import net.sourceforge.zbar.SymbolSet;

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
    private TextView validDateFromView;
    private TextView validDateToView;

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
    private LinearLayout containerView;

    private ImageView merchantButton;
    private ImageView descriptionButton;
    private ImageView validDateButton;

    private FrameLayout preview;

    private TextView promotionTextCode;
    private Button redeemButton;

    private boolean merchantStatus;
    private boolean descriptionStatus;
    private boolean validDateStatus;

    //For camera API
    private Camera mCamera;
    private CameraPreview mPreview;
    private Handler autoFocusHandler;

    private TextView scanText;
    private Button scanButton;

    private int iconSize;
    private ImageScanner scanner;

    private boolean barcodeScanned = false;
    private boolean previewing = true;

    Context mContext;

    public static PromotionDialogFragment newInstance(Promotion promotion) {
        PromotionDialogFragment f = new PromotionDialogFragment();
        Bundle args = new Bundle();
        args.putSerializable(PROMOTION_KEY, promotion);
        f.setArguments(args);

        return f;
    }

    private Promotion promotion;
    private PromotionDialogListener promotionDialogListener;

    public static interface PromotionDialogListener {
        void onPromotionDialogCanceled();
    }

    public PromotionDialogFragment() {
        setStyle(STYLE_NORMAL, android.R.style.Theme_Holo_Light_DarkActionBar);
        mContext = getActivity();
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        promotionDialogListener = (PromotionDialogListener) getActivity();
        promotion = (Promotion) getArguments().getSerializable(PROMOTION_KEY);

        Display display = getActivity().getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        iconSize = size.x / 5;
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

//        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        autoFocusHandler = new Handler();
        mCamera = getCameraInstance();

        /* Instance barcode scanner */
        scanner = new ImageScanner();
        scanner.setConfig(0, Config.X_DENSITY, 3);
        scanner.setConfig(0, Config.Y_DENSITY, 3);

       // logoView = (ImageView) view.findViewById(R.id.logo_view);
        photoView = (ImageView) view.findViewById(R.id.photo_view);
        //campaignNameView = (TextView) view.findViewById(R.id.campaignName_txt);
        headerView = (TextView) view.findViewById(R.id.title);
        titleView = (TextView) view.findViewById(R.id.title_txt);
        merchantView = (TextView) view.findViewById(R.id.merchant_txt);
        descriptionView = (TextView) view.findViewById(R.id.description_txt);
        validDateFromView = (TextView) view.findViewById(R.id.valid_date_from_txt);
        validDateToView = (TextView) view.findViewById(R.id.valid_date_to_txt);

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
        validDateFromView.setTypeface(tfRegular);
        validDateToView.setTypeface(tfRegular);
        merchantView.setTypeface(tfRegular);

        merchantOperatingHourView.setTypeface(tfRegular);
        merchantLocationView.setTypeface(tfRegular);
        merchantPhoneView.setTypeface(tfRegular);
        merchantEmailView.setTypeface(tfRegular);
        merchantWebView.setTypeface(tfRegular);

        containerView = (LinearLayout) view.findViewById(R.id.container);


        merchantLayout = (LinearLayout) view.findViewById(R.id.merchant_layout);
        descriptionLayout = (LinearLayout) view.findViewById(R.id.description_layout);
        validDateLayout = (LinearLayout) view.findViewById(R.id.valid_date_layout);

        //image button
        merchantButton = (ImageView) view.findViewById(R.id.merchant_button);
        descriptionButton = (ImageView) view.findViewById(R.id.description_button);
        validDateButton = (ImageView) view.findViewById(R.id.valid_date_button);

        promotionTextCode = (TextView) view.findViewById(R.id.promotion_code);
        promotionTextCode.setTypeface(tfSemiBold);
        redeemButton = (Button) view.findViewById(R.id.redeemButton);
        redeemButton.setTypeface(tfRegular);

        preview = (FrameLayout)view.findViewById(R.id.cameraPreview);
        if (preview != null) {
            mPreview = new CameraPreview(getActivity(), mCamera, previewCb, autoFocusCB);
            preview.addView(mPreview);
        }




        /**
         * The status will determine the appearance of the redeem button.
         * There are 2 status, "sent" will allow the redeem button to be displayed, while "redeemed" will have the button removed
         */
        System.out.println("Status: " + promotion.getStatus());
        if(promotion.getStatus().toLowerCase().equals("redeemed")){
            //redeemButton.setVisibility(View.GONE);
            redeemButton.setBackgroundColor(0xFF666666);
            redeemButton.setText("This item has been redeemed. S/N: " + promotion.getSerialNumber());
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

        SimpleDateFormat df = new SimpleDateFormat("MM/dd/yyyy");
        SimpleDateFormat yf = new SimpleDateFormat("yyyy");

        String validDate = df.format(promotion.getStartTime()) + " to " + df.format(promotion.getEndTime());
        String year = yf.format(promotion.getStartTime());
        //System.out.println("Year:>>>>>> " + year);

        if(Integer.parseInt(year) <= 1970){
            validDateFromView.setText("Anytime");
        }
        else{;
            validDateFromView.setText(Html.fromHtml("<b>From: </b>" + df.format(promotion.getStartTime())) );
            validDateToView.setText(Html.fromHtml("<b>To: </b>" + df.format(promotion.getEndTime())) );
        }


        if (promotion.isRegRequired() && !promotion.getStatus().toLowerCase().equals("redeemed")){
            promotionTextCode.setText(Html.fromHtml("<b> Promo code: " + promotion.getRegDiscountCode()));
            promotionTextCode.setVisibility(View.VISIBLE);
            redeemButton.setText("REGISTER NOW!");
            redeemButton.setEnabled(true);
            redeemButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String url = String.format("%s?mac=%s&campaignId=%s&code=%s",
                            promotion.getRegUrl(),
                            LiveLabsApi.getInstance().macAddressSHA1,
                            promotion.getCampaignId(),
                            promotion.getRegDiscountCode());
                    Intent i = new Intent(Intent.ACTION_VIEW);
                    i.setData(Uri.parse(url));
                    startActivity(i);
//                    RegistrationDialogFragment f = RegistrationDialogFragment.newInstance(promotion);
//                    f.show(getActivity().getSupportFragmentManager(), "dialog");
                }
            });

        } else {
            //Onclick functions
            promotionTextCode.setVisibility(View.GONE);
            redeemButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    redeemButton.setText("VERIFYING");
                    redeemButton.setEnabled(false);
                    redeemButton.setBackgroundColor(0xFF666666);
                    redeemButton.setTextColor(0xffffffff);
                    preview.setVisibility(View.VISIBLE);
                    containerView.setVisibility(View.GONE);

                    //For using ZBar QR code scanner lib
                    if (barcodeScanned) {
                        barcodeScanned = false;
                        mCamera.setPreviewCallback(previewCb);
                        mCamera.startPreview();
                        previewing = true;
                        mCamera.autoFocus(autoFocusCB);
                    }


                    //For using ZXing QR code scanner lib
//                //result callback to PromotionActivity
//                IntentIntegrator integrator = new IntentIntegrator(getActivity());
//
//                Display display = getActivity().getWindowManager().getDefaultDisplay();
//                Point size = new Point();
//                display.getSize(size);
//
//
//                integrator.setScanningRectangle(size.y, size.x);
//                List<String> type = new ArrayList<String>();
//                type.add("QR_CODE");
//                integrator.initiateScan(type);


                }
            });
        }



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
                    validDateFromView.setVisibility(View.VISIBLE);
                    validDateToView.setVisibility(View.VISIBLE);
                    validDateStatus = true;
                } else {
                    validDateButton.setImageResource(R.drawable.collapsebutton);
                    validDateToView.setVisibility(View.GONE);
                    validDateFromView.setVisibility(View.GONE);
                    validDateStatus = false;
                }
            }
        });
    }


    ////////////////////// for ZBar QR scanning Camera API 21 and below //////////////////////////////////////
    public void onPause() {
        super.onPause();
        releaseCamera();
    }

    /** A safe way to get an instance of the Camera object. */
    public static Camera getCameraInstance(){
        Camera c = null;
        try {
            c = Camera.open();
        } catch (Exception e){
        }
        return c;
    }

    private void releaseCamera() {
        if (mCamera != null) {
            previewing = false;
            mCamera.setPreviewCallback(null);
            mCamera.release();
            mCamera = null;
        }
    }

    private Runnable doAutoFocus = new Runnable() {
        public void run() {
            if (previewing)
                mCamera.autoFocus(autoFocusCB);
        }
    };

    PreviewCallback previewCb = new Camera.PreviewCallback() {
        public void onPreviewFrame(byte[] data, Camera camera) {
            Camera.Parameters parameters = camera.getParameters();
            Camera.Size size = parameters.getPreviewSize();

            Image barcode = new Image(size.width, size.height, "Y800");
            barcode.setData(data);

            int result = scanner.scanImage(barcode);

            if (result != 0) {
                previewing = false;
                mCamera.setPreviewCallback(null);
                mCamera.stopPreview();

                SymbolSet syms = scanner.getResults();
                for (Symbol sym : syms) {
                    preview.setVisibility(View.GONE);
                    containerView.setVisibility(View.VISIBLE);
                    System.out.println("barcode result " + sym.getData());
                    onDialog(sym.getData());
                    barcodeScanned = true;
                }
            }
        }
    };

    // Mimic continuous auto-focusing
    AutoFocusCallback autoFocusCB = new AutoFocusCallback() {
        public void onAutoFocus(boolean success, Camera camera) {
            autoFocusHandler.postDelayed(doAutoFocus, 1000);
        }
    };

    private void onDialog(String data){


        if (data == null) {
            Log.d("PromotionFragment", "werid scan");

            redeemButton.setEnabled(true);
        }
        else if(!Integer.toString(promotion.getCampaignId()).equals(data) ){
            showDialog("Opps, this is not the correct QR code for this coupon. Please check with the counter.", false);

            redeemButton.setEnabled(true);
            redeemButton.setText("REDEEM");
            redeemButton.setBackgroundColor(0xFF000000);
            redeemButton.setTextColor(0xffffffff);

        }
        else {

            LiveLabsApi.getInstance().redeemPromotion(data,
                    new LiveLabsApi.RedeemPromotionCallback() {

                        @Override
                        public void onResult(String status, String message) {

                            if(status.equals("success")) {
                                showDialog(message, true);
                                redeemButton.setEnabled(false);
                                redeemButton.setText("This item has been redeemed. S/N: " + promotion.getSerialNumber());
                                redeemButton.setBackgroundColor(0xFF666666);
                                redeemButton.setTextColor(0xffffffff);
                            }
                            else{
                                showDialog(message, false);

                                redeemButton.setEnabled(true);
                                redeemButton.setText("REDEEM");
                                redeemButton.setBackgroundColor(0xFF000000);
                                redeemButton.setTextColor(0xffffffff);
                            }
                        }

                        @Override
                        public void onError(Throwable t, String message) {

                            showDialog("Opps, something went wrong, please try again later", false);

                            redeemButton.setEnabled(true);
                            redeemButton.setText("REDEEM");
                            redeemButton.setBackgroundColor(0xFF000000);
                            redeemButton.setTextColor(0xffffffff);

                        }
                    });
        }
    }

    private void showDialog(String message, boolean isSuccess){

        final Dialog dialog = new Dialog(getActivity());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_box);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));


        TextView title = (TextView) dialog.findViewById(R.id.main_text);
        TextView serial = (TextView) dialog.findViewById(R.id.serial);
        TextView text = (TextView) dialog.findViewById(R.id.message);
        final Button button = (Button) dialog.findViewById(R.id.button);
        ImageView icon = (ImageView) dialog.findViewById(R.id.icon);
        LinearLayout  container = (LinearLayout) dialog.findViewById(R.id.container);

        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        RelativeLayout.LayoutParams iconParams = new RelativeLayout.LayoutParams( iconSize, iconSize);
        iconParams.setMargins(0, (iconSize / 2), 0, 0);
        iconParams.addRule(RelativeLayout.CENTER_HORIZONTAL);

        layoutParams.setMargins(30, iconSize, 30, 0);
        container.setLayoutParams(layoutParams);

        icon.setLayoutParams(iconParams);

        if(isSuccess){
            title.setText("Success");
            button.setBackgroundResource(R.drawable.button_green_bg);
            serial.setVisibility(View.VISIBLE);
            serial.setText("S/N: " + message);
            text.setText("Please show the serial number to the merchant before closing this message");
            Picasso.with(getActivity()).load(R.drawable.success_icon).into(icon);

            new CountDownTimer(30000, 1000) {//CountDownTimer(edittext1.getText()+edittext2.getText()) also parse it to long

                public void onTick(long millisUntilFinished) {
                    dialog.setCancelable(false);
                    button.setEnabled(false);
                    button.setText("" + millisUntilFinished / 1000);
                    button.setTextColor(0xffffffff);
                }

                public void onFinish() {
                    dialog.setCancelable(true);
                    button.setEnabled(true);
                    button.setText("Close");
                }
            }
                    .start();

            dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialog) {
                    getActivity().finish();
                    startActivity(getActivity().getIntent());
                }
            });
        }
        else{
            title.setText("Redeemption Failed");
            serial.setVisibility(View.GONE);
            text.setText(message);
            button.setBackgroundResource(R.drawable.button_red_bg);
            button.setText("Close");
        }


        dialog.show();

        button.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        super.onCancel(dialog);
        promotionDialogListener.onPromotionDialogCanceled();
    }



}
