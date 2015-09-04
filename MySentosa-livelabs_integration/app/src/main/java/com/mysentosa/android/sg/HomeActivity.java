package com.mysentosa.android.sg;

import android.content.Intent;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.flurry.android.FlurryAgent;
import com.google.analytics.tracking.android.EasyTracker;
import com.google.analytics.tracking.android.Fields;
import com.google.analytics.tracking.android.MapBuilder;
import com.mysentosa.android.sg.adapter.HomePagerAdapter;
import com.mysentosa.android.sg.asynctask.GetAnnouncementsAsyncTask;
import com.mysentosa.android.sg.custom_views.CirclePageIndicator;
import com.mysentosa.android.sg.custom_views.CurrentWeatherView;
import com.mysentosa.android.sg.custom_views.CustomViewPager;
import com.mysentosa.android.sg.helper.WeatherData;
import com.mysentosa.android.sg.utils.Const;
import com.mysentosa.android.sg.utils.Const.FlurryStrings;
import com.mysentosa.android.sg.fragments.SearchFragment;

import java.util.HashMap;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import sg.edu.smu.livelabs.integration.LiveLabsApi;

public class HomeActivity extends BaseActivity {

    public static final String TAG = HomeActivity.class.getSimpleName();

    @InjectView(R.id.header_title) TextView headerTitle;
    @InjectView(R.id.header_search) ImageView headerSearch;
    @InjectView(R.id.vp_home) CustomViewPager viewPager;
    @InjectView(R.id.cpi_indicator) public CirclePageIndicator mPageIndicator;
    @InjectView(R.id.current_weather) CurrentWeatherView currentWeather;
    @InjectView(R.id.search_frame) FrameLayout searchFrame;

    private HomePagerAdapter homePagerAdapter;
    private SearchFragment searchFragment;

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LiveLabsApi.getInstance().onMainActivityCreated(this, savedInstanceState);

        setContentView(R.layout.home_screen);
        ButterKnife.inject(this);

        initializeViews();
        setupWeatherData();

        EasyTracker easyTracker = EasyTracker.getInstance(this);
        easyTracker.set(Fields.SCREEN_NAME, Const.GAStrings.LANDING);
        easyTracker.send(MapBuilder
                        .createAppView()
                        .build()
        );
    }

    @Override
    public void onResume() {
        super.onResume();
        LiveLabsApi.getInstance().onMainActivityResumed(this);

        currentWeather.updateCurrentWeatherValues(); //this checks if current weather is updated and if not downloads value and sets the view
        new GetAnnouncementsAsyncTask(this).execute();
    }

    @Override
    protected void onPause() {
        super.onPause();
        LiveLabsApi.getInstance().onMainActivityPaused(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LiveLabsApi.getInstance().onMainActivityDestroyed(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        WeatherData.save(currentWeather.getIconResID(), currentWeather.getTemperature(), currentWeather.getTimeOfReading());
    }

    /////////////////////////////////////////INITIALIZE VIEWS////////////////////////////////////////////////////
    private void initializeViews() {
        headerTitle.setText(R.string.home_title);

        headerSearch.setVisibility(View.VISIBLE);
        searchFragment = (SearchFragment) getSupportFragmentManager().findFragmentById(R.id.search_fragment);

        homePagerAdapter = new HomePagerAdapter(this);//this,mScaleHeight,mScaleWidth);
        viewPager.setAdapter(homePagerAdapter);
        viewPager.setOnFrameClickListener(onFrameClickListener);

        float ratio = 300.0f / 480.0f; //(this is based on a sample home screen image)
        DisplayMetrics d = this.getResources().getDisplayMetrics();
        int imgHeight = (int) Math.floor(d.widthPixels * ratio);
        viewPager.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, (int) Math.round(1.8 * imgHeight)));

        mPageIndicator.setViewPager(viewPager);
    }

    /////////////////////////////////////////SETUP WEATHER DATA////////////////////////////////////////////////////
    private void setupWeatherData() {
        int iconResID = WeatherData.getIconResID();
        int temperature = WeatherData.getTemperature();
        long timeOfReading = WeatherData.getTimeOfReading();
        currentWeather.setCurrentWeatherData(timeOfReading, temperature, iconResID);
    }

    /////////////////////////////////////////LISTENERS FOR THE VIEW PAGER//////////////////////////////////////////
    private final CustomViewPager.OnFrameClickListener onFrameClickListener = new CustomViewPager.OnFrameClickListener() {
        @Override
        public void onSingleClickReceived(int currentPage) {
            //flurry
            HashMap<String, String> params = new HashMap<>();
            params.put("AttractionName", homePagerAdapter.getHomeItemsList().get(currentPage).getTitle());
            FlurryAgent.logEvent(FlurryStrings.TopAttraction, params);

            //launch itinerary activity
            int nodeID = homePagerAdapter.getHomeItemsList().get(currentPage).getNodeID();

            Intent mIntent = new Intent();
            mIntent.setClass(HomeActivity.this, NodeDetailActivity.class);
            mIntent.putExtra(NodeDetailActivity.SOURCE_ACTIVITY, ACTIVITY_HOME);
            mIntent.putExtra(Const.NODE_ID, nodeID);
            startActivity(mIntent);
        }
    };

    @OnClick(R.id.header_search)
    public void openSearch() {
        if(searchFrame.getVisibility() == View.GONE) {
            searchFrame.setVisibility(View.VISIBLE);
        } else {
            closeSearch();
        }
    }

    @Override
    public void onBackPressed() {
        if(searchFrame.getVisibility() == View.VISIBLE) {
            closeSearch();
        } else {
            super.onBackPressed();
        }
    }

    private void closeSearch() {
        searchFragment.clearSearch();
        searchFrame.setVisibility(View.GONE);
    }
}