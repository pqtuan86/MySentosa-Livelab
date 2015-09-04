package com.mysentosa.android.sg;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.flurry.android.FlurryAgent;
import com.google.analytics.tracking.android.EasyTracker;
import com.google.analytics.tracking.android.Fields;
import com.google.analytics.tracking.android.MapBuilder;
import com.mysentosa.android.sg.asynctask.GetLocationIdAsyncTask;
import com.mysentosa.android.sg.custom_views.Node_Event_PromoDetailCollapsibleView;
import com.mysentosa.android.sg.fragments.SearchFragment;
import com.mysentosa.android.sg.provider.SentosaContentProvider;
import com.mysentosa.android.sg.provider.utils.SentosaDatabaseStructure.ContentURIs;
import com.mysentosa.android.sg.provider.utils.SentosaDatabaseStructure.NodeDetailsData;
import com.mysentosa.android.sg.provider.utils.SentosaDatabaseStructure.Queries;
import com.mysentosa.android.sg.utils.Const;
import com.mysentosa.android.sg.utils.Const.FlurryStrings;
import com.mysentosa.android.sg.utils.HttpHelper;
import com.mysentosa.android.sg.utils.RepoTools;
import com.mysentosa.android.sg.utils.SentosaUtils;

import java.util.HashMap;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

public class NodeDetailActivity extends BaseActivity implements LoaderCallbacks<Cursor> {

    private static final String TAG = NodeDetailActivity.class.getSimpleName();

    public static final String IMAGE_PATH_SUFFIX = "/Content/Photos/AssetsAttractionAndroid/";

    @InjectView(R.id.header_title) TextView headerTitle;
    @InjectView(R.id.header_search) ImageView headerSearch;
    @InjectView(R.id.search_frame) FrameLayout searchFrame;

    @InjectView(R.id.node_name) TextView nodeName;
    @InjectView(R.id.node_image) ImageView imgView;
    @InjectView(R.id.bookmark) ImageView bookmark;
    @InjectView(R.id.call_us) ImageView callUs;
    @InjectView(R.id.directions) ImageView directions;
    @InjectView(R.id.play_video) ImageView playVideo;
    @InjectView(R.id.container) LinearLayout collapsiblesContainer;
    @InjectView(R.id.pb_loading) ProgressBar progressBar;

    private int sourceActivity;
    private int nodeID;
    private int nodeDetailsID;
    private int isBookmarked;

    private String nodeTitle;
    private String categoryName;
    private String contactNumber;
    private String videoLink;

    private View eventsCollapsible = null;
    private View promosCollapsible = null;

    private ContentValues currentNodeDetails;
    private SearchFragment searchFragment;

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.node_detail_screen);
        ButterKnife.inject(this);

        sourceActivity = getIntent().getIntExtra(SOURCE_ACTIVITY, ACTIVITY_MAP);
        nodeID = getIntent().getIntExtra(Const.NODE_ID, -1);

        initializeViews();
        logFlurry(getIntent().getStringExtra(FlurryStrings.FlurryEventName), false);

        EasyTracker easyTracker = EasyTracker.getInstance(this);
        easyTracker.set(Fields.SCREEN_NAME, Const.GAStrings.ATTARCTION);
        easyTracker.send(MapBuilder
                        .createAppView()
                        .build()
        );
    }

    private void initializeViews() {
        headerSearch.setVisibility(View.VISIBLE);
        searchFragment = (SearchFragment) getSupportFragmentManager().findFragmentById(R.id.search_fragment);

        Cursor c = getContentResolver().query(ContentURIs.SENTOSA_URI, null, Queries.ATTRACTION_DETAIL_QUERY, new String[]{Integer.toString(nodeID)}, null);

        if (RepoTools.isRowAvailable(c)) {
            nodeTitle = RepoTools.getString(c, NodeDetailsData.TITLE_COL);
            nodeName.setText(nodeTitle);

            this.addCollapsible(Node_Event_PromoDetailCollapsibleView.DESCRIPTION, RepoTools.getString(c, NodeDetailsData.DESCRIPTION_COL));
            this.addCollapsible("Opening Hours", RepoTools.getString(c, NodeDetailsData.OPENING_TIMES_COL));
            this.addCollapsible("Location", RepoTools.getString(c, NodeDetailsData.SECTION_COL));
            this.addCollapsible("Pricing", RepoTools.getString(c, NodeDetailsData.ADMISSION_COL));
            this.addCollapsible("Requirements", RepoTools.getString(c, NodeDetailsData.OTHER_DETAILS_COL));
            this.addCollapsible("Email", RepoTools.getString(c, NodeDetailsData.EMAIL_COL));
            this.addCollapsible("Website", RepoTools.getString(c, NodeDetailsData.WEBSITE_COL));

            categoryName = RepoTools.getString(c, NodeDetailsData.CATEGORY_COL);
            headerTitle.setText(categoryName);

            contactNumber = RepoTools.getString(c, NodeDetailsData.CONTACT_NO_COL);
            if (!SentosaUtils.isValidString(contactNumber)) {
                callUs.setVisibility(View.GONE);
            }

            videoLink = RepoTools.getString(c, NodeDetailsData.VIDEO_URL_COL);
            if (!SentosaUtils.isValidString(videoLink)) {
                playVideo.setVisibility(View.GONE);
            }

            isBookmarked = RepoTools.getInt(c, NodeDetailsData.IS_BOOKMARKED_COL);
            if (isBookmarked == 1) {
                bookmark.setImageResource(R.drawable.bt_bookmark_remove);
            }

            nodeDetailsID = RepoTools.getInt(c, NodeDetailsData.ID_COL);

            String imagePath = RepoTools.getString(c, NodeDetailsData.IMAGE_NAME_COL);
            setImg(imagePath);

            currentNodeDetails = new ContentValues();
            DatabaseUtils.cursorRowToContentValues(c, currentNodeDetails);
        }

        c.close();

        getSupportLoaderManager().initLoader(Const.EVENT_LIST_LOADER, null, this);
        getSupportLoaderManager().initLoader(Const.PROMOTION_LIST_LOADER, null, this);

        logFlurry(FlurryStrings.LocationDetail, false);
    }

    private void setImg(String imagePath) {
        String imgUrl;
        if(imagePath != null && imagePath.length() > 0) {
            imgUrl = HttpHelper.BASE_HOST + imagePath;
        } else {
            if (categoryName.equals(Const.ATTRACTION)
                    || categoryName.equals(Const.FNB)
                    || categoryName.equals(Const.HOTEL_AND_SPA)
                    || categoryName.equals(Const.SHOPPING)
                    || categoryName.equals(Const.BUS)
                    || categoryName.equals(Const.TRAIN)
                    || categoryName.equals(Const.TRAM)) {
                imgUrl = HttpHelper.BASE_HOST + IMAGE_PATH_SUFFIX + nodeDetailsID + ".jpg";
            } else {
                imgUrl = HttpHelper.BASE_HOST + IMAGE_PATH_SUFFIX + "category_" + categoryName.toLowerCase().replace("'", "").replace(' ', '_').trim() + ".png";
            }
        }

        Log.d(TAG, "imageUrl: " + imgUrl);
        if (SentosaUtils.isValidString(imgUrl)) {
            mImageFetcher.loadImage(imgUrl, imgView, progressBar, R.drawable.stub_large, false, null);
        }
    }

    @OnClick(R.id.play_video)
    public void playVideo() {
        logFlurry(FlurryStrings.LocationDetailsVideo, false);
        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(videoLink)));
    }

    @OnClick(R.id.directions)
    public void getDirections() {
        logFlurry(FlurryStrings.DirectionsFromLocationDetails, false);
        logFlurry(FlurryStrings.MapRouting, false);

        Intent intent = null;

        if (sourceActivity == ACTIVITY_HOME
                || sourceActivity == ACTIVITY_EVENT_PROMO
                || sourceActivity == ACTIVITY_MYBOOKMARKS
                || sourceActivity == ACTIVITY_THINGS_TO_DO) {

            intent = new Intent(NodeDetailActivity.this, MapActivity.class);
            intent.putExtra(MapActivity.ROUTE_TO_NODE, nodeID);
            intent.putExtra(MapActivity.ROUTE_TO_NODE_TEXT, nodeTitle);
            intent.putExtra(MapActivity.IS_WALK_ONLY, false);
            intent.putExtra(MapActivity.START_FROM_ANOTHER_ACTIVITY, true);

            startActivity(intent);

        } else if (sourceActivity == ACTIVITY_MAP) {
            intent = new Intent(NodeDetailActivity.this, MapActivity.class);
            intent.putExtra(MapActivity.ROUTE_TO_NODE, nodeID);
            intent.putExtra(MapActivity.ROUTE_TO_NODE_TEXT, nodeTitle);
            intent.putExtra(MapActivity.START_FROM_ANOTHER_ACTIVITY, true);
            intent.putExtra(MapActivity.IS_WALK_ONLY, false);

            startActivity(intent);
        }
    }

    @OnClick(R.id.call_us)
    public void callUs() {
        showDialog(0);
    }

    @OnClick(R.id.bookmark)
    public void clickBookmark() {
        String message;
        if (isBookmarked == 1) {
            isBookmarked = 0;
            bookmark.setImageResource(R.drawable.bt_bookmark_add);
            message = "Bookmark deleted";
        } else {
            isBookmarked = 1;
            bookmark.setImageResource(R.drawable.bt_bookmark_remove);
            message = "Attraction bookmarked";
        }

        currentNodeDetails.remove(NodeDetailsData.IS_BOOKMARKED_COL);
        currentNodeDetails.put(NodeDetailsData.IS_BOOKMARKED_COL, isBookmarked);

        getContentResolver().update(ContentURIs.NODE_DETAILS_URI, currentNodeDetails, SentosaContentProvider.TABLE_NODE_DETAILS + "." + NodeDetailsData.ID_COL + "=?", new String[]{nodeDetailsID + ""});

        Toast tMessage = Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG);
        tMessage.setGravity(Gravity.CENTER, 0, 0);
        tMessage.show();
    }

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

    @Override
    protected Dialog onCreateDialog(int id, Bundle args) {
        if (id == 0) {
            AlertDialog.Builder builderCall = new AlertDialog.Builder(this);
            builderCall.setTitle("Call " + contactNumber + "?");
            builderCall.setNegativeButton("Cancel", new android.content.DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });

            builderCall.setPositiveButton("Call", new android.content.DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    logFlurry(FlurryStrings.LocationDetailsCall, false);
                    if (contactNumber != null)
                        SentosaUtils.startToCall(
                                NodeDetailActivity.this, contactNumber);
                    dialog.dismiss();
                }
            });

            return builderCall.create();
        }

        return null;
    }

    private void addCollapsible(String title, String description) {
        if (SentosaUtils.isValidString(description)) {
            collapsiblesContainer.addView(new Node_Event_PromoDetailCollapsibleView(this, title, description));
        }
    }

    private void logFlurry(String eventName, boolean isWalk) {
        if (eventName == null || eventName.equals(""))
            return;

        HashMap<String, String> attr = new HashMap<String, String>();
        attr.put(FlurryStrings.NameOfLocation, this.nodeTitle);
        attr.put(FlurryStrings.Category, this.categoryName);

        if (eventName.equals(FlurryStrings.MapRouting)) {
            attr.put(FlurryStrings.TypeOfRouting, isWalk ? FlurryStrings.WALK : FlurryStrings.TRANSPORT);
        }

        FlurryAgent.logEvent(eventName, attr);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        CursorLoader loader = null;

        switch (id) {
            case Const.EVENT_LIST_LOADER:
                loader = new CursorLoader(this, ContentURIs.EVENTS_URI, null, Queries.RELATED_EVENTS_PROMOTIONS_QUERY(true, nodeDetailsID), null, null);
                break;

            case Const.PROMOTION_LIST_LOADER:
                loader = new CursorLoader(this, ContentURIs.PROMOTIONS_URI, null, Queries.RELATED_EVENTS_PROMOTIONS_QUERY(false, nodeDetailsID), null, null);
                break;
        }

        return loader;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        inflateList(cursor, loader.getId());
        GetLocationIdAsyncTask getLocation = new GetLocationIdAsyncTask(this, nodeTitle);
        getLocation.execute();
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        inflateList(null, loader.getId());
    }

    private void inflateList(Cursor cursor, int type) {
        if (type == Const.EVENT_LIST_LOADER) {
            if (eventsCollapsible != null) {
                collapsiblesContainer.removeView(eventsCollapsible);
                eventsCollapsible = null;
            }

            if (cursor != null && cursor.getCount() > 0) {
                eventsCollapsible = new Node_Event_PromoDetailCollapsibleView(this, Node_Event_PromoDetailCollapsibleView.EVENT, cursor);
                int index = collapsiblesContainer.getChildCount() - (promosCollapsible == null ? 0 : 1);
                collapsiblesContainer.addView(eventsCollapsible, index);
            }
        } else {
            if (promosCollapsible != null) {
                collapsiblesContainer.removeView(promosCollapsible);
                promosCollapsible = null;
            }
            if (cursor != null && cursor.getCount() > 0) {
                promosCollapsible = new Node_Event_PromoDetailCollapsibleView(this, Node_Event_PromoDetailCollapsibleView.PROMOTION, cursor);
                collapsiblesContainer.addView(promosCollapsible);
            }
        }
    }
}