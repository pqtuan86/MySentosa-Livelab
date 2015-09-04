package com.mysentosa.android.sg;


import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.flurry.android.FlurryAgent;
import com.mysentosa.android.sg.adapter.MapFilterCursorAdapter;
import com.mysentosa.android.sg.fragments.SearchFragment;
import com.mysentosa.android.sg.provider.utils.SentosaDatabaseStructure.ContentURIs;
import com.mysentosa.android.sg.provider.utils.SentosaDatabaseStructure.Queries;
import com.mysentosa.android.sg.utils.Const;
import com.mysentosa.android.sg.utils.Const.FlurryStrings;
import com.mysentosa.android.sg.utils.SentosaUtils;

import java.util.ArrayList;
import java.util.HashMap;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

public class MapFilterListActivity extends BaseActivity {

    public static final String TAG = MapFilterListActivity.class.getSimpleName();

    public static final String CATEGORY_ACTIVITY_RESULT = "CATEGORY_ACTIVITY_RESULT";
    public static final String CURRENT_CATEGORIES_LIST = "CURRENT_CATEGORIES_STRING";

    public static final int FILTER_ACTIVITY_REQUEST_CODE = 668;
    public static int DEFAULT_ITEM = 6;

    @InjectView(R.id.header_title) TextView headerTitle;
    @InjectView(R.id.header_search) ImageView headerSearch;
    @InjectView(R.id.search_frame) FrameLayout searchFrame;
    @InjectView(R.id.map_filter) ListView filterList;
    @InjectView(R.id.filter_cancel) ImageView filterCancel;
    @InjectView(R.id.filter_done) ImageView filterDone;

    private ArrayList<String> filteredCategory;
    private ArrayList<String> defaultCategories;

    private MapFilterCursorAdapter mAdapter;
    private SearchFragment searchFragment;

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.map_filter_screen);
        ButterKnife.inject(this);

        setDefaultCategories();
        initializeViews();
    }

    private void setDefaultCategories() {
        defaultCategories = new ArrayList<String>();
        defaultCategories.add(Const.BUS);
        defaultCategories.add(Const.TRAIN);
        defaultCategories.add(Const.TRAM);
        defaultCategories.add(Const.CABLE);
        defaultCategories.add(Const.BUS_INT);
        defaultCategories.add(Const.MRT);
        defaultCategories.add(Const.CABLEWAY);
    }

    private void initializeViews() {
        headerTitle.setText(R.string.map);
        headerSearch.setVisibility(View.VISIBLE);
        searchFragment = (SearchFragment) getSupportFragmentManager().findFragmentById(R.id.search_fragment);

        SentosaUtils.addListViewFooter(this, filterList, false);
        setListViewData();
    }

    private void setListViewData() {
        String[] array = defaultCategories.toArray(new String[defaultCategories.size()]);

        ArrayList<String> currentCategories = this.getIntent().getStringArrayListExtra(CURRENT_CATEGORIES_LIST);
        if (currentCategories == null) {
            currentCategories = new ArrayList<String>();
        }

        Cursor c = getContentResolver().query(ContentURIs.SENTOSA_URI, null, Queries.CATEGORIES_LIST_FILTER_QUERY(array.length), array, null);
        mAdapter = new MapFilterCursorAdapter(this, c, currentCategories);

        filterList.setAdapter(mAdapter);
    }

    @OnClick(R.id.filter_cancel)
    public void cancelFilter() {
        MapFilterListActivity.this.finish();
    }

    @OnClick(R.id.filter_done)
    public void doneFilter() {
        filteredCategory = mAdapter.getFilteredCategory();

        HashMap<String, String> attr = new HashMap<String, String>();
        for (String category : filteredCategory) {
            Log.d("test", "test category chosen: " + category);
            attr.put(FlurryStrings.CategorySelected, category);
            FlurryAgent.logEvent(FlurryStrings.MapPageFilterCategoriesChosen, attr);
        }

        Intent resultIntent = new Intent();
        resultIntent.putStringArrayListExtra(CATEGORY_ACTIVITY_RESULT, filteredCategory); //here it is the category selected
        MapFilterListActivity.this.setResult(RESULT_OK, resultIntent);
        MapFilterListActivity.this.finish();
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
}