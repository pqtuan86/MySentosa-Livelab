package sg.edu.smu.livelabs.integration;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;
import android.widget.ListView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URL;
import java.security.MessageDigest;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import sg.edu.smu.livelabs.integration.model.Promotion;

/**
 * The main class to interact with LiveLabs component. This class implements singleton pattern. Use {@link #getInstance()} to get access to other methods.
 */
public class LiveLabsApi {
    public static final String DEV = "dev";
    public static final String PROD = "prod";

    private static final String DEV_BASE_URL = "https://athena.smu.edu.sg/hestia/sentosa";
    private static final String PROD_BASE_URL = "https://tyr.livelabs.smu.edu.sg/sentosa"; //"https://tyr.livelabs.smu.edu.sg/odin/sentosa"; //(https://tyr.livelabs.smu.edu.sg/odin/
    private static final String DEV_BASE_SENTOSA_BACKEND = "https://athena.smu.edu.sg/hestia/sentosaBackend/index.php/Utilities";
    private static final String PROD_BASE_SENTOSA_BACKEND = "https://tyr.livelabs.smu.edu.sg/sentosaBackend/index.php/Utilities";
    private static final String TAG = "LIVELABS";
    private static final String VENDORID = "0565F790-965E-43C1-A192-55A79E03E64E";

    /**
     * This interface help us to pass you the promotion list after a call to {@link #getPromotions(sg.edu.smu.livelabs.integration.LiveLabsApi.PromotionCallback callback)} method.
     * We recommend you to implement
     * this interface in the Activity/Fragment that will display the promotion list. Or you can pass us an anonymous class also.
     */
    public interface PromotionCallback {
        /**
         * Recommend to process and display the list of promotions from LiveLabs in this method.
         * @param promotions The result promotions from LiveLabs, can be an empty list, but will never be null.
         */
        void onResult(List<Promotion> promotions);

        /**
         * Oops! => Simply alert users with the message.
         * @param t The exception.
         * @param message The message to display to users.
         */
        void onError(Throwable t, String message);
    }

    /**
     * This interface help us to pass you the result of the redeem after a call to {@link #redeemPromotion(String, sg.edu.smu.livelabs.integration.LiveLabsApi.RedeemPromotionCallback callback)} method.
     * This will pass you the message of the result or the error message encountered
     */
    public interface RedeemPromotionCallback{

        /**
         * @param message The message return by the server after success
         */
        void onResult(String status, String message);

        /**
         * Oops! => Simply alert users with the message.
         * @param t The exception.
         * @param message The message to display to users.
         */
        void onError(Throwable t, String message);
    }

    private static final LiveLabsApi instance = new LiveLabsApi();

    private Context c;

    private boolean initialized;
    private PromotionCallback apiCallback;
    private Activity mainActivity;
    private Activity promotionActivity;

    private String model;

    private String ip; //local ip
    private String uuid;
    private String uuidSHA1;
    public String macAddress;
    public String macAddressSHA1;
    public String sentosaIp;
    private SharedPreferences preferences;
    private String baseUrl;
    private String backendBaseUrl;
    private SimpleDateFormat promotionDf;

    private boolean mainActivityPaused;
    private boolean promotionActivityPaused;
    private boolean isMappedMacToUUID;
    private boolean initialCheckIp;
    private boolean isCheckUserNetworkCalling;

    private LiveLabsApi() {
        initialized = false;
        isMappedMacToUUID = false;
        initialCheckIp = false;
        isCheckUserNetworkCalling = false;
        macAddress = null;
        promotionDf = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.ENGLISH);
    }

    /**
     * Get LiveLabsApi instance. You need this to call other APIs.
     * @return LiveLabsApi instance
     */
    public static LiveLabsApi getInstance() {
        return instance;
    }

    /**
     * You need to call this method only once, when the app's starting. We recommend you to call this method in your splash screen.
     * @param context ApplicationContext. Inside activity, you can use getApplicationContext() method
     * @param environment For testing use {@link #DEV}, for production use {@link #PROD}
     */
    public void initialize(Context context, String environment) {
        c = context;
        if (initialized) {
            return;
        }
        preferences = context.getSharedPreferences("LiveLabs_Preferences", Context.MODE_PRIVATE);
        if (PROD.equals(environment)) {
            baseUrl = PROD_BASE_URL;
            backendBaseUrl = PROD_BASE_SENTOSA_BACKEND;
        } else {
            baseUrl = DEV_BASE_URL;
            backendBaseUrl = DEV_BASE_SENTOSA_BACKEND;
        }

        int i = android.os.Build.VERSION.SDK_INT;
        if ( android.os.Build.VERSION.SDK_INT <= Build.VERSION_CODES.LOLLIPOP_MR1){ //version below android 6
            macAddress = preferences.getString("MAC_ADDR", null);
            if (macAddress == null) {
                macAddress = Net.getMACAddress(context);
                preferences.edit().putString("MAC_ADDR", macAddress).commit();
            }

            macAddressSHA1 = normalizeMac(macAddress);
            model = Build.MODEL;
            int version = Build.VERSION.SDK_INT;

            if (macAddress == null || macAddress.isEmpty()) {
                Log.d(TAG, "Cannot get mac address of this device: " + model);
            } else {
                Map<String, String> params = new HashMap<>();
                params.put("devId", macAddressSHA1);
                params.put("phoneModel", model);
                params.put("osType", "android");
                params.put("osVersion", "" + version);
                params.put("time", "" + (System.currentTimeMillis() / 1000));
                post("/app-initialized", params, new Net.HttpCallback() {
                    @Override
                    public void onSuccess(String respone) {
                        Log.d(TAG, "Initialized:  " + respone);
                    }

                    @Override
                    public void onFailed(Throwable t) {
                        Log.e(TAG, "Initialized failed.", t);
                    }
                });

                String regId = preferences.getString("REG_ID", null);
                if (regId != null) {
                    registerNoti(regId);
                }
            }

            sendAppInstallTracking();

            updateBackendUserInfo();

            initialized = true;
        }
        else{ //for android 6 and above
            //need to call m-enquiry (passing ip address of the phone) to get the mac
            //settings.secure is use if the user doesn't have any network connection to call the m-enquiry
            //thus, setting the macAddress with a UUID first
            uuid = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID); //UUID
            macAddress = uuid;
            preferences.edit().putString("MAC_ADDR", uuid).commit();

            //intialize with UUID first
            uuidSHA1 = SHA1(uuid);
            macAddressSHA1 = uuidSHA1;
            model = Build.MODEL;
            int version = Build.VERSION.SDK_INT;

            if (uuidSHA1 == null || uuidSHA1.isEmpty()) {
                Log.d(TAG, "Cannot get mac address of this device: " + model);
            } else {
                Map<String, String> params = new HashMap<>();
                params.put("devId", uuidSHA1);
                params.put("phoneModel", model);
                params.put("osType", "android");
                params.put("osVersion", "" + version);
                params.put("time", "" + (System.currentTimeMillis() / 1000));
                post("/app-initialized", params, new Net.HttpCallback() {
                    @Override
                    public void onSuccess(String respone) {
                        Log.d(TAG, "Initialized AV6:  " + respone);
                    }

                    @Override
                    public void onFailed(Throwable t) {
                        Log.e(TAG, "Initialized failed.", t);
                    }
                });

                String regId = preferences.getString("REG_ID", null);
                if (regId != null) {
                    registerNoti(regId);
                }
            }

            sendAppInstallTracking();

            updateBackendUserInfo();

            checkUserInSentosaNetwork();

            initialized = true;
        }
    }

    private void checkUserInSentosaNetwork(){
        isCheckUserNetworkCalling = true;
        ConnectivityManager connManager = (ConnectivityManager) c.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo mWifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        WifiManager wifiMan = (WifiManager) c.getSystemService(Context.WIFI_SERVICE);
        WifiInfo wifiInf = wifiMan.getConnectionInfo();

        if (mWifi.isConnected()) { //check if wifi is connected and then check if it is in sentosa network
            int ipAddress = wifiInf.getIpAddress();
            ip = String.format("%d.%d.%d.%d", (ipAddress & 0xff), (ipAddress >> 8 & 0xff), (ipAddress >> 16 & 0xff), (ipAddress >> 24 & 0xff)); //local ip

            postURL("http://checkip.dyndns.org/", new HashMap<String, String>(), new Net.HttpCallback() { //check if current ip is within Sentosa network
                @Override
                public void onSuccess(String response) {
                    try {
                        String[] tmp = response.split(":");
                        if (tmp.length == 2) {//sample>> <html><head><title>Current IP Check</title></head><body>Current IP Address: 202.161.57.176</body></html>
                            String[] tmp2 = tmp[1].split("</body>");
                            if (tmp2.length == 2) { //sample>> 202.161.57.176</body></html>
                                final String publicIP = tmp2[0].trim();
                                postSentosaBackendUrl("/getIPList", new HashMap<String, String>(), false, "", new Net.HttpCallback() { //check if current ip is within Sentosa network
                                    @Override
                                    public void onSuccess(String respone) {
                                        isCheckUserNetworkCalling = false;
                                        try {
                                            JSONArray jArray = new JSONArray(respone);
                                            for(int i=0; i<jArray.length(); i++){
                                                if(jArray.get(i).equals(publicIP)){
                                                    initialCheckIp = true;
                                                    Log.e(TAG,"PublicIP:" + publicIP);
                                                    mapUUIDAndMac();
                                                    break;
                                                }
                                            }
                                        } catch (Exception e) {
                                        }
                                    }

                                    @Override
                                    public void onFailed(Throwable t) {
                                        isCheckUserNetworkCalling = false;
                                        initialCheckIp = true;
                                        Log.e(TAG, "Cannot get Sentosa IP list. Will map uuid to mac later", t);
                                    }
                                });
                            }
                        }
                    } catch (Exception e) {
                    }
                }

                @Override
                public void onFailed(Throwable t) {
                    isCheckUserNetworkCalling = false;
                    initialCheckIp = true;
                    Log.e(TAG, "Cannot check public ip", t);
                }
            });
        }
    }

    private void mapUUIDAndMac(){
        Map<String, String> params = new HashMap<>();
        params.put("ip", ip);
        params.put("vendorId", VENDORID);

        post("/m-enquiry", params, new Net.HttpCallback() {
            @Override
            public void onSuccess(String response) {
                try {
                    JSONObject promotionJson = new JSONObject(response);
                    String macAddressSHA1ByIp = promotionJson.getString("mac"); //mac address return is in SHA1
                    Log.d(TAG, "MAC Address AV6:  " + macAddressSHA1ByIp);

                    Map<String, String> params2 = new HashMap<>();
                    String paramsStr = "{\"mac\":\"" + macAddressSHA1ByIp + "\", \"uuid\":\"" + uuidSHA1 + "\"}";
                    params2.put("mac", macAddressSHA1ByIp);
                    params2.put("uuid", uuidSHA1);

                    postSentosaBackendUrl("/reportUUIDMACMapping", params2, true, paramsStr, new Net.HttpCallback() {
                        @Override
                        public void onSuccess(String response) {
                            isMappedMacToUUID = true;
                            Log.d(TAG, "UUID map to Mac");
                        }

                        @Override
                        public void onFailed(Throwable t) {
                            Log.e(TAG, "Cannot map uuid to mac.", t);
                        }
                    });
                } catch (Exception e) {

                }
            }

            @Override
            public void onFailed(Throwable t) {
                Log.e(TAG, "Convert IP to Mac failed.", t);
            }
        });
    }


    /**
     * Once you have the GCM registration id, call this to send us the information.
     * @param regId Google Cloud Messaging registration id. Cannot be null or empty
     */
    public void gcmRegistered(String regId) {
        if (!initialized) {
            throw new RuntimeException("initialize must be invoked first.");
        }
        if (regId == null || regId.isEmpty()) {
            throw new IllegalArgumentException("regId cannot be null or empty");
        }

        preferences.edit().putString("REG_ID", regId).commit();
        registerNoti(regId);
    }

    /**
     * This allow user to disable notification from LiveLabs.
     */
    public void disableNotification() {
        Map<String, String> params = new HashMap<>();
        params.put("devId", macAddressSHA1);
        post("/unregister-push", params, new Net.HttpCallback() {
            @Override
            public void onSuccess(String respone) {
                Log.d(TAG, "Unregister push done.");
            }

            @Override
            public void onFailed(Throwable t) {
                Log.e("TAG", "Unregister push failed. Retry after 5s", t);
            }
        });
    }

    /**
     * Call this method from your GCMIntentService.onHandleIntent(Intent intent). Once  you know that this is NOT your notification. We will process it (showing alert or notification).
     * @param extras Bundle from the Intent, simply use intent.getExtras()
     * @return A String to be display using NotificationManager. If null, you don't need to do anything.
     */
    public Map<String, String> processNotification(Bundle extras) {
        if (!initialized) {
            throw new RuntimeException("initialize must be invoked first.");
        }

        Map<String, String> params = new HashMap<>();


        String type = extras.getString("type");
        if ("LiveLabs".equals(type)) {
            final String title = extras.getString("title");
            final String message = extras.getString("message");
            final String notificationId = extras.getString("id");
            final String promotionId = extras.getString("promotion_id");

            params.put("message", message);
            params.put("id", notificationId);
            params.put("promotion_id", promotionId);

            if (mainActivity != null && !mainActivityPaused) {
                Handler h = new Handler(Looper.getMainLooper());
                h.post(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            new AlertDialog.Builder(mainActivity)
                                    .setTitle(title)
                                    .setMessage(message)
                                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {

                                            notificationTracking(notificationId);

                                            Intent intent = new Intent(mainActivity, PromotionActivity.class);
                                            mainActivity.startActivity(intent);
                                        }
                                    })
                                    .show();
                        } catch (Exception e) {

                        }
                    }
                });
                return null;
            }
            else if (promotionActivity != null && !promotionActivityPaused) {
                Handler h = new Handler(Looper.getMainLooper());
                h.post(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            new AlertDialog.Builder(promotionActivity)
                                    .setTitle(title)
                                    .setMessage(message)
                                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {

                                            notificationTracking(notificationId);

                                            Intent intent = new Intent(promotionActivity, PromotionActivity.class);
                                            promotionActivity.startActivity(intent);
                                        }
                                    })
                                    .show();
                        } catch (Exception e) {

                        }
                    }
                });

                return params;
            }
            return params;
        }
        return null;
    }

    /**
     * In order to keep track of the installation of the app. Every time users install or upgrade the app from Play store, you need to call this method.
     * @param oldVersion The version before upgrading, null if this is the new installation.
     * @param currentVersion The current version of the app. Cannot be null or empty.
     */
    public void appInstalled(String oldVersion, String currentVersion) {
        if (!initialized) {
            throw new RuntimeException("initialize must be invoked first.");
        }
        if (currentVersion == null || currentVersion.isEmpty()) {
            throw new IllegalArgumentException("currentVersion cannot be null or empty.");
        }
        if (oldVersion == null) {
            oldVersion = "";
        }
        preferences
                .edit()
                .putString("OLD_VERSION", oldVersion)
                .putString("CUR_VERSION", currentVersion)
                .putLong("INSTALLED_TIME", System.currentTimeMillis())
                .putBoolean("NOTIFY_VERSION_CHANGED", false)
                .commit();
        sendAppInstallTracking();
    }

    /**
     * In order to keep track of app usages, please call this method when users create or update his/her profile.
     * @param name User's name.
     * @param gender User's gender.
     * @param email User's email.
     * @param phone User's phone.
     * @param birthday User's birthday.
     * @param postalCode User's postal code.
     */
    public void userInfoUpdated(String name, String gender, String email, String phone, String birthday, String postalCode) {
        if (!initialized) {
            throw new RuntimeException("initialize must be invoked first.");
        }
        if (name == null) {
            name = "";
        }
        if (gender == null) {
            gender = "";
        }
        if (email == null) {
            email = "";
        }
        if (phone == null) {
            phone = "";
        }
        if (birthday == null) {
            birthday = "";
        }
        if (postalCode == null) {
            postalCode = "";
        }
        preferences.edit()
                .putString("NAME", name)
                .putString("GENDER", gender)
                .putString("EMAIL", email)
                .putString("PHONE", phone)
                .putString("BIRTHDAY", birthday)
                .putString("POSTAL_CODE", postalCode)
                .putLong("USER_INFO_CHANGED_TIME", System.currentTimeMillis())
                .putBoolean("NOTIFY_USER_INFO_CHANGED", false)
                .commit();

        updateBackendUserInfo();
    }

    /**
     * Help us to coordinate with your main activity life cycle. Call this when your main activity is created.
     * @param activity Your main activity. Please don't call this on other activities.
     *  @param savedInstanceState
     */
    public ListView onMainActivityCreated(Activity activity, Bundle savedInstanceState) {
        if (!initialized) {
            throw new RuntimeException("initialize must be invoked first.");
        }

        //for android 6 and above
        if(initialCheckIp && !isMappedMacToUUID && android.os.Build.VERSION.SDK_INT > android.os.Build.VERSION_CODES.LOLLIPOP_MR1){
            mapUUIDAndMac();
        }
        else if(!isCheckUserNetworkCalling && !isMappedMacToUUID && android.os.Build.VERSION.SDK_INT > android.os.Build.VERSION_CODES.LOLLIPOP_MR1){
            checkUserInSentosaNetwork();
        }

        mainActivity = activity;
        sendAppStatusTracking("start");
        mainActivityPaused = false;
        return null;
    }

    /**
     * Help us to coordinate with your main activity life cycle. Call this when your main activity is resumed.
     * @param activity Your main activity. Please don't call this on other activities.
     */
    public void onMainActivityResumed(Activity activity) {
        if (!initialized) {
            throw new RuntimeException("initialize must be invoked first.");
        }

        //for android 6 and above
        if(initialCheckIp && !isMappedMacToUUID && android.os.Build.VERSION.SDK_INT > android.os.Build.VERSION_CODES.LOLLIPOP_MR1){
            mapUUIDAndMac();
        }
        else if(!isCheckUserNetworkCalling && !isMappedMacToUUID && android.os.Build.VERSION.SDK_INT > android.os.Build.VERSION_CODES.LOLLIPOP_MR1){
            checkUserInSentosaNetwork();
        }

        mainActivityPaused = false;
        sendAppStatusTracking("foreground");
    }

    /**
     * Help us to coordinate with your main activity life cycle. Call this when your main activity is paused.
     * @param activity Your main activity. Please don't call this on other activities.
     */
    public void onMainActivityPaused(Activity activity) {
        if (!initialized) {
            throw new RuntimeException("initialize must be invoked first.");
        }
        mainActivityPaused =true;
        sendAppStatusTracking("pause");
    }

    /**
     * Help us to coordinate with your main activity life cycle. Call this when your main activity is destroyed.
     * @param activity Your main activity. Please don't call this on other activities.
     */
    public void onMainActivityDestroyed(Activity activity) {
        mainActivity = null;
        mainActivityPaused = true;
        sendAppStatusTracking("stop");
    }

    /**
     * Call this method to get the list of all promotions for the current user. This method is asynchronous so you need to pass it a callback to process the result.
     * @param callback Your callback to process the result, See more {@link sg.edu.smu.livelabs.integration.LiveLabsApi.PromotionCallback}
     */
    public void getPromotions(final PromotionCallback callback) {
        if (!initialized) {
            throw new RuntimeException("initialize must be invoked first.");
        }
        if (callback == null) {
            throw new IllegalArgumentException("callback cannot be null");
        }
        Map<String, String> params = new HashMap<>();
        params.put("devId", macAddressSHA1);

//        System.out.println("MAC SHA1: " + macAddressSHA1);
        post("/get-promotions", params, new Net.HttpCallback() {
            @Override
            public void onSuccess(String respone) {

                List<Promotion> promotions = new ArrayList<Promotion>();
                try {
                    JSONArray promotionsJson = new JSONArray(respone);

                    for (int i = 0, count = promotionsJson.length(); i < count; i++) {
                        JSONObject promotionJson = promotionsJson.getJSONObject(i);
                        Date startTime = promotionDf.parse(promotionJson.getString("startTime"));
                        Date endTime = promotionDf.parse(promotionJson.getString("endTime"));

                        Promotion promotion = new Promotion(promotionJson.getInt("id"), promotionJson.getString("title"),
                                promotionJson.getString("details"), promotionJson.getString("description"),
                                promotionJson.getString("campaignName"), startTime,
                                endTime, new URL(promotionJson.getString("image")),
                                promotionJson.getString("workingHour"), promotionJson.getString("status"),
                                promotionJson.getString("merchantName"), promotionJson.getString("merchantLocation"),
                                promotionJson.getString("merchantPhone"), promotionJson.getString("merchantEmail"),
                                promotionJson.getString("merchantWeb"), promotionJson.getInt("campaignId"),
                                promotionJson.getInt("serial"));
                        try {
                            String str = promotionJson.getString("regRequired");
                            if ("f".equals(str)) {
                                promotion.setRegRequired(false);
                            } else {
                                promotion.setRegRequired(true);
                            }
                            promotion.setRegUrl(promotionJson.getString("regURL"));
                            promotion.setRegDiscountCode(promotionJson.getString("regDiscountCode"));
                        } catch (Exception e) {
                            promotion.setRegRequired(false);
                        }
                        promotions.add(promotion);
                    }
                } catch (Throwable t) {
                    callback.onError(t, "Cannot parse promotions JSON.");
                    return;
                }
                callback.onResult(promotions);
            }

            @Override
            public void onFailed(Throwable t) {
                callback.onError(t, "Cannot parse redeem of promotion JSON.");
            }
        });
    }

    /**
     * Call this method to redeem the promotion.
     * @param  campaginId This is the campaign ID that the QR had scanned.
     * @param callback Your callback to process the result, See more {@link sg.edu.smu.livelabs.integration.LiveLabsApi.RedeemPromotionCallback}
     */
    public void redeemPromotion(String campaginId, final RedeemPromotionCallback callback) {
        if (!initialized) {
            throw new RuntimeException("initialize must be invoked first.");
        }
        if(campaginId == null || campaginId.isEmpty()){
            throw new RuntimeException("campagin Id cannot be empty or null");
        }
        if (callback == null) {
            throw  new IllegalArgumentException("callback cannot be null");
        }


        Map<String, String> params = new HashMap<>();
        params.put("devId", macAddressSHA1);
        params.put("campaignId", campaginId);
        post("/redeem-promotion", params, new Net.HttpCallback() {
            @Override
            public void onSuccess(String response) {

                try {
                    JSONObject redeemPromotionJson = new JSONObject(response);
                    if (redeemPromotionJson.getString("status").toLowerCase().equals("failed")) {
                        callback.onResult("failed", redeemPromotionJson.getString("message"));
                    } else {
                        callback.onResult("success", redeemPromotionJson.getString("serial"));
                    }

                } catch (Throwable t) {
                    callback.onError(t, "Cannot parse redeem of promotion JSON.");
                    return;
                }
            }

            @Override
            public void onFailed(Throwable t) {
                callback.onError(t, "Cannot parse redeem of promotion JSONs.");
            }
        });

    }

    /**
     * This function allow us to track the promotion notification prompt to the user upon clicking it.
     * @param notificationId is the notification id send together in the notification
     */

    public void notificationTracking(String notificationId ){
        Map<String, String> params = new HashMap<>();
        params.put("devId", macAddressSHA1);
        params.put("notificationId", notificationId );
        post("/notification-tracking", params, new Net.HttpCallback() {
            @Override
            public void onSuccess(String respone) {
                Log.d(TAG, "Notification tracking done. " + respone);
            }

            @Override
            public void onFailed(Throwable t) {
                Log.e("TAG", "Notification tracking failed.", t);
            }
        });
    }

    /**
     * This function allow us to track the promotion that the user have clicked from the listview
     * @param promotionId is the promotion id sent together in the JSON from #getPromotions
     * @param campaignId is the campaign id sent together in the JSON from #getPromotions
     */

    public void promotionTracking(String promotionId, String campaignId ){
        Map<String, String> params = new HashMap<>();
        params.put("devId", macAddressSHA1);

        params.put("promotionId", promotionId);
        params.put("campaignId", (campaignId));


                post("/promotion-tracking", params, new Net.HttpCallback() {
                    @Override
                    public void onSuccess(String respone) {
                        // if (respone.toLowerCase().equals("success")) {
                        Log.d(TAG, "Promotion tracking done.");
                        // } else {
                        //     Log.d(TAG, "Promotion tracking failed..");
                        //}
                    }

                    @Override
                    public void onFailed(Throwable t) {
                        Log.e("TAG", "Promotion tracking failed.", t);
                    }
                });
    }


    private void post(String path, Map<String, String> params, Net.HttpCallback callback) {
        Net.post(buildUrl(path), params, callback);
    }

    private void postURL(String path, Map<String, String> params, Net.HttpCallback callback) {
        Net.post(path, params, callback);
    }

    private void postSentosaBackendUrl(String path, Map<String, String> params, boolean isRaw, String data, Net.HttpCallback callback) {
        if(isRaw){
            Net.postRaw(buildUrlBackend(path), data, callback);
        }
        else{
            Net.post(buildUrlBackend(path), params, callback);
        }

    }


    private String buildUrl(String path) {
        if (path.startsWith("/")) {
            return baseUrl + path;
        } else {
            return baseUrl + "/" + path;
        }
    }

    private String buildUrlBackend(String path) {
        if (path.startsWith("/")) {
            return backendBaseUrl + path;
        } else {
            return backendBaseUrl + "/" + path;
        }
    }

    private void sendAppInstallTracking() {
        boolean sent = preferences.getBoolean("NOTIFY_VERSION_CHANGED", false);
        if (!sent) {
            String oldVersion = preferences.getString("OLD_VERSION", "");
            String currentVersion = preferences.getString("CUR_VERSION", "");
            long time = preferences.getLong("INSTALLED_TIME", 0) / 1000;

            Map<String, String> params = new HashMap<>();
            params.put("devId", macAddressSHA1);
            params.put("oldVersion", oldVersion);
            params.put("currentVersion", currentVersion);
            params.put("time", "" + time);
            post("/app-installed", params, new Net.HttpCallback() {
                @Override
                public void onSuccess(String respone) {
                    Log.d(TAG, "Notification registration done.");
                    preferences.edit().putBoolean("NOTIFY_VERSION_CHANGED", true).commit();
                }

                @Override
                public void onFailed(Throwable t) {
                    Log.e("TAG", "Register failed. Retry after 5s", t);
                    Handler h = new Handler(Looper.getMainLooper());
                    h.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            sendAppInstallTracking();
                        }
                    }, 5000);
                }
            });
        }
    }

    private void registerNoti(String regId) {
        Map<String, String> params = new HashMap<>();
        params.put("devId", macAddressSHA1);
        params.put("osType", "android");
        params.put("pushId", regId);
        post("/register-push", params, new Net.HttpCallback() {
            @Override
            public void onSuccess(String respone) {
                Log.d(TAG, "Notification registration done.");
            }

            @Override
            public void onFailed(Throwable t) {
                Log.e("TAG", "Register failed. Retry after 5s", t);
                Handler h = new Handler(Looper.getMainLooper());
                h.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        String regId = preferences.getString("REG_ID", null);
                        if (regId != null) {
                            registerNoti(regId);
                        }
                    }
                }, 5000);
            }
        });
    }

    private void updateBackendUserInfo() {
        boolean sent = preferences.getBoolean("NOTIFY_USER_INFO_CHANGED", false);
        if (!sent) {
            String name = preferences.getString("NAME", "");
            String gender = preferences.getString("GENDER", "");
            String email = preferences.getString("EMAIL", "");
            String phone = preferences.getString("PHONE", "");
            String birthday = preferences.getString("BIRTHDAY",  "");
            String postalCode = preferences.getString("POSTAL_CODE", "");
            long time = preferences.getLong("USER_INFO_CHANGED_TIME", 0) / 1000;

            Map<String, String> params = new HashMap<>();
            params.put("devId", macAddressSHA1);
            params.put("name", name);
            params.put("gender", gender);
            params.put("email", email);
            params.put("phone", phone);
            params.put("birthday", birthday);
            params.put("postalCode", postalCode);
            params.put("time", "" + time);

            post("/update-user", params, new Net.HttpCallback() {
                @Override
                public void onSuccess(String respone) {
                    Log.d(TAG, "User updated.");
                    preferences.edit().putBoolean("NOTIFY_USER_INFO_CHANGED", true).commit();
                }

                @Override
                public void onFailed(Throwable t) {
                    Log.e("TAG", "User not updated. Retry after 5s", t);
                    Handler h = new Handler(Looper.getMainLooper());
                    h.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            updateBackendUserInfo();
                        }
                    }, 5000);
                }
            });
        }
    }

    private void sendAppStatusTracking(String status) {
        Map<String, String> params = new HashMap<>();
        params.put("devId", macAddressSHA1);
        params.put("status", status);
        params.put("time", "" + (System.currentTimeMillis() / 1000));
        post("/app-tracking", params, new Net.HttpCallback() {
            @Override
            public void onSuccess(String respone) {
                Log.d(TAG, "Tracking start: " + respone);
            }

            @Override
            public void onFailed(Throwable t) {
                Log.e("TAG", "Tracking start failed", t);
            }
        });
    }

    private String normalizeMac(String macAddress) {
        if (macAddress == null) {
            return "";
        }
        String[] macComponents = macAddress.split(":");
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < macComponents.length; i++) {
            int decComp = Integer.parseInt(macComponents[i], 16);
            sb.append(decComp).append(".");
        }
        macAddress = sb.substring(0, sb.length() - 1);
        return SHA1(macAddress);
    }

    private String convertToHex(byte[] data) {
        StringBuilder buf = new StringBuilder();
        for (byte b : data) {
            int halfbyte = (b >>> 4) & 0x0F;
            int two_halfs = 0;
            do {
                buf.append((0 <= halfbyte) && (halfbyte <= 9) ? (char) ('0' + halfbyte)
                        : (char) ('a' + (halfbyte - 10)));
                halfbyte = b & 0x0F;
            } while (two_halfs++ < 1);
        }
        return buf.toString().toLowerCase(Locale.ENGLISH);
    }

    private String SHA1(String text) {

        try {
            MessageDigest md = MessageDigest.getInstance("SHA-1");
            md.update(text.getBytes("iso-8859-1"), 0, text.length());
            byte[] sha1hash = md.digest();
            return convertToHex(sha1hash);
        } catch (Exception e) {

        }
        return "";
    }



    /**
     * Help us to coordinate with your promotion activity life cycle. Call this when your promotion activity is created.
     * @param activity Your promotion activity. Please don't call this on other activities.
     *  @param savedInstanceState
     */
    public ListView onPromotionActivityCreated(Activity activity, Bundle savedInstanceState) {
        if (!initialized) {
            throw new RuntimeException("initialize must be invoked first.");
        }
        promotionActivity = activity;
        //sendAppStatusTracking("start");
        promotionActivityPaused = false;
        return null;
    }

    /**
     * Help us to coordinate with your promotion activity life cycle. Call this when your promotion activity is resumed.
     * @param activity Your promotion activity. Please don't call this on other activities.
     */
    public void onPromotionActivityResumed(Activity activity) {
        if (!initialized) {
            throw new RuntimeException("initialize must be invoked first.");
        }
        promotionActivityPaused = false;
        //sendAppStatusTracking("foreground");
    }

    /**
     * Help us to coordinate with your promotion activity life cycle. Call this when your promotion activity is paused.
     * @param activity Your promotion activity. Please don't call this on other activities.
     */
    public void onPromotionActivityPaused(Activity activity) {
        if (!initialized) {
            throw new RuntimeException("initialize must be invoked first.");
        }
        promotionActivityPaused =true;
        //sendAppStatusTracking("pause");
    }

    /**
     * Help us to coordinate with your promotion activity life cycle. Call this when your promotion activity is destroyed.
     * @param activity Your promotion activity. Please don't call this on other activities.
     */
    public void onPromtionActivityDestroyed(Activity activity) {
        promotionActivity= null;
        promotionActivityPaused = true;
        //sendAppStatusTracking("stop");
    }

    public boolean isInitialized() {
        return initialized;
    }

    public Activity getMainActivity() {
        return mainActivity;
    }

    public Activity getPromotionActivity() {
        return promotionActivity;
    }

    public boolean isMainActivityPaused() {
        return mainActivityPaused;
    }

    public boolean isPromotionActivityPaused() {
        return promotionActivityPaused;
    }

}
