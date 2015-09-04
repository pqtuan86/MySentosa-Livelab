package com.mysentosa.android.sg;


import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.mysentosa.android.sg.adapter.ThingsToDoList_Bookmarks_CursorAdapter;
import com.mysentosa.android.sg.fragments.SearchFragment;
import com.mysentosa.android.sg.provider.utils.SentosaDatabaseStructure;
import com.mysentosa.android.sg.provider.utils.SentosaDatabaseStructure.ContentURIs;
import com.mysentosa.android.sg.provider.utils.SentosaDatabaseStructure.Queries;
import com.mysentosa.android.sg.utils.Const;
import com.mysentosa.android.sg.utils.RepoTools;
import com.mysentosa.android.sg.utils.SentosaUtils;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import butterknife.OnItemClick;

public class ThingsToDoCategoryListActivity extends BaseActivity {

    public static final String TAG = ThingsToDoCategoryListActivity.class.getSimpleName();

    public static final String CATEGORY_NAME_FOR_NODES = "CATEGORY_NAME_FOR_NODES";

    @InjectView(R.id.list) ListView listView;
    @InjectView(R.id.header_title) TextView headerTitle;
    @InjectView(R.id.header_search) ImageView headerSearch;
    @InjectView(R.id.search_frame) FrameLayout searchFrame;

    private ThingsToDoList_Bookmarks_CursorAdapter adapter;
    private SearchFragment searchFragment;

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.generic_list_screen);
        ButterKnife.inject(this);

        String currentCategory = getIntent().getStringExtra(CATEGORY_NAME_FOR_NODES);
        int pickedID = getIntent().getIntExtra(NavigationManagerActivity.ACTIVITY_TO_START_ID, -1);

        if (SentosaUtils.isValidString(currentCategory)) {
            headerTitle.setText(currentCategory);
        }

        headerSearch.setVisibility(View.VISIBLE);
        searchFragment = (SearchFragment) getSupportFragmentManager().findFragmentById(R.id.search_fragment);

        SentosaUtils.addListViewFooter(this, listView, false);
        setListViewData(currentCategory);

        if (pickedID != -1) {
            moveToDetailItem(pickedID);
        }
    }

    @OnItemClick(R.id.list)
    public void clickThings(View view, int position) {
        int node_id = adapter.getNodeId(position);
        moveToDetailItem(node_id);
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

    private void setListViewData(String currentCategory) {
        Cursor childCursor = getContentResolver().query(ContentURIs.SENTOSA_URI, null, Queries.NODE_DETAILS_FOR_THINGS_TO_DO_TYPE(currentCategory), null, null);
        Cursor zoneCursor = getContentResolver().query(ContentURIs.SENTOSA_URI, null, Queries.ZONES_GROUP_FOR_CATEGORY_QUERY(currentCategory), null, null);
        adapter = new ThingsToDoList_Bookmarks_CursorAdapter(this, childCursor, zoneCursor, 0);
        listView.setAdapter(adapter);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == MapActivity.ROUTE_DESTINATION_REQUEST_CODE && resultCode == RESULT_OK) {
            setResult(RESULT_OK, data);
            finish();
        }
    }

    private void moveToDetailItem(int id) {
        Intent intent = new Intent(ThingsToDoCategoryListActivity.this, NodeDetailActivity.class);
        intent.putExtra(Const.NODE_ID, id);
        intent.putExtra(SOURCE_ACTIVITY, ACTIVITY_THINGS_TO_DO);
        intent.putExtra(Const.FlurryStrings.FlurryEventName, Const.FlurryStrings.LocationDetailsSourceCategoryList);
        startActivityForResult(intent, MapActivity.ROUTE_DESTINATION_REQUEST_CODE);
    }
}