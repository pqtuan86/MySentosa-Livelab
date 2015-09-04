package com.mysentosa.android.sg.fragments;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ListView;

import com.mysentosa.android.sg.EventsAndPromotionsActivity;
import com.mysentosa.android.sg.EventsAndPromotionsDetailActivity;
import com.mysentosa.android.sg.NodeDetailActivity;
import com.mysentosa.android.sg.R;
import com.mysentosa.android.sg.adapter.SimpleSearchCursorAdapter;
import com.mysentosa.android.sg.provider.utils.SentosaDatabaseStructure;
import com.mysentosa.android.sg.utils.Const;
import com.mysentosa.android.sg.utils.RepoTools;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnItemClick;

/**
 * Created by randiwaranugraha on 5/15/15.
 */
public class SearchFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    public static final String TAG = SearchFragment.class.getSimpleName();

    private static final int QUERY = 1;
    private static final String ORIGIN = "origin";
    private static final String LTITLE = "ltitle";

    public static SearchFragment newInstance() {
        SearchFragment searchFragment = new SearchFragment();
        return searchFragment;
    }

    @InjectView(R.id.search_input) EditText searchInput;
    @InjectView(R.id.search_result) ListView searchResult;

    private SimpleSearchCursorAdapter searchCursorAdapter;
    private LoaderManager loaderManager;
    private String searchTerm;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_search, container, false);
        ButterKnife.inject(this, view);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        searchInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String searchFilter = !TextUtils.isEmpty(s) ? s.toString() : null;
                searchTerm = searchFilter;
                searchCursorAdapter.setSearchTerm(searchTerm);
                loaderManager.restartLoader(QUERY, null, SearchFragment.this);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        searchCursorAdapter = new SimpleSearchCursorAdapter(getActivity().getApplicationContext());
        searchResult.setAdapter(searchCursorAdapter);

        loaderManager = getLoaderManager();
        loaderManager.initLoader(QUERY, null, this);
    }

    @OnItemClick(R.id.search_result)
    public void pickAResult(int position) {
        Cursor cursor = searchCursorAdapter.getCursor();
        if (cursor.moveToPosition(position)) {
            String origin = RepoTools.getString(cursor, ORIGIN);
            Intent intent = null;

            if(SentosaDatabaseStructure.TABLE_NODE_DETAILS.equals(origin)) {
                int id = RepoTools.getInt(cursor, SentosaDatabaseStructure.NodeDetailsData.NODE_ID_COL);
                intent = new Intent(getActivity(), NodeDetailActivity.class);
                intent.putExtra(Const.NODE_ID, id);
            } else {
                long id = RepoTools.getLong(cursor, SentosaDatabaseStructure.EventsPromotionsBase.ID_COL);
                intent = new Intent(getActivity(), EventsAndPromotionsDetailActivity.class);
                intent.putExtra(SentosaDatabaseStructure.EventsPromotionsBase.ID_COL, id);

                if(SentosaDatabaseStructure.TABLE_EVENTS.equals(origin)) {
                    intent.putExtra(EventsAndPromotionsDetailActivity.CURRENT_TYPE, EventsAndPromotionsDetailActivity.TYPE_EVENT);
                } else if(SentosaDatabaseStructure.TABLE_PROMOTIONS.equals(origin)) {
                    intent.putExtra(EventsAndPromotionsDetailActivity.CURRENT_TYPE, EventsAndPromotionsDetailActivity.TYPE_PROMOTION);
                }
            }

            startActivity(intent);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.reset(this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        if (QUERY == id) {
            String rawQuery = queryNodeDetails(searchTerm) + " UNION " + queryEvents(searchTerm) + " UNION " + queryPromotions(searchTerm);

            rawQuery += " ORDER BY " + LTITLE;
            return new CursorLoader(getActivity().getApplicationContext(), SentosaDatabaseStructure.ContentURIs.SENTOSA_URI, null, rawQuery, null, null);
        }
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if(QUERY == loader.getId()) {
            searchCursorAdapter.swapCursor(data);
            searchCursorAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        if(QUERY == loader.getId()) {
            searchCursorAdapter.swapCursor(null);
            searchCursorAdapter.notifyDataSetChanged();
        }
    }

    public void clearSearch() {
        searchTerm = null;
        searchInput.setText(null);
        searchCursorAdapter.swapCursor(null);
        searchCursorAdapter.notifyDataSetChanged();
    }

    private String queryNodeDetails(String searchTerm) {
        String selection = "SELECT " + SentosaDatabaseStructure.NodeDetailsData.ID_COL + ", "
                + SentosaDatabaseStructure.NodeDetailsData.NODE_ID_COL + ", "
                + SentosaDatabaseStructure.NodeDetailsData.TITLE_COL + ", "
                + SentosaDatabaseStructure.NodeDetailsData.DESCRIPTION_COL + ", "
                + "'" + SentosaDatabaseStructure.TABLE_NODE_DETAILS + "' AS " + ORIGIN + ", "
                + "LOWER(" + SentosaDatabaseStructure.NodeDetailsData.TITLE_COL + ") AS "+ LTITLE + " "
                + "FROM " + SentosaDatabaseStructure.TABLE_NODE_DETAILS;

        if(searchTerm != null) {
            selection += " WHERE " + SentosaDatabaseStructure.NodeDetailsData.TITLE_COL + " LIKE '%" + searchTerm + "%'";
        }

        return selection;
    }

    private String queryEvents(String searchTerm) {
        String selection = "SELECT " + SentosaDatabaseStructure.EventsPromotionsBase.ID_COL + ", "
                + SentosaDatabaseStructure.EventsPromotionsBase.ID_COL + ", "
                + SentosaDatabaseStructure.EventsPromotionsBase.TITLE_COL + ", "
                + SentosaDatabaseStructure.EventsPromotionsBase.DESCRIPTION_COL + ", "
                + "'" + SentosaDatabaseStructure.TABLE_EVENTS + "', "
                + "LOWER(" + SentosaDatabaseStructure.EventsPromotionsBase.TITLE_COL + ") "
                + "FROM " + SentosaDatabaseStructure.TABLE_EVENTS;

        if(searchTerm != null) {
            selection += " WHERE " + SentosaDatabaseStructure.EventsPromotionsBase.TITLE_COL + " LIKE '%" + searchTerm + "%'";
        }

        return selection;
    }

    private String queryPromotions(String searchTerm) {
        String selection = "SELECT " + SentosaDatabaseStructure.EventsPromotionsBase.ID_COL + ", "
                + SentosaDatabaseStructure.EventsPromotionsBase.ID_COL + ", "
                + SentosaDatabaseStructure.EventsPromotionsBase.TITLE_COL + ", "
                + SentosaDatabaseStructure.EventsPromotionsBase.DESCRIPTION_COL + ", "
                + "'" + SentosaDatabaseStructure.TABLE_PROMOTIONS + "', "
                + "LOWER(" + SentosaDatabaseStructure.EventsPromotionsBase.TITLE_COL + ") "
                + "FROM " + SentosaDatabaseStructure.TABLE_PROMOTIONS;

        if(searchTerm != null) {
            selection += " WHERE " + SentosaDatabaseStructure.EventsPromotionsBase.TITLE_COL + " LIKE '%" + searchTerm + "%'";
        }

        return selection;
    }
}