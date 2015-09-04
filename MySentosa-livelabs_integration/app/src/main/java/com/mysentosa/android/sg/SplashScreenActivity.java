package com.mysentosa.android.sg;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;

import com.crittercism.app.Crittercism;
import com.flurry.android.FlurryAgent;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.mysentosa.android.sg.asynctask.GetEventsPromotionsAsyncTask;
import com.mysentosa.android.sg.asynctask.GetLocationsAsyncTask;
import com.mysentosa.android.sg.asynctask.GetNodesEdgesAsyncTask;
import com.mysentosa.android.sg.asynctask.GetRegisterTokenAsyncTask;
import com.mysentosa.android.sg.provider.SentosaContentProvider;
import com.mysentosa.android.sg.utils.Const;
import com.mysentosa.android.sg.utils.Const.FlurryStrings;
import com.mysentosa.android.sg.utils.SentosaUtils;

import java.io.IOException;

import sg.edu.smu.livelabs.integration.LiveLabsApi;

public class SplashScreenActivity extends Activity {

    private static final String TAG = SplashScreenActivity.class.getSimpleName();

    private final int SPLASH_TIMEOUT = 1500;
    private boolean appFirstLaunch = true;
    private SharedPreferences mPrefs;

    private int eventId = -1;
    private Intent mainIntent;

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ImageView iv = new ImageView(this);
        iv.setImageResource(R.drawable.splash);
        iv.setScaleType(ScaleType.FIT_XY);
        this.setContentView(iv);
        Crittercism.initialize(getApplicationContext(), Const.CRITTERCISM_KEY);

        mainIntent = this.getIntent();
        mPrefs = getSharedPreferences(ProfileAndSettingsActivity.USER_DETAILS_PREFS, MODE_PRIVATE);
        appFirstLaunch = !mPrefs.getBoolean(ProfileAndSettingsActivity.USER_DETAILS_PREFS_ENTRY_CREATED, false);


        // This is to delete the database if the database version has been
        // increased or to install it in the beginning
        SentosaContentProvider.setupDatabase(this);

        // clear all the old images from the cache
        ((SentosaApplication) this.getApplication()).mImageCache.clearExpiredCacheImages();

        // ((SentosaApplication)this.getApplication()).mImageCache.clearCaches();
        // download the events and promotions data
        if (!SentosaContentProvider.IS_PRELOAD_DATABASE_CREATION) {
            new GetEventsPromotionsAsyncTask(this).execute();
            new GetNodesEdgesAsyncTask(this).execute();
            new GetLocationsAsyncTask(this).execute();
        }
        if (appFirstLaunch) {
            new GetRegisterTokenAsyncTask(SplashScreenActivity.this).execute();
        }

        final SharedPreferences.Editor edit = mPrefs.edit();

        String storedVersion = mPrefs.getString("CUR_VERSION", null);
        String currentVersion = getAppVersionString(getApplicationContext());
        if(!currentVersion.equals(storedVersion)) {
            LiveLabsApi.getInstance().appInstalled(storedVersion, currentVersion);
            edit.putString("CUR_VERSION", currentVersion);
            edit.commit();
        }

        Handler mSplashHandler = new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                if (checkPlayServices()) {
                    gcm = GoogleCloudMessaging.getInstance(getApplicationContext());
                    regid = getRegistrationId(getApplicationContext());
                    Log.d(TAG, "current RegId: " + regid);

                    if (regid.isEmpty()) {
                        registerInBackground();
                    }
                }

                Intent mIntent;
                Log.d(TAG, "appFirstLaunch: " + appFirstLaunch);
                if (appFirstLaunch) {
                    edit.putBoolean(ProfileAndSettingsActivity.USER_DETAILS_PREFS_ENTRY_CREATED, true);
                    edit.commit();

                    mIntent = new Intent(SplashScreenActivity.this, ProfileAndSettingsActivity.class);
                    mIntent.putExtra(ProfileAndSettingsActivity.FROM_FIRST_LAUNCH, true);

                } else {
                    boolean isRegistered = mPrefs.getBoolean(ProfileAndSettingsActivity.USER_DETAILS_PREFS_IS_REGISTERED, false);
                    if (!isRegistered) {
                        String name = mPrefs.getString(ProfileAndSettingsActivity.USER_DETAILS_PREFS_NAME, null);
                        if (SentosaUtils.isValidString(name)) {
                            String email = mPrefs.getString(ProfileAndSettingsActivity.USER_DETAILS_PREFS_EMAIL, null);
                            String mobile = mPrefs.getString(ProfileAndSettingsActivity.USER_DETAILS_PREFS_MOBILE, null);
                            String gender = mPrefs.getString(ProfileAndSettingsActivity.USER_DETAILS_PREFS_GENDER, null);
                            String birthDate = mPrefs.getString(ProfileAndSettingsActivity.USER_DETAILS_PREFS_DOB, null);
                            String postalCode = mPrefs.getString(ProfileAndSettingsActivity.USER_DETAILS_PREFS_POSTAL_CODE, null);

                            if (SentosaUtils.isValidString(birthDate)) {
                                birthDate = birthDate.replace("/", "-");
                            }
                            ProfileAndSettingsActivity.registerVisitor(SplashScreenActivity.this, name, email, mobile, gender, postalCode, birthDate);
                        }
                    }

                    mIntent = new Intent(SplashScreenActivity.this, NavigationManagerActivity.class);
                    String toClass = mainIntent.getStringExtra(NavigationManagerActivity.ACTIVITY_TO_START);
                    eventId = mainIntent.getIntExtra(NavigationManagerActivity.ACTIVITY_TO_START_ID, -1);
                    if (toClass == null || toClass.length() == 0) {
                        mIntent.putExtra(NavigationManagerActivity.ACTIVITY_TO_START, HomeActivity.class.getName());
                        FlurryAgent.logEvent(FlurryStrings.HomePageLaunch);
                    } else {
                        if (eventId != -1) {
                            mIntent.putExtra(NavigationManagerActivity.ACTIVITY_TO_START_ID, eventId);
                        }
                        if (toClass.equals(EventsAndPromotionsActivity.class.getName())) {
                            mIntent.putExtra(NavigationManagerActivity.ACTIVITY_TO_START, EventsAndPromotionsActivity.class.getName());
                            mIntent.putExtra(EventsAndPromotionsActivity.CURRENT_TYPE, mainIntent.getIntExtra(EventsAndPromotionsActivity.CURRENT_TYPE, EventsAndPromotionsActivity.TYPE_EVENT));
                        } else if (toClass.equals(ThingsToDoCategoryListActivity.class.getName())) {
                            mIntent.putExtra(NavigationManagerActivity.ACTIVITY_TO_START, ThingsToDoCategoryListActivity.class.getName());
                            mIntent.putExtra(ThingsToDoCategoryListActivity.CATEGORY_NAME_FOR_NODES, mainIntent.getStringExtra(ThingsToDoCategoryListActivity.CATEGORY_NAME_FOR_NODES));
                        } else if (toClass.equals(TicketsActivity.class.getName())) {
                            mIntent.putExtra(NavigationManagerActivity.ACTIVITY_TO_START, TicketsActivity.class.getName());
                            mIntent.putExtra(TicketsActivity.TICKET_TYPE, mainIntent.getStringExtra(TicketsActivity.TICKET_TYPE));
                        }
                    }

                }
                SplashScreenActivity.this.startActivity(mIntent);
                overridePendingTransition(R.anim.fadein, R.anim.fadeout);
                finish();
                return true;
            }
        });
        mSplashHandler.sendEmptyMessageDelayed(0, SPLASH_TIMEOUT);
    }

    private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    private static final String PROPERTY_APP_VERSION = "appVersion";
    private static final String PROPERTY_REG_ID = "registration_id";

    private GoogleCloudMessaging gcm;
    ;
    private String regid;

    private boolean checkPlayServices() {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                GooglePlayServicesUtil.getErrorDialog(resultCode, this, PLAY_SERVICES_RESOLUTION_REQUEST).show();
            } else {
                Log.i("XXX", "This device is not supported.");
                finish();
            }
            return false;
        }
        return true;
    }

    private void registerInBackground() {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                try {
                    if (gcm == null) {
                        gcm = GoogleCloudMessaging.getInstance(getApplicationContext());
                    }
                    regid = gcm.register(getString(R.string.sender_id));
                    Log.d("XXX", "GCM Registered with id:: " + regid);
                } catch (IOException ex) {
                    Log.e("XXX", "Cannot register at this moment", ex);
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                sendRegistrationIdToBackend();
            }
        }.execute(null, null, null);
    }

    private static String getAppVersionString(Context context) {
        try {
            PackageInfo packageInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            return packageInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            // should never happen
            throw new RuntimeException("Could not get package name: " + e);
        }
    }

    private static int getAppVersion(Context context) {
        try {
            PackageInfo packageInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            return packageInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            // should never happen
            throw new RuntimeException("Could not get package name: " + e);
        }
    }

    private void storeRegistrationId(Context context, String regId) {
        Log.d(TAG, "registrationId: " + regId);

        int appVersion = getAppVersion(context);

        SharedPreferences.Editor editor = mPrefs.edit();
        editor.putString(PROPERTY_REG_ID, regId);
        editor.putInt(PROPERTY_APP_VERSION, appVersion);
        editor.commit();
    }

    private void sendRegistrationIdToBackend() {
        //You may have your own logic here to send the regid back to your backend
        storeRegistrationId(this, regid);
        //Send to LiveLabs also
        LiveLabsApi.getInstance().gcmRegistered(regid);
    }

    private String getRegistrationId(Context context) {
        String registrationId = mPrefs.getString(PROPERTY_REG_ID, "");
        if (registrationId.isEmpty()) {
            Log.i(TAG, "Registration not found.");
            return "";
        }

        // Check if app was updated; if so, it must clear the registration ID
        // since the existing registration ID is not guaranteed to work with
        // the new app version.
        int registeredVersion = mPrefs.getInt(PROPERTY_APP_VERSION, Integer.MIN_VALUE);
        int currentVersion = getAppVersion(context);
        if (registeredVersion != currentVersion) {
            Log.i(TAG, "App version changed.");
            return "";
        }

        return registrationId;
    }
}