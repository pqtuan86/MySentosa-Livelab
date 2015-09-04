package com.mysentosa.android.sg;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.location.Location;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Parcelable;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AutoCompleteTextView;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.analytics.tracking.android.EasyTracker;
import com.google.analytics.tracking.android.Fields;
import com.google.analytics.tracking.android.MapBuilder;
import com.mysentosa.android.sg.adapter.DirectionsAdapter;
import com.mysentosa.android.sg.adapter.DirectionsAutoCompleteAdapter;
import com.mysentosa.android.sg.fragments.SearchFragment;
import com.mysentosa.android.sg.location.LocationFinder;
import com.mysentosa.android.sg.location.LocationNotifier;
import com.mysentosa.android.sg.location.PlacesConstants;
import com.mysentosa.android.sg.map.MapDataManager;
import com.mysentosa.android.sg.map.models.Direction;
import com.mysentosa.android.sg.provider.utils.SentosaDatabaseStructure.ContentURIs;
import com.mysentosa.android.sg.provider.utils.SentosaDatabaseStructure.Queries;
import com.mysentosa.android.sg.utils.Const;
import com.mysentosa.android.sg.utils.ResourceProxyImpl;
import com.mysentosa.android.sg.utils.SentosaUtils;

import org.osmdroid.util.GeoPoint;

import java.util.ArrayList;
import java.util.Iterator;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import butterknife.OnItemClick;

public class DirectionsActivity extends BaseActivity {

    public static final String TAG = DirectionsActivity.class.getSimpleName();

    public static int DIALOG_LOCATION = 1;
    public static int DIALOG_LOADING = 2;
    private static final int TIMEOUT_BEFORE_DIRECTIONS = 1;

    @InjectView(R.id.header_title) TextView headerTitle;
    @InjectView(R.id.header_map) ImageView headerMap;
    @InjectView(R.id.header_search) ImageView headerSearch;
    @InjectView(R.id.search_frame) FrameLayout searchFrame;
    @InjectView(R.id.filter_from) AutoCompleteTextView filterFrom;
    @InjectView(R.id.filter_to) AutoCompleteTextView filterTo;
    @InjectView(R.id.directions_lv) ListView list;
    @InjectView(R.id.get_directions) ImageView getDirections;
    @InjectView(R.id.get_directions_tram) ImageView directionTram;
    @InjectView(R.id.get_directions_walk) ImageView directionWalk;

    private DirectionsAutoCompleteAdapter mCursorAdapter;
    private NfcAdapter mNfcAdapter;
    DirectionsAdapter mAdapter = null;

    ArrayList<Direction> directions = null;
    LocationFinder locationFinder;
    Location currentLocation;
    MapDataManager mdm;
    ResourceProxyImpl mResourceProxy;
    Handler handler;

    int directionsPosition = 0;
    int fromNode = -1, toNode = -1;

    boolean useLocation, directionsGenerated;
    boolean isNFCEnabled = false;
    boolean walkOnly = false;

    private SearchFragment searchFragment;
    private View footerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.directions_screen);
        ButterKnife.inject(this);

        initializeViews();
        initCursorAdapter();

        mNfcAdapter = NfcAdapter.getDefaultAdapter(this);

        if (mNfcAdapter != null && mNfcAdapter.isEnabled()) {
            isNFCEnabled = true;
        } else {
            isNFCEnabled = false;
        }

        mResourceProxy = new ResourceProxyImpl(getApplicationContext());
        mdm = new MapDataManager(this, mResourceProxy);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            restoreFromBundle(extras);
        }

        initItemFilter();
        setDirectionsTypeButtonImage();

        EasyTracker easyTracker = EasyTracker.getInstance(this);
        easyTracker.set(Fields.SCREEN_NAME, Const.GAStrings.DIRECTIONS);
        easyTracker.send(MapBuilder
                        .createAppView()
                        .build()
        );
    }

    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(MapActivity.ROUTE_TO_NODE, toNode);
        outState.putInt(MapActivity.ROUTE_FROM_NODE, fromNode);
        outState.putBoolean(MapActivity.IS_WALK_ONLY, walkOnly);
        outState.putInt(MapActivity.DIRECTIONS_POSITION, directionsPosition);
        outState.putBoolean(MapActivity.DISPLAY_DIRECTIONS, directionsGenerated);
    }

    protected void onRestoreInstanceState(Bundle savedState) {
        restoreFromBundle(savedState);
        setDirectionsTypeButtonImage();
    }

    private void restoreFromBundle(Bundle extras) {
        toNode = extras.getInt(MapActivity.ROUTE_TO_NODE, -1);
        fromNode = extras.getInt(MapActivity.ROUTE_FROM_NODE, -1);
        walkOnly = extras.getBoolean(MapActivity.IS_WALK_ONLY, false);
        directionsPosition = extras.getInt(MapActivity.DIRECTIONS_POSITION, 0);
        String fromNodeText = extras.getString(MapActivity.ROUTE_FROM_NODE_TEXT);
        String toNodeText = extras.getString(MapActivity.ROUTE_TO_NODE_TEXT);

        if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(getIntent().getAction())) {
            toNodeText = getNodeFromNFC();
        }

        if (toNode != -1 && toNodeText != null) {
            filterTo.setText(toNodeText);
            filterTo.setTag(toNodeText);
        }

        if (fromNode != -1 && fromNodeText != null) {
            useLocation = false;
            filterFrom.setText(fromNodeText);
            filterFrom.setTag(fromNodeText);
        }

        filterFrom.clearFocus();
        filterTo.clearFocus();

        if (extras.getBoolean(MapActivity.DISPLAY_DIRECTIONS, true) && validate(false)) {
            displayDirections();
        }

        if (fromNode == -1) {
            detectLocation();
        }
    }

    private String getNodeFromNFC() {
        String toNodeText = "";
        Log.d("NFC", "NDEF_Discovered");
        Parcelable[] rawMsgs = getIntent().getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);

        NdefRecord dataRecord = ((NdefMessage) rawMsgs[0]).getRecords()[1];
        String nfcData = new String(dataRecord.getPayload());
        String numberOnly = nfcData.replaceAll("[^0-9]", "");

        try {
            toNode = Integer.parseInt(numberOnly);
            Cursor c = this.getContentResolver().query(ContentURIs.SENTOSA_URI,
                    null, Queries.TITLE_QUERY, new String[]{toNode + ""}, null);
            if (c.moveToFirst()) {
                toNodeText = c.getString(0);
            }
            c.close();
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }

        return toNodeText;
    }

    @Override
    protected void onResume() {
        super.onResume();
        Cursor mItemCursor = getContentResolver().query(ContentURIs.SENTOSA_URI, null, Queries.DIRECTIONS_AUTOCOMPLETE_DEFAULT_QUERY, null, null);
        mCursorAdapter.swapCursor(mItemCursor);
    }

    @Override
    protected void onPause() {
        // TODO Auto-generated method stub
        super.onPause();
        mCursorAdapter.swapCursor(null);
    }

    private void initializeViews() {
        headerTitle.setText(R.string.directions);
        headerMap.setVisibility(View.VISIBLE);
        headerSearch.setVisibility(View.VISIBLE);
        searchFragment = (SearchFragment) getSupportFragmentManager().findFragmentById(R.id.search_fragment);

        footerView = SentosaUtils.addListViewFooter(this, R.layout.item_direction_footer, list, true);
        directions = new ArrayList<Direction>();
        mAdapter = new DirectionsAdapter(this, directions);
        list.setAdapter(mAdapter);
    }

    @OnClick(R.id.header_map)
    public void backToMap() {
        backToMap(-1);
    }

    @OnClick(R.id.header_search)
    public void openSearch() {
        if (searchFrame.getVisibility() == View.GONE) {
            searchFrame.setVisibility(View.VISIBLE);
        } else {
            closeSearch();
        }
    }

    @OnClick(R.id.get_directions_tram)
    public void getDirectionsTram() {
        hideSoftKeyboard();
        clearDirections();
        walkOnly = false;
        setDirectionsTypeButtonImage();
        if (directionsGenerated && validate(true)) {
            displayDirections();
        }
    }

    @OnClick(R.id.get_directions_walk)
    public void getDirectionsWalk() {
        hideSoftKeyboard();
        clearDirections();
        walkOnly = true;
        setDirectionsTypeButtonImage();
        if (directionsGenerated && validate(true)) {
            displayDirections();
        }
    }

    @OnClick(R.id.get_directions)
    public void getDirections() {
        hideSoftKeyboard();
        clearDirections();
        if (validate(true)) {
            displayDirections();
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

    @OnItemClick(R.id.directions_lv)
    public void pickDirections(int position) {
        Direction d = directions.get(position);
        if (position == directions.size() - 1) { // last item
            backToMap(d.getEdge().getToNode().getID());
        } else {
            backToMap(d.getEdge().getFromNode().getID());
        }
    }

    private void backToMap(int centerOn) {
        Intent intent = new Intent();
        intent.putExtra(NavigationManagerActivity.ACTIVITY_TO_START, MapActivity.class.getName());

        if (validate(false) && directionsGenerated) {
            intent.putExtra(MapActivity.ROUTE_TO_NODE, toNode);
            intent.putExtra(MapActivity.ROUTE_TO_NODE_TEXT, filterTo.getText().toString().trim());
            intent.putExtra(MapActivity.ROUTE_FROM_NODE_TEXT, filterFrom.getText().toString().trim());
            intent.putExtra(MapActivity.ROUTE_CENTER_FOCUS, centerOn);

            if (!useLocation) {
                intent.putExtra(MapActivity.ROUTE_FROM_NODE, fromNode);
            }
            intent.putExtra(MapActivity.IS_WALK_ONLY, walkOnly);
        }

        setResult(RESULT_OK, intent);
        finish();
    }

    private boolean validate(boolean showErrors) {
        if (filterFrom.getText().toString().trim().length() == 0 || (!useLocation && fromNode == -1)) {
            if (showErrors) {
                Toast.makeText(this, "Please select your starting location", Toast.LENGTH_SHORT).show();
            }

            return false;
        }

        if (filterTo.getText().toString().trim().length() == 0 || toNode == -1) {
            if (showErrors) {
                Toast.makeText(this, "Please select the destination location", Toast.LENGTH_SHORT).show();
            }

            return false;
        }

        return true;
    }

    private void displayDirections() {
        if (!mdm.loadMapTask.isCompleted() || (useLocation && currentLocation == null)) {
            initializeDisplayDirectionsThread();
            return;
        }

        ArrayList<Direction> directions_temp = null;
        if (useLocation && currentLocation != null) {
            directions_temp = mdm.directionsTo(toNode, walkOnly, new GeoPoint(currentLocation.getLatitude(), currentLocation.getLongitude()));
        } else {
            directions_temp = mdm.directionsTo(toNode, walkOnly, fromNode);
        }

        if (directions_temp != null && directions_temp.size() > 0) {
            Iterator<Direction> directionsIterator = directions_temp.iterator();
            directions.clear();

            while (directionsIterator.hasNext()) {
                directions.add(directionsIterator.next());
            }

            mAdapter.notifyDataSetChanged();
            directionsGenerated = true;
        }

        if (directionsPosition > 0) {
            list.setSelection(directionsPosition - 1);
            directionsPosition = 0;
        }

        footerView.setVisibility(View.VISIBLE);
    }

    private void initCursorAdapter() {
        mCursorAdapter = new DirectionsAutoCompleteAdapter(getApplicationContext(), null);
    }

    private void initItemFilter() {
        filterFrom.setAdapter(mCursorAdapter);
        filterFrom.setThreshold(1);
        filterFrom.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
                                    long arg3) {
                fromNode = (int) arg3;
                useLocation = false;
                directionsGenerated = false;
                clearDirections();
                filterFrom.setTag(arg1.getTag().toString());
                filterFrom.setText(arg1.getTag().toString());

                filterFrom.clearFocus();
                filterTo.clearFocus();
            }
        });

        filterFrom.setOnTouchListener(new OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (filterFrom.getText().toString().equals(getString(R.string.my_location))) {
                    filterFrom.setText("");
                    fromNode = -1;
                    useLocation = false;
                }
                return false;
            }
        });

        filterFrom.addTextChangedListener(new TextWatcher() {

            public void afterTextChanged(Editable s) {
                if (filterFrom.getTag() != null && !filterFrom.getText().toString().equals(filterFrom.getTag().toString()) && (fromNode != -1 || useLocation == true)) {
                    fromNode = -1;
                    useLocation = false;
                }
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }
        });

        filterTo.setAdapter(mCursorAdapter);
        filterTo.setThreshold(1);
        filterTo.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
                                    long arg3) {
                toNode = (int) arg3;
                directionsGenerated = false;
                clearDirections();
                filterTo.setTag(arg1.getTag().toString());
                filterTo.setText(arg1.getTag().toString() + "");

                filterFrom.clearFocus();
                filterTo.clearFocus();
            }
        });

        filterTo.addTextChangedListener(new TextWatcher() {

            public void afterTextChanged(Editable s) {
                if (filterTo.getTag() != null && !filterTo.getText().toString().equals(filterTo.getTag().toString()) && toNode != -1) {
                    toNode = -1;
                }
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }
        });
    }

    private void detectLocation() {
        DirectionsActivity.this.showDialog(DIALOG_LOCATION);
        locationFinder = new LocationFinder(DirectionsActivity.this, new LocationNotifier() {
            @Override
            public void updatedLocation(Location location) {
                if (location != null) {
                    currentLocation = location;
                    DirectionsActivity.this.removeDialog(DIALOG_LOCATION);

                    GeoPoint gp = new GeoPoint(currentLocation.getLatitude(), currentLocation.getLongitude());
                    if (filterFrom.getText().toString().trim().length() == 0 && MapActivity.BOUNDING_BOX.contains(gp)) {
                        useLocation = true;
                        filterFrom.setText(R.string.my_location);
                    }
                }
            }

            @Override
            public void listenersDisabled() {
                DirectionsActivity.this.removeDialog(DIALOG_LOCATION);
            }
        }, PlacesConstants.MAX_TIME_TO_GET_LOCATION);

        locationFinder.requestLocationUpdates();
    }

    @Override
    protected Dialog onCreateDialog(int id, Bundle args) {
        if (id == DIALOG_LOCATION) {
            ProgressDialog pd = new ProgressDialog(this);
            pd.setMessage("Determining location...");
            pd.setCancelable(false);
            return pd;
        }
        if (id == DIALOG_LOADING) {
            ProgressDialog pd = new ProgressDialog(this);
            pd.setMessage("Loading directions..");
            pd.setCancelable(false);
            return pd;
        }
        return super.onCreateDialog(id);
    }

    private void clearDirections() {
        directions.clear();
        mAdapter.notifyDataSetChanged();
    }

    private void setDirectionsTypeButtonImage() {
        if (walkOnly) {
            directionTram.setImageResource(R.drawable.bt_tram_directions_normal);
            directionWalk.setImageResource(R.drawable.bt_walk_directions_pressed);
        } else {
            directionTram.setImageResource(R.drawable.bt_tram_directions_pressed);
            directionWalk.setImageResource(R.drawable.bt_walk_directions_normal);
        }
    }

    private void hideSoftKeyboard() {
        ((InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(filterFrom.getWindowToken(), 0);
        ((InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(filterTo.getWindowToken(), 0);
    }

    private void initializeDisplayDirectionsThread() {
        DirectionsActivity.this.showDialog(DIALOG_LOADING);
        handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case TIMEOUT_BEFORE_DIRECTIONS:
                        if (validate(true))
                            displayDirections();

                        DirectionsActivity.this.removeDialog(DIALOG_LOADING);
                }
            }
        };

        Thread splash = new Thread() {
            @Override
            public void run() {
                boolean breakLoop = false;
                while (!breakLoop) {
                    try {
                        sleep(1000);
                    } catch (InterruptedException e) {
                        breakLoop = true;
                    }

                    if (mdm.loadMapTask.isCompleted() && (!useLocation || currentLocation != null)) {
                        breakLoop = true;
                    }
                }
                handler.sendEmptyMessage(TIMEOUT_BEFORE_DIRECTIONS);
            }
        };
        splash.start();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        Log.d("NFC", "onNewIntent");
        super.onNewIntent(intent);
    }
}