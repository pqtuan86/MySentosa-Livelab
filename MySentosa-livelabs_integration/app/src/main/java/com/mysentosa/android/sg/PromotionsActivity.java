package com.mysentosa.android.sg;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import sg.edu.smu.livelabs.integration.LiveLabsApi;
import sg.edu.smu.livelabs.integration.model.Promotion;
import sg.edu.smu.livelabs.integration.promotion.PromotionDialogFragment;
import sg.edu.smu.livelabs.integration.promotion.PromotionItemAdapter;

/**
 * Created by randiwaranugraha on 7/14/15.
 */
public class PromotionsActivity extends BaseActivity {

    public static final String TAG = "LIVELABS";

    @InjectView(R.id.header_title) TextView headerTitle;
    @InjectView(R.id.list) ListView listView;
    @InjectView(R.id.no_promotion) TextView noPromotion;

    private PromotionItemAdapter promotionItemAdapter;
    private boolean haveNetworkFault;
    private int campaignId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LiveLabsApi.getInstance().onPromotionActivityCreated(this, savedInstanceState);

        setContentView(R.layout.activity_promotions);
        ButterKnife.inject(this);

        headerTitle.setText(R.string.coupons);

        haveNetworkFault = false;

        WifiManager wifi = (WifiManager)getSystemService(this.WIFI_SERVICE);
        ConnectivityManager connManager = (ConnectivityManager) getSystemService(this.CONNECTIVITY_SERVICE);
        NetworkInfo mWifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

        if (!wifi.isWifiEnabled()){
            haveNetworkFault = true;
        }

        //if there is connection error
        if (!mWifi.isConnected()) {
            haveNetworkFault = true;
        }

//        if(haveNetworkFault){
//            new AlertDialog.Builder(PromotionActivity.this)
//                    .setTitle("Wifi")
//                    .setMessage("Cannot connect to network. Please check your WIFI connection.")
//                    .setPositiveButton("OK",  new DialogInterface.OnClickListener() {
//                        public void onClick(DialogInterface dialog, int id) {
//                            finish();
//                            //finishActivity(100);
//                        }
//                    })
//                    .show();
//            return;
//        }

        Intent intent = getIntent();
        boolean isNotification = intent.getBooleanExtra("Notification", false);

        if(isNotification){
            String id = intent.getStringExtra("id");
            LiveLabsApi.getInstance().notificationTracking(id);
        }

        //This stuff is to test the promotion feature
        promotionItemAdapter = new PromotionItemAdapter(this);
        listView.setAdapter(promotionItemAdapter);
        listView.setEmptyView(noPromotion);
        refreshPromotions();

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Promotion p = promotionItemAdapter.getItem(position);
                campaignId = p.getCampaignId();

                LiveLabsApi.getInstance().promotionTracking(Integer.toString(p.getId()), Integer.toString(p.getCampaignId()));

                PromotionDialogFragment f = PromotionDialogFragment.newInstance(p);
                f.show(getSupportFragmentManager(), "dialog");
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        LiveLabsApi.getInstance().onPromotionActivityResumed(this);

    }

    @Override
    protected void onPause() {
        super.onPause();
        LiveLabsApi.getInstance().onPromotionActivityPaused(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LiveLabsApi.getInstance().onPromtionActivityDestroyed(this);
    }

    private void refreshPromotions() {
        LiveLabsApi.getInstance().getPromotions(new LiveLabsApi.PromotionCallback() {
            @Override
            public void onResult(List<Promotion> promotions) {
                promotionItemAdapter.promotionsUpdated(promotions);
            }

            @Override
            public void onError(Throwable t, String message) {
                new AlertDialog.Builder(PromotionsActivity.this)
                        .setTitle("Promotion")
                        .setMessage("Promotion error: " + t)
                        .setPositiveButton("OK", null)
                        .show();
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if(result != null) {
            if(result.getContents() == null) {
                // Log.d("MainActivity", "Cancelled scan");
                // Toast.makeText(this, "Cancelled", Toast.LENGTH_LONG).show();
            } else if(!Integer.toString(campaignId).equals(result.getContents())) {
                new AlertDialog.Builder(PromotionsActivity.this)
                        .setTitle("Redeem")
                        .setMessage("Opps, this is not the correct QR code for this coupon. Please check with the counter.")
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                //System.out.println("refresh");
                                //finish();
                                //startActivity(getIntent());
                            }
                        })
                        .show();

            } else {
                //Log.d("MainActivity", "Scanned");
                //Toast.makeText(this, "Scanned: " + result.getContents(), Toast.LENGTH_LONG).show();

                LiveLabsApi.getInstance().redeemPromotion(result.getContents(),
                        new LiveLabsApi.RedeemPromotionCallback() {

                            @Override
                            public void onResult(String message) {

                                if(message.equals("success")) {
                                    new AlertDialog.Builder(PromotionsActivity.this)
                                            .setTitle("Redeem")
                                            .setMessage("You have successfully redeem this promotion")
                                            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                                public void onClick(DialogInterface dialog, int id) {
                                                    System.out.println("refresh");
                                                    finish();
                                                    startActivity(getIntent());
                                                }
                                            })
                                            .show();
                                }
                                else{
                                    new AlertDialog.Builder(PromotionsActivity.this)
                                            .setTitle("Redeem")
                                            .setMessage(message)
                                            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                                public void onClick(DialogInterface dialog, int id) {
                                                    System.out.println("refresh");
                                                    finish();
                                                    startActivity(getIntent());
                                                }
                                            })
                                            .show();
                                }
                            }

                            @Override
                            public void onError(Throwable t, String message) {
                                new AlertDialog.Builder(PromotionsActivity.this)
                                        .setTitle("Redeem")
                                        .setMessage("Redemption Error: " + t)
                                        .setPositiveButton("OK", null)
                                        .show();
                            }
                        });
            }
        } else {
            Log.d("PromotionActivity", "Weird");
            // This is important, otherwise the result will not be passed to the fragment
            super.onActivityResult(requestCode, resultCode, data);
        }
    }
}