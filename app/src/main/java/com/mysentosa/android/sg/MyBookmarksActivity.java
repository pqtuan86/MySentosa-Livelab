package com.mysentosa.android.sg;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.mysentosa.android.sg.adapter.ThingsToDoList_Bookmarks_CursorAdapter;
import com.mysentosa.android.sg.fragments.SearchFragment;
import com.mysentosa.android.sg.provider.utils.SentosaDatabaseStructure.ContentURIs;
import com.mysentosa.android.sg.provider.utils.SentosaDatabaseStructure.Queries;
import com.mysentosa.android.sg.utils.Const;
import com.mysentosa.android.sg.utils.SentosaUtils;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import butterknife.OnItemClick;

public class MyBookmarksActivity extends BaseActivity implements LoaderCallbacks<Cursor> {

    public static final String TAG = MyBookmarksActivity.class.getSimpleName();

    @InjectView(R.id.header_title) TextView headerTitle;
    @InjectView(R.id.header_search) ImageView headerSearch;
    @InjectView(R.id.search_frame) FrameLayout searchFrame;
    @InjectView(R.id.list) ListView list;

    private ThingsToDoList_Bookmarks_CursorAdapter adapter = null;
    private Cursor childCursor = null, zoneCursor = null;
    private SearchFragment searchFragment;

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.generic_list_screen);
        ButterKnife.inject(this);

        initializeViews();
    }

    private void initializeViews() {
        headerTitle.setText(R.string.my_bookmarks);
        headerSearch.setVisibility(View.VISIBLE);
        searchFragment = (SearchFragment) getSupportFragmentManager().findFragmentById(R.id.search_fragment);

        adapter = new ThingsToDoList_Bookmarks_CursorAdapter(this, null, null, 0);

        TextView emptyText = new TextView(this);
        emptyText.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        emptyText.setText("You have not bookmarked any pages");
        emptyText.setGravity(Gravity.CENTER);
        emptyText.setTextSize(18);
        emptyText.setTypeface(null, Typeface.BOLD);

        ((ViewGroup) list.getRootView()).addView(emptyText);
        list.setEmptyView(emptyText);

        SentosaUtils.addListViewFooter(this, list, false);

        list.setAdapter(adapter);

        getSupportLoaderManager().initLoader(0, null, this);
    }

    @OnItemClick(R.id.list)
    public void pickBookmark(View view, int position) {
        Intent intent = new Intent(MyBookmarksActivity.this, NodeDetailActivity.class);
        intent.putExtra(Const.NODE_ID, (Integer) view.getTag());
        intent.putExtra(Const.FlurryStrings.FlurryEventName, Const.FlurryStrings.LocationDetailsSourceMyBookmarks);
        intent.putExtra(NodeDetailActivity.SOURCE_ACTIVITY, NodeDetailActivity.ACTIVITY_MYBOOKMARKS);
        startActivity(intent);
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
    public Loader<Cursor> onCreateLoader(int arg0, Bundle arg1) {
        return new CursorLoader(this, ContentURIs.NODE_DETAILS_URI, null, Queries.MYBOOKMARKS_QUERY, null, "MANUAL");
    }

    @Override
    public void onLoadFinished(Loader<Cursor> arg0, Cursor cursor) {
        childCursor = cursor;
        zoneCursor = getContentResolver().query(ContentURIs.SENTOSA_URI, null, Queries.ZONES_GROUP_FOR_MYBOOKMARKS_QUERY(), null, null);
        adapter.swapCursor(childCursor, zoneCursor);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> arg0) {
        adapter.swapCursor(null, null);
    }
}