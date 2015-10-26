package sg.edu.smu.livelabs.integration;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.util.List;

import javax.net.ssl.SSLHandshakeException;

import sg.edu.smu.livelabs.integration.model.Promotion;
import sg.edu.smu.livelabs.integration.promotion.PromotionDialogFragment;
import sg.edu.smu.livelabs.integration.promotion.PromotionItemAdapter;


/**
 *  This activity handles the list of promotions. The calling of the promotions are via LiveLabAPI.
 *  Created by Le Gia Hai on 18/5/2015.
 *  Edited by John on 1 July 2015
 *
 *
 */
public class PromotionActivity extends FragmentActivity implements PromotionDialogFragment.PromotionDialogListener{
    public static final String TAG = "LIVELABS";
    private ListView listView;
    private LinearLayout  noPromotionLL;
    private PromotionItemAdapter promotionItemAdapter;
    private boolean haveNetworkFault;
    private int campaingId;
    private ProgressBar progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        this.requestWindowFeature(Window.FEATURE_NO_TITLE);

        //this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        this.getWindow().setFeatureInt(Window.FEATURE_NO_TITLE, 0);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_int);


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

        /*
        if(haveNetworkFault){
            new AlertDialog.Builder(PromotionActivity.this)
                    .setTitle("Wifi")
                    .setMessage("Cannot connect to network. Please check your WIFI connection.")
                    .setPositiveButton("OK",  new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            finish();
                            //finishActivity(100);
                        }
                    })
                    .show();
            return;
        }*/

        LiveLabsApi.getInstance().onPromotionActivityCreated(this, savedInstanceState);

        Intent intent = getIntent();
        boolean isNotification = intent.getBooleanExtra("Notification", false);

        if(isNotification){
            String id = intent.getStringExtra("id");
            LiveLabsApi.getInstance().notificationTracking(id);
        }

        Typeface tfSemiBold = Typeface.createFromAsset(this.getAssets(), "font/MyriadPro-Semibold.otf");
        TextView titleView = (TextView) this.findViewById(R.id.title);
        titleView.setTypeface(tfSemiBold);

        /*
        ImageView backButton = (ImageView) this.findViewById(R.id.back_button);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                //finishActivity(100);
            }
        });*/

        noPromotionLL = (LinearLayout)findViewById(R.id.no_promotion);

        //This stuff is to test the promotion feature
        listView = (ListView) findViewById(R.id.list_view);
        promotionItemAdapter = new PromotionItemAdapter(this);
        listView.setAdapter(promotionItemAdapter);
        refreshPromotions();


        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Promotion p = promotionItemAdapter.getItem(position);
                campaingId = p.getCampaignId();

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
        System.out.println("Resume");
    }

    @Override
    protected void onPause() {
        super.onPause();
        LiveLabsApi.getInstance().onPromotionActivityPaused(this);
        System.out.println("Pasue");
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        System.out.println("onReStart");
    }

    @Override
    protected void onStart() {
        super.onStart();
        System.out.println("onStart");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LiveLabsApi.getInstance().onPromtionActivityDestroyed(this);
        System.out.println("Destory");
    }

    @Override
    public void onPromotionDialogCanceled() {
        findViewById(R.id.loadingPanel).setVisibility(View.VISIBLE);
        refreshPromotions();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action1) {
            refreshPromotions();
            return true;
        } else if (id == R.id.action2) {
            //This is to test the update user info feature
            LiveLabsApi.getInstance().userInfoUpdated("livelabs1", "male", "livelabs@smu.edu.ssg", "81180099", "10/02/2911", "312345");
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void refreshPromotions() {

        LiveLabsApi.getInstance().getPromotions(new LiveLabsApi.PromotionCallback() {
            @Override
            public void onResult(List<Promotion> promotions) {
                promotionItemAdapter.promotionsUpdated(promotions);

                if(promotionItemAdapter.getCount() > 0)
                {
                    noPromotionLL.setVisibility(View.GONE);
                    listView.setVisibility(View.VISIBLE);
                }
                else{
                    noPromotionLL.setVisibility(View.VISIBLE);
                    listView.setVisibility(View.GONE);
                }
                findViewById(R.id.loadingPanel).setVisibility(View.GONE);
            }

            @Override
            public void onError(Throwable t, String message) {
                findViewById(R.id.loadingPanel).setVisibility(View.GONE);

                if (t instanceof SSLHandshakeException) {
                    new AlertDialog.Builder(PromotionActivity.this)
                            .setTitle("Promotion")
                            .setMessage("Please connect & sign-in to the WiFi to receive the latest promotion.")
                            .setPositiveButton("OK", null)
                            .show();
                }
                noPromotionLL.setVisibility(View.VISIBLE);
                listView.setVisibility(View.GONE);
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
            }
            else if(!Integer.toString(campaingId).equals(result.getContents()) ){

                new AlertDialog.Builder(PromotionActivity.this)
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
            }
            else {

                //Log.d("MainActivity", "Scanned");
                //Toast.makeText(this, "Scanned: " + result.getContents(), Toast.LENGTH_LONG).show();

                LiveLabsApi.getInstance().redeemPromotion(result.getContents(),
                new LiveLabsApi.RedeemPromotionCallback() {

                    @Override
                    public void onResult(String status, String message) {

                        if(status.equals("success")) {
                            AlertDialog alertDialog = new AlertDialog.Builder(PromotionActivity.this)
                                    .setTitle("Redeem")
                                    .setCancelable(false)
                                    .setMessage("You have successfully redeem this promotion")
                                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {
                                            //System.out.println("refresh");
                                            finish();
                                            startActivity(getIntent());
                                        }
                                    })
                                    .show();

                            alertDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                                @Override
                                public void onCancel(DialogInterface dialog) {

                                    finish();
                                    startActivity(getIntent());
                                }
                            });
                        }
                        else{
                            new AlertDialog.Builder(PromotionActivity.this)
                                    .setTitle("Redeem")
                                    .setMessage(message)
                                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {
                                            //System.out.println("refresh");
                                            finish();
                                            startActivity(getIntent());
                                        }
                                    })
                                    .show();
                        }
                    }

                    @Override
                    public void onError(Throwable t, String message) {

                        new AlertDialog.Builder(PromotionActivity.this)
                                .setTitle("Redeem")
                                .setMessage("Invalid coupon. Please try to scan the QR again.")
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



