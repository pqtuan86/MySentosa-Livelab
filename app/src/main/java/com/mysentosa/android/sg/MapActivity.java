package com.mysentosa.android.sg;

import android.annotation.TargetApi;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.flurry.android.FlurryAgent;
import com.google.analytics.tracking.android.EasyTracker;
import com.google.analytics.tracking.android.Fields;
import com.google.analytics.tracking.android.MapBuilder;
import com.mysentosa.android.sg.constants.MapConstants;
import com.mysentosa.android.sg.custom_views.BannerView;
import com.mysentosa.android.sg.fragments.SearchFragment;
import com.mysentosa.android.sg.map.MapDataManager;
import com.mysentosa.android.sg.tilesourcebase.CustomBitmapTileSourceBase;
import com.mysentosa.android.sg.utils.Const;
import com.mysentosa.android.sg.utils.Const.FlurryStrings;
import com.mysentosa.android.sg.utils.ResourceProxyImpl;

import org.osmdroid.ResourceProxy;
import org.osmdroid.tileprovider.tilesource.ITileSource;
import org.osmdroid.util.BoundingBoxE6;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;

import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;


public class MapActivity extends BaseActivity implements MapConstants {

    public static final String TAG = MapActivity.class.getSimpleName();

    public static String ROUTE_TO_NODE = "ROUTE_TO_NODE";
    public static String ROUTE_TO_NODE_TEXT = "ROUTE_TO_NODE_TEXT";
    public static String ROUTE_FROM_NODE = "ROUTE_FROM_NODE";
    public static String ROUTE_FROM_NODE_TEXT = "ROUTE_FROM_NODE_TEXT";
    public static String ROUTE_CENTER_FOCUS = "ROUTE_CENTER_FOCUS";
    public static String IS_WALK_ONLY = "IS_WALK_ONLY";
    public static String DIRECTIONS_POSITION = "DIRECTIONS_POSITION";
    public static String DISPLAY_DIRECTIONS = "DISPLAY_DIRECTIONS";
    public static String START_FROM_ANOTHER_ACTIVITY = "FROM_ANOTHER_ACTIVITY";

    public static final BoundingBoxE6 BOUNDING_BOX;

    static {
        if (BuildConfig.DEBUG)
            BOUNDING_BOX = new BoundingBoxE6(1331286, 103849897, 1236467, 103732567);
        else
            BOUNDING_BOX = new BoundingBoxE6(1268131, 103849897, 1236467, 103804665);
    }

    public static int ROUTE_DESTINATION_REQUEST_CODE = 111;
    public static int ROUTE_FROM_DIRECTIONS_REQUEST_CODE = 112;

    private final int MIN_ZOOM = 16;
    private final int MAX_ZOOM = 18;
    private final int MAP_PIXEL_SIZE = 256;

    @InjectView(R.id.header_title) TextView headerTitle;
    @InjectView(R.id.header_search) ImageView headerSearch;
    @InjectView(R.id.search_frame) FrameLayout searchFrame;
    @InjectView(R.id.map_holder) FrameLayout mapHolder;
    @InjectView(R.id.map_user_location) ImageView mapUserLocation;
    @InjectView(R.id.map_menu) ImageView mapMenu;
    @InjectView(R.id.map_listing) ImageView mapShowCategory;
    @InjectView(R.id.map_clear_route) ImageView mapClearRoute;
    @InjectView(R.id.map_show_bookmarks) ImageView mapShowbookmarks;
    @InjectView(R.id.map_menu_container) LinearLayout mapMenuContainer;
    @InjectView(R.id.map_directions) public ImageView mapDirections;

    private MapView mapView;
    private BannerView bannerView;
    private SearchFragment searchFragment;

    private ITileSource tileSource;
    private MapDataManager mapDataManager;
    private ResourceProxy resourceProxy;

    private String nodeFromText;
    private String nodeToText;

    public int directionsPosition = 0;
    private int nodeFrom;
    private int nodeTo;

    private boolean isWalkOnly;
    private boolean startedFromAnotherActivity = false;

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.map_screen);
        ButterKnife.inject(this);

        initializeViews();

        EasyTracker easyTracker = EasyTracker.getInstance(this);
        easyTracker.set(Fields.SCREEN_NAME, Const.GAStrings.MAP);
        easyTracker.send(MapBuilder
                        .createAppView()
                        .build()
        );
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private void initializeViews() {
        headerTitle.setText(R.string.map);
        headerSearch.setVisibility(View.VISIBLE);
        searchFragment = (SearchFragment) getSupportFragmentManager().findFragmentById(R.id.search_fragment);

        resourceProxy = new ResourceProxyImpl(getApplicationContext());
        mapView = new MapView(this, MAP_PIXEL_SIZE, resourceProxy);
        mapView.setMultiTouchControls(true);

        try {
            tileSource = new CustomBitmapTileSourceBase(getApplicationContext().getAssets(), MAX_ZOOM, MIN_ZOOM, MAP_PIXEL_SIZE, ".jpg", "tiles");
            mapView.setTileSource(tileSource);
        } catch (Exception ignore) {
            ignore.printStackTrace();
        }

        try {
            mapView.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        } catch (NoSuchMethodError ignore) {
            ignore.printStackTrace();
        }

        mapHolder.addView(this.mapView, new FrameLayout.LayoutParams(android.view.ViewGroup.LayoutParams.FILL_PARENT, android.view.ViewGroup.LayoutParams.FILL_PARENT));

        mapDataManager = new MapDataManager(mapView, resourceProxy);

        //plotting route if requested
        startedFromAnotherActivity = getIntent().getBooleanExtra(START_FROM_ANOTHER_ACTIVITY, false);
        int nodeId = getIntent().getIntExtra(ROUTE_TO_NODE, -1);
        String nodeTitle = getIntent().getStringExtra(ROUTE_TO_NODE_TEXT);
        boolean walkOnly = getIntent().getBooleanExtra(IS_WALK_ONLY, true);

        if (nodeId != -1) {
            openDirections(null, 0, nodeTitle, nodeId, walkOnly, false);
        }

        //setting the bounding box for map operation
        mapView.setScrollableAreaLimit(BOUNDING_BOX);
        resetDirectionsState();

        bannerView = (BannerView) findViewById(R.id.banner);
        if (!bannerView.isShowing())
            bannerView.updateBanner();
    }

    @OnClick(R.id.map_user_location)
    public void goToMapUserLocation() {
        FlurryAgent.logEvent(FlurryStrings.MapPageCurrentLocation);
        GeoPoint point = mapDataManager.getCurrentUserLocation();
        if (point == null) {
            Toast.makeText(MapActivity.this, "User location has not been determined!", Toast.LENGTH_LONG).show();
            return;
        }

        if (BOUNDING_BOX.contains(point)) {
            mapDataManager.centerOnUserLocation();
            mapDataManager.hideBalloonOverlay();
        } else {
            Toast.makeText(MapActivity.this, "Feature available within Sentosa", Toast.LENGTH_LONG).show();
        }
    }

    @OnClick(R.id.map_listing)
    public void showCategories() {
        FlurryAgent.logEvent(FlurryStrings.MapPageFilter);
        mapMenu.performClick();

        Intent intent = new Intent(MapActivity.this, MapFilterListActivity.class);
        intent.putExtra(MapFilterListActivity.CURRENT_CATEGORIES_LIST, mapDataManager.getCategoriesDisplayed());
        startActivityForResult(intent, MapFilterListActivity.FILTER_ACTIVITY_REQUEST_CODE);

        mapDataManager.hideBalloonOverlay();
    }

    @OnClick(R.id.map_clear_route)
    public void clearRoute() {
        FlurryAgent.logEvent(FlurryStrings.MapPageEraseRoute);
        mapDataManager.clearExistingPath();
        resetDirectionsState();
    }

    @OnClick(R.id.map_show_bookmarks)
    public void showBookmarks() {
        FlurryAgent.logEvent(FlurryStrings.ShowBookmarksInMap);
        startActivity(new Intent(MapActivity.this, MyBookmarksActivity.class));
    }

    @OnClick(R.id.map_menu)
    public void toggleMapMenu() {
        if (mapMenuContainer.getVisibility() == View.GONE) {
            mapMenuContainer.setVisibility(View.VISIBLE);
            mapMenu.setImageResource(R.drawable.bt_showmenu_state2);
        } else {
            mapMenuContainer.setVisibility(View.GONE);
            mapMenu.setImageResource(R.drawable.bt_showmenu);
        }
    }

    @OnClick(R.id.map_directions)
    public void getDirections() {
        FlurryAgent.logEvent(FlurryStrings.MapPageDirections);
        mapMenu.performClick();

        openDirections(nodeFromText, nodeFrom, nodeToText, nodeTo, isWalkOnly, false);
    }

    @OnClick(R.id.header_search)
    public void openSearch() {
        if (searchFrame.getVisibility() == View.GONE) {
            searchFrame.setVisibility(View.VISIBLE);
        } else {
            closeSearch();
        }
    }

    @Override
    public void onBackPressed() {
        if (searchFrame.getVisibility() == View.VISIBLE) {
            closeSearch();
        } else {
            super.onBackPressed();
        }
    }

    private void closeSearch() {
        searchFragment.clearSearch();
        searchFrame.setVisibility(View.GONE);
    }

    private void openDirections(String nodeFromText, int nodeFrom, String nodeToText, int nodeTo, boolean walkOnly, boolean displayDirections) {
        Intent intent = new Intent(MapActivity.this, DirectionsActivity.class);

        if (nodeFromText != null && nodeFromText.length() > 0) {
            intent.putExtra(MapActivity.ROUTE_FROM_NODE_TEXT, nodeFromText);
            intent.putExtra(MapActivity.ROUTE_FROM_NODE, nodeFrom);
            intent.putExtra(MapActivity.ROUTE_TO_NODE_TEXT, nodeToText);
            intent.putExtra(MapActivity.ROUTE_TO_NODE, nodeTo);
            intent.putExtra(MapActivity.IS_WALK_ONLY, walkOnly);
            intent.putExtra(MapActivity.DISPLAY_DIRECTIONS, displayDirections);
        }

        if (directionsPosition > 0) {
            intent.putExtra(MapActivity.DIRECTIONS_POSITION, directionsPosition);
        }
        directionsPosition = 0;

        startActivityForResult(intent, ROUTE_FROM_DIRECTIONS_REQUEST_CODE);
        mapDataManager.hideBalloonOverlay();
    }

    private void resetDirectionsState() {
        nodeFromText = null;
        nodeFrom = 0;
        nodeToText = null;
        nodeTo = 0;
        isWalkOnly = false;
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapDataManager.disableLocationOverlays();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mapView.getController().setCenter(new GeoPoint((int) (MapDataManager.MAP_CENTER_LATITUDE * 1E6), (int) (MapDataManager.MAP_CENTER_LONGITUDE * 1E6)));
        mapDataManager.enableLocationOverlay(); //show location and compass
        mapView.getController().setZoom(MIN_ZOOM);

        if (mapDataManager.getPathPointSize() == 0) {
            mapClearRoute.setVisibility(View.GONE);
        } else {
            mapClearRoute.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        bannerView.clear();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == MapFilterListActivity.FILTER_ACTIVITY_REQUEST_CODE && resultCode == RESULT_OK) {
            List<String> categories = data.getStringArrayListExtra(MapFilterListActivity.CATEGORY_ACTIVITY_RESULT);
            if (categories.size() > MapFilterListActivity.DEFAULT_ITEM) {
                Toast.makeText(MapActivity.this, getString(R.string.map_added_categories), Toast.LENGTH_SHORT).show();
            }
            mapDataManager.displayIconsOnMap(categories);

        } else if (requestCode == ROUTE_DESTINATION_REQUEST_CODE && resultCode == RESULT_OK) {
            int nodeId = data.getIntExtra(ROUTE_TO_NODE, -1);
            boolean walkOnly = data.getBooleanExtra(IS_WALK_ONLY, true);

            String nodeTitle = data.getStringExtra(ROUTE_TO_NODE_TEXT);
            if (nodeId != -1) {
                openDirections(null, 0, nodeTitle, nodeId, walkOnly, false);
            }

        } else if (requestCode == ROUTE_FROM_DIRECTIONS_REQUEST_CODE && resultCode == RESULT_OK) {
            nodeTo = data.getIntExtra(ROUTE_TO_NODE, -1);
            nodeToText = data.getStringExtra(ROUTE_FROM_NODE_TEXT);
            nodeFrom = data.getIntExtra(ROUTE_FROM_NODE, -1);
            nodeFromText = data.getStringExtra(ROUTE_TO_NODE_TEXT);
            isWalkOnly = data.getBooleanExtra(IS_WALK_ONLY, true);

            int centerOn = data.getIntExtra(ROUTE_CENTER_FOCUS, -1);
            boolean centerMap = centerOn == -1;

            if (nodeTo != -1 && nodeFrom != -1) {
                mapDataManager.routeTo(nodeTo, isWalkOnly, nodeFrom, centerMap);
            } else if (nodeTo != -1) {
                mapDataManager.routeTo(nodeTo, isWalkOnly, centerMap);
            }

            if (centerOn != -1) {
                mapDataManager.centerOn(centerOn, 600);
            }

            if (nodeTo == -1 && nodeFrom == -1) {
                resetDirectionsState();
            }

        } else if (requestCode == ROUTE_FROM_DIRECTIONS_REQUEST_CODE && resultCode != RESULT_OK && startedFromAnotherActivity) {
            finish();
        }
    }
}
