package com.mysentosa.android.sg;

import android.app.Application;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Typeface;
import android.util.Log;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.facebook.FacebookSdk;
import com.mysentosa.android.sg.helper.DataHelper;
import com.mysentosa.android.sg.imageloader.ImageCache;
import com.mysentosa.android.sg.imageloader.ImageCache.ImageCacheParams;
import com.mysentosa.android.sg.imageloader.ImageFetcher;
import com.mysentosa.android.sg.imageloader.ImageLoaderUtils;
import com.mysentosa.android.sg.models.IslanderUser;
import com.mysentosa.android.sg.models.Promotion;
import com.mysentosa.android.sg.receiver.IntentReceiver;
import com.urbanairship.AirshipConfigOptions;
import com.urbanairship.Logger;
import com.urbanairship.UAirship;
import com.urbanairship.push.PushManager;
import com.urbanairship.push.PushPreferences;

import java.util.ArrayList;

import sg.edu.smu.livelabs.integration.LiveLabsApi;

public class SentosaApplication extends Application {

    public static final String UNDEFINED_VERSION = "undefined version";
    public static final String IMAGE_CACHE_DIR = "sentosa_images";
    public static String application_version;
    public static RequestQueue mRequestQueue;
    public static IslanderUser mCurrentIslanderUser;
    ;
    public ImageFetcher mImageFetcher;
    public ImageCache mImageCache;
    public static SentosaApplication appInstance;
    public Typeface myridTypeFace;
    public static ArrayList<Promotion> mClaimedDeals;

    @Override
    public void onCreate() {
        super.onCreate();
        appInstance = this;
        mRequestQueue = Volley.newRequestQueue(this);

        setupLiveLabsApi();
        FacebookSdk.sdkInitialize(getApplicationContext());
        DataHelper.init(getApplicationContext());

        AirshipConfigOptions options = AirshipConfigOptions.loadDefaultOptions(this);
        options.inProduction = !BuildConfig.DEBUG;
        UAirship.takeOff(this, options);
        PushManager.enablePush();
        PushManager.shared().setIntentReceiver(IntentReceiver.class);
        PushPreferences prefs = PushManager.shared().getPreferences();
        Log.i("TAG", " testing My Application onCreate - App APID: " + prefs.getPushId());

        Logger.info("My Application onCreate - App APID: " + prefs.getPushId());
        Log.d(" testing", " testing My Application onCreate - App APID: " + prefs.getPushId());

        myridTypeFace = Typeface.createFromAsset(getAssets(), "MyriadPro-Semibold.otf");

        ImageCacheParams cacheParams = new ImageCacheParams(IMAGE_CACHE_DIR);

        // PhucVM: we need to cast this to LONG because the value can be greater than int maximum => it will be less than 0 if we use int
        long tempMemory = 1024 * 1024 * (long) (ImageLoaderUtils.getMemoryClass(this.getApplicationContext())) * 8;
        cacheParams.memCacheSize = (int) (tempMemory / 19);

        mImageCache = new ImageCache(this, cacheParams);
        mImageFetcher = new ImageFetcher(this);
        mImageFetcher.setImageCache(mImageCache);
        try {
            application_version = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
        } catch (NameNotFoundException e) {
            application_version = UNDEFINED_VERSION;
        }
    }

    private void setupLiveLabsApi() {
        if(BuildConfig.DEBUG) {
//            LiveLabsApi.getInstance().initialize(getApplicationContext(), LiveLabsApi.DEV);
            LiveLabsApi.getInstance().initialize(getApplicationContext(), LiveLabsApi.PROD);
        } else {
            LiveLabsApi.getInstance().initialize(getApplicationContext(), LiveLabsApi.PROD);
        }
    }

}
