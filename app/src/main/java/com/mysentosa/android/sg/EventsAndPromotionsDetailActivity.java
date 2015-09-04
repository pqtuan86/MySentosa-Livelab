package com.mysentosa.android.sg;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.flurry.android.FlurryAgent;
import com.google.gson.Gson;
import com.mysentosa.android.sg.custom_views.Node_Event_PromoDetailCollapsibleView;
import com.mysentosa.android.sg.models.Promotion;
import com.mysentosa.android.sg.provider.utils.JSONParseUtil;
import com.mysentosa.android.sg.provider.utils.SentosaDatabaseStructure.ContentURIs;
import com.mysentosa.android.sg.provider.utils.SentosaDatabaseStructure.EventsPromotionsBase;
import com.mysentosa.android.sg.provider.utils.SentosaDatabaseStructure.Queries;
import com.mysentosa.android.sg.share.FacebookShare;
import com.mysentosa.android.sg.share.FacebookShare.FacebookSharedItem;
import com.mysentosa.android.sg.share.TwitterShare;
import com.mysentosa.android.sg.utils.Const.FlurryStrings;
import com.mysentosa.android.sg.utils.HttpHelper;
import com.mysentosa.android.sg.utils.SentosaUtils;

import org.apache.http.NameValuePair;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class EventsAndPromotionsDetailActivity extends BaseActivity {

    private long epID;

    public static final int INVALID_LOC = -1;
    public static final String CURRENT_TYPE = "CURRENT_TYPE", ISLANDER_CLAIMED_DEAL = "ISLANDER",
            ID = "ID"; // ID is used to launch this activity
    // from notification
    public static final int TYPE_EVENT = 0, TYPE_PROMOTION = 1;
    private int currentType = TYPE_EVENT;

    private ImageView imgView;
    private ImageView btnCallUs, btnFacebookShare, btnTwitterShare, btnLinkedTicket;
    private LinearLayout collapsiblesContainer;

    private boolean fbShared, twitterShared;
    private FacebookShare facebookShare;
    private TwitterShare twitterShare;
    private ProgressBar pbLoading;
    private static final String DEFAULT_LINK = "http://sentosa.com", EMAIL = "Email";

    private String description, imgUrl, contactNumber, socialLink, detailTitle,
            detailDate, detailTime, detailLinkedTicket;
    protected static final String EVENTS = "Events", PROMOTIONS = "Deals";
    protected String callEvent, twitterShareEvent, facebookShareEvent,
            emailEvent, linkedTicketEvent;

    private boolean isFromIslanderClaimed = false;
    private Promotion currentPromotion;

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.event_promo_detail_screen);
        Intent intent = this.getIntent();
        if (intent != null) {
            epID = intent.getLongExtra(EventsPromotionsBase.ID_COL, -1);
            currentType = intent.getIntExtra(CURRENT_TYPE, -1);
            if (currentType == -1 || epID == -1)
                finish();
        }

        setFlurryStrings();
        initializeViews();
        setProgressDialogVisibility(true);

        if (currentType == TYPE_PROMOTION) {
            String claimedDealJSON = intent.getStringExtra(ISLANDER_CLAIMED_DEAL);
            if (SentosaUtils.isValidString(claimedDealJSON)) {
                isFromIslanderClaimed = true;
                currentPromotion = new Gson().fromJson(claimedDealJSON, Promotion.class);
                showDetail();
            }
        }

        if (!isFromIslanderClaimed) {
            boolean success = queryData(false);
            if (!success)
                new LoadEventTask().execute();
        }
        setListeners();
    }

    private void initializeViews() {
        ((TextView) findViewById(R.id.header_title))
                .setText(currentType == TYPE_EVENT ? EVENTS : PROMOTIONS);
        imgView = (ImageView) findViewById(R.id.iv_detail_img);
        collapsiblesContainer = (LinearLayout) findViewById(R.id.container);

        btnCallUs = (ImageView) findViewById(R.id.call_us);
        btnFacebookShare = (ImageView) findViewById(R.id.iv_facebook_share);
        btnTwitterShare = (ImageView) findViewById(R.id.iv_twitter_share);
        btnLinkedTicket = (ImageView) findViewById(R.id.iv_linked_ticket);
        pbLoading = (ProgressBar) this.findViewById(R.id.pb_main_loading);

        facebookShare = new FacebookShare(this);
        twitterShare = new TwitterShare(this);
    }

    protected boolean queryData(boolean skipDetailQuery) {
        if (!skipDetailQuery) {
            String detailRawQuery = Queries.EVENTS_PROMOTIONS_DETAIL_QUERY(
                    currentType == TYPE_EVENT, epID);
            Cursor detailCursor = this.getContentResolver().query(
                    ContentURIs.SENTOSA_URI, null, detailRawQuery, null, null);
            if (detailCursor == null || detailCursor.getCount() == 0)
                return false;
            ContentValues contentValues = new ContentValues();
            detailCursor.moveToFirst();
            DatabaseUtils.cursorRowToContentValues(detailCursor, contentValues);
            showDetail(contentValues);
        }

        new LoadLocationTask().execute();
        return true;
    }

    protected void setFlurryStrings() {
        if (currentType == TYPE_EVENT) {
            callEvent = FlurryStrings.EventDetailsCall;
            twitterShareEvent = FlurryStrings.EventDetailsTwitter;
            facebookShareEvent = FlurryStrings.EventDetailsFacebook;
            emailEvent = FlurryStrings.EventDetailsEmail;
            linkedTicketEvent = FlurryStrings.EventDetailsLinked;
        } else {
            callEvent = FlurryStrings.PromotionsDetailsCall;
            twitterShareEvent = FlurryStrings.PromotionsDetailsTwitter;
            facebookShareEvent = FlurryStrings.PromotionsDetailsFacebook;
            emailEvent = FlurryStrings.PromotionsDetailsEmail;
            linkedTicketEvent = FlurryStrings.PromotionsDetailsLinked;
        }
    }

    private OnClickListener btnClickListener = new OnClickListener() {

        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.call_us:
                    showDialog(0);
                    break;
                case R.id.iv_twitter_share:
                    FlurryAgent.logEvent(twitterShareEvent);
                    twitterShare.sendTweet(socialLink);
                    break;
                case R.id.iv_facebook_share:
                    FlurryAgent.logEvent(facebookShareEvent);
                    if (SentosaUtils.isValidString(detailDate)
                            && SentosaUtils.isValidString(detailTime))
                        facebookShare.share(new FacebookSharedItem(detailTitle
                                + ": " + detailDate + ": " + detailTime,
                                description, socialLink, imgUrl));
                    else if (!SentosaUtils.isValidString(detailDate)
                            && SentosaUtils.isValidString(detailTime))
                        facebookShare.share(new FacebookSharedItem(detailTitle
                                + ": " + detailTime, description, socialLink,
                                imgUrl));
                    else if (SentosaUtils.isValidString(detailDate)
                            && !SentosaUtils.isValidString(detailTime))
                        facebookShare.share(new FacebookSharedItem(detailTitle
                                + ": " + detailDate, description, socialLink,
                                imgUrl));
                    else
                        facebookShare.share(new FacebookSharedItem(detailTitle,
                                description, socialLink, imgUrl));
                    break;
                case R.id.iv_linked_ticket:
                    FlurryAgent.logEvent(linkedTicketEvent);
                    Intent intent = new Intent(EventsAndPromotionsDetailActivity.this, TicketsActivity.class);
                    intent.putExtra(TicketsActivity.TICKET_TYPE, detailLinkedTicket);
                    startActivity(intent);
                    break;
                default:
                    if (v.getTag().equals(EMAIL))
                        FlurryAgent.logEvent(emailEvent);
                    break;
            }

        }

        ;
    };

    private void setListeners() {
        btnCallUs.setOnClickListener(btnClickListener);
        btnFacebookShare.setOnClickListener(btnClickListener);
        btnTwitterShare.setOnClickListener(btnClickListener);
        btnLinkedTicket.setOnClickListener(btnClickListener);
    }

    @Override
    protected Dialog onCreateDialog(int id, Bundle args) {
        if (id == 0) {
            AlertDialog.Builder builderCall = new AlertDialog.Builder(this);
            builderCall.setTitle("Call " + contactNumber + "?");
            // builder.setIcon(R.drawable.stub_thumb);
            builderCall.setNegativeButton("Cancel",
                    new android.content.DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
            builderCall.setPositiveButton("Call",
                    new android.content.DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            FlurryAgent.logEvent(callEvent);
                            if (contactNumber != null)
                                SentosaUtils.startToCall(
                                        EventsAndPromotionsDetailActivity.this,
                                        contactNumber);
                            dialog.dismiss();
                        }
                    });
            return builderCall.create();
        }
        return null;
    }

    private void showDetail(ContentValues contentValues) {
        if (contentValues != null) {
            setProgressDialogVisibility(false);
            detailTitle = contentValues
                    .getAsString(EventsPromotionsBase.TITLE_COL);
            ((TextView) findViewById(R.id.tv_eventpromo_name)).setText(detailTitle);

            detailDate = null;
            SimpleDateFormat sdf = new SimpleDateFormat("dd MMM");
            long startDate = contentValues
                    .getAsLong(EventsPromotionsBase.START_DATE_COL) * 1000;
            long endDate = contentValues
                    .getAsLong(EventsPromotionsBase.END_DATE_COL) * 1000;
            if (startDate != 0 && endDate != 0) {
                detailDate = "From " + sdf.format(new Date(startDate)) + " to "
                        + sdf.format(new Date(endDate));
            } else if (startDate == 0 && endDate != 0) {
                detailDate = "Till " + sdf.format(new Date(endDate));
            } else if (startDate != 0 && endDate == 0) {
                detailDate = "From " + sdf.format(new Date(startDate));
            }

            this.addCollapsible(Node_Event_PromoDetailCollapsibleView.DESCRIPTION, contentValues.getAsString(EventsPromotionsBase.DESCRIPTION_COL));
            this.addCollapsible("Date", detailDate);
            this.addCollapsible("Opening Hours", contentValues.getAsString(EventsPromotionsBase.TIME_TEXT_COL));
            this.addCollapsible("Location", contentValues.getAsString(EventsPromotionsBase.VENUE_COL));
            this.addCollapsible("Pricing", contentValues.getAsString(EventsPromotionsBase.ADMISSION_COL));
            this.addCollapsible("Details", contentValues.getAsString(EventsPromotionsBase.DETAIL_COL));
            this.addCollapsible("Web", contentValues.getAsString(EventsPromotionsBase.EXTERNAL_LINK_COL));
            this.addCollapsible(EMAIL, contentValues.getAsString(EventsPromotionsBase.EMAIL_COL));

            imgUrl = contentValues
                    .getAsString(EventsPromotionsBase.IMAGE_URL_COL);
            if (SentosaUtils.isValidString(imgUrl))
                mImageFetcher.loadImage(HttpHelper.BASE_HOST + imgUrl, imgView,
                        (ProgressBar) this.findViewById(R.id.pb_loading),
                        R.drawable.stub_large, false, null);

            contactNumber = contentValues
                    .getAsString(EventsPromotionsBase.CONTACT_COL);
            if (!SentosaUtils.isValidString(contactNumber))
                btnCallUs.setVisibility(View.GONE);

            Integer fbs = contentValues
                    .getAsInteger(EventsPromotionsBase.FACEBOOK_SHARED_COL);
            fbShared = false;
            if (fbs != null && fbs == 1)
                fbShared = true;
            if (!fbShared)
                btnFacebookShare.setVisibility(View.GONE);

            Integer tws = contentValues
                    .getAsInteger(EventsPromotionsBase.TWITTER_SHARED_COL);
            twitterShared = false;
            if (tws != null && tws == 1)
                twitterShared = true;
            if (!twitterShared)
                btnTwitterShare.setVisibility(View.GONE);

            socialLink = contentValues
                    .getAsString(EventsPromotionsBase.SOCIAL_NETWORK_URL_COL);
            if (!SentosaUtils.isValidString(socialLink))
                socialLink = DEFAULT_LINK;


            detailLinkedTicket = contentValues
                    .getAsString(EventsPromotionsBase.LINKEDTICKET_COL);
            if (detailLinkedTicket.equalsIgnoreCase("Unassigned")) {
                btnLinkedTicket.setVisibility(View.GONE);
            }

        }
    }

    private void showDetail() {
        setProgressDialogVisibility(false);
        detailTitle = currentPromotion.getTitle();
        ((TextView) findViewById(R.id.tv_eventpromo_name)).setText(detailTitle);

        detailDate = null;
        SimpleDateFormat sdf = new SimpleDateFormat("dd MMM");
        long startDate = currentPromotion.getVisibleStartDate() * 1000;
        long endDate = currentPromotion.getVisibleEndDate() * 1000;
        if (startDate != 0 && endDate != 0) {
            detailDate = "From " + sdf.format(new Date(startDate)) + " to "
                    + sdf.format(new Date(endDate));
        } else if (startDate == 0 && endDate != 0) {
            detailDate = "Till " + sdf.format(new Date(endDate));
        } else if (startDate != 0 && endDate == 0) {
            detailDate = "From " + sdf.format(new Date(startDate));
        }

        this.addCollapsible(Node_Event_PromoDetailCollapsibleView.DESCRIPTION, currentPromotion.getDescription());
        this.addCollapsible("Date", detailDate);
        //this.addCollapsible("Opening Hours",contentValues.getAsString(EventsPromotionsBase.TIME_TEXT_COL));
        //this.addCollapsible("Location",contentValues.getAsString(EventsPromotionsBase.VENUE_COL));
        //this.addCollapsible("Pricing",contentValues.getAsString(EventsPromotionsBase.ADMISSION_COL));
        this.addCollapsible("Details", currentPromotion.getDetail());
        //this.addCollapsible("Web",contentValues.getAsString(EventsPromotionsBase.EXTERNAL_LINK_COL));
        //this.addCollapsible(EMAIL,contentValues.getAsString(EventsPromotionsBase.EMAIL_COL));

        imgUrl = currentPromotion.getImageURL();
        if (SentosaUtils.isValidString(imgUrl))
            mImageFetcher.loadImage(HttpHelper.BASE_HOST + imgUrl, imgView,
                    (ProgressBar) this.findViewById(R.id.pb_loading),
                    R.drawable.stub_large, false, null);

        contactNumber = "";
        if (!SentosaUtils.isValidString(contactNumber))
            btnCallUs.setVisibility(View.GONE);

        Integer fbs = 0;
        fbShared = false;
        if (fbs != null && fbs == 1)
            fbShared = true;
        if (!fbShared)
            btnFacebookShare.setVisibility(View.GONE);

        Integer tws = 0;
        twitterShared = false;
        if (tws != null && tws == 1)
            twitterShared = true;
        if (!twitterShared)
            btnTwitterShare.setVisibility(View.GONE);

        socialLink = "";
        if (!SentosaUtils.isValidString(socialLink))
            socialLink = DEFAULT_LINK;


        detailLinkedTicket = "";
        if (detailLinkedTicket.equalsIgnoreCase("Unassigned")) {
            btnLinkedTicket.setVisibility(View.GONE);
        }

    }

    public void setProgressDialogVisibility(boolean isShown) {
        int pdVisibility = isShown ? View.VISIBLE : View.GONE;
        int containerVisibility = isShown ? View.GONE : View.VISIBLE;
        findViewById(R.id.sv_event_promo_detail_container).setVisibility(
                containerVisibility);
        pbLoading.setVisibility(pdVisibility);
    }

    private void inflateLocationList(Cursor cursor) {
        if (cursor.getCount() != 0) {
            View v = new Node_Event_PromoDetailCollapsibleView(this, Node_Event_PromoDetailCollapsibleView.LOCATION, cursor);
            collapsiblesContainer.addView(v);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (facebookShare != null) {
            facebookShare.authorizeCallback(requestCode, resultCode, data);
        }
    }

    public class LoadEventTask extends AsyncTask<Void, Void, Boolean> {
        private final String EVENTS = "Events/";
        private final String DATA_JSON = "Data", EVENT_JSON = "Event";
        private ContentValues contentValues;

        @Override
        protected void onPostExecute(Boolean success) {
            if (success && contentValues != null && contentValues.size() > 0) {
                showDetail(contentValues);
                queryData(true);
            } else {
                Toast.makeText(getApplicationContext(),
                        "Connection error, kindly check your mobile network availability.", Toast.LENGTH_LONG)
                        .show();
                finish();
            }
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            try {
                String response = this.getResponseFromServer();
                JSONObject eventJSON = new JSONObject(response).getJSONObject(
                        DATA_JSON).getJSONObject(EVENT_JSON);
                contentValues = JSONParseUtil
                        .getEventPromoContentValue(eventJSON);
                return true;
            } catch (Exception e) {
                e.printStackTrace();
            }
            return false;
        }

        private String getResponseFromServer() throws Exception {
            String requestUri = HttpHelper.BASE_ADDRESSV2 + EVENTS
                    + EventsAndPromotionsDetailActivity.this.epID;
            ArrayList<NameValuePair> params = null;
            String result = HttpHelper.sendRequestUsingGet(requestUri, params);
            return result;
        }
    }

    private void addCollapsible(String title, String description) {
        if (SentosaUtils.isValidString(description)) {
            Node_Event_PromoDetailCollapsibleView v = new Node_Event_PromoDetailCollapsibleView(this, title, description);
            if (title.equals(EMAIL)) {
                View tvDetail = v.findViewById(Node_Event_PromoDetailCollapsibleView.DETAIL_TEXT_VIEW);
                tvDetail.setTag(EMAIL);
                tvDetail.setOnClickListener(btnClickListener);
            }
            this.collapsiblesContainer.addView(v);
        }
    }

    public class LoadLocationTask extends AsyncTask<Void, Void, Boolean> {
        Cursor locationCursor;

        @Override
        protected void onPostExecute(Boolean success) {
            if (success)
                inflateLocationList(locationCursor);
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            String locationRawQuery = Queries
                    .EVENTS_PROMOTIONS_LOCATIONS_QUERY(
                            currentType == TYPE_EVENT, epID);
            locationCursor = EventsAndPromotionsDetailActivity.this
                    .getContentResolver().query(ContentURIs.SENTOSA_URI, null,
                            locationRawQuery, null, null);
            if (locationCursor != null)
                return true;
            return false;
        }
    }

}
