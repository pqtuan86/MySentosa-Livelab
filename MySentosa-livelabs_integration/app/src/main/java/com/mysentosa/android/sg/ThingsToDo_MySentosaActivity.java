package com.mysentosa.android.sg;


import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.flurry.android.FlurryAgent;
import com.google.analytics.tracking.android.EasyTracker;
import com.google.analytics.tracking.android.Fields;
import com.google.analytics.tracking.android.MapBuilder;
import com.mysentosa.android.sg.adapter.ThingsToDo_MySentosa_ArrayAdapter;
import com.mysentosa.android.sg.adapter.ThingsToDo_MySentosa_ArrayAdapter.ThingsToDo_MySentosa_ListItem;
import com.mysentosa.android.sg.asynctask.GetEventsPromotionsAsyncTask;
import com.mysentosa.android.sg.fragments.SearchFragment;
import com.mysentosa.android.sg.provider.utils.SentosaDatabaseStructure.ContentURIs;
import com.mysentosa.android.sg.provider.utils.SentosaDatabaseStructure.Queries;
import com.mysentosa.android.sg.utils.Const;
import com.mysentosa.android.sg.utils.Const.FlurryStrings;
import com.mysentosa.android.sg.utils.SentosaUtils;

import java.util.Arrays;
import java.util.HashMap;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import butterknife.OnItemClick;

public class ThingsToDo_MySentosaActivity extends BaseActivity {

    public final static String TAG = ThingsToDo_MySentosaActivity.class.getSimpleName();

    public final static String CURRENT_TYPE = "CURRENT_TYPE";
    public final static int TYPE_THINGSTODO = 0;
    public final static int TYPE_MYSENTOSA = 1;

    @InjectView(R.id.list) ListView listView;
    @InjectView(R.id.header_title) TextView headerTitle;
    @InjectView(R.id.header_search) ImageView headerSearch;
    @InjectView(R.id.header_bookmark) ImageView headerBookmark;
    @InjectView(R.id.search_frame) FrameLayout searchFrame;

    private ThingsToDo_MySentosa_ArrayAdapter adapter;
    private boolean isThingsToDo;
    private SearchFragment searchFragment;

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.generic_list_screen);
        ButterKnife.inject(this);

        initializeViews();
    }

    private void initializeViews() {
        int currentType = getIntent().getIntExtra(CURRENT_TYPE, TYPE_THINGSTODO);
        isThingsToDo = currentType == TYPE_THINGSTODO;

        headerTitle.setText(isThingsToDo ? R.string.things_to_do : R.string.my_sentosa);
        headerSearch.setVisibility(View.VISIBLE);
        searchFragment = (SearchFragment) getSupportFragmentManager().findFragmentById(R.id.search_fragment);

        SentosaUtils.addListViewFooter(this, listView, false);

        if (isThingsToDo) {
            headerBookmark.setVisibility(View.VISIBLE);

            EasyTracker easyTracker = EasyTracker.getInstance(this);
            easyTracker.set(Fields.SCREEN_NAME, Const.GAStrings.THINGS_TO_DO);
            easyTracker.send(MapBuilder
                            .createAppView()
                            .build()
            );

            Cursor c = getContentResolver().query(ContentURIs.SENTOSA_URI, null, Queries.THINGS_TO_DO_LIST_QUERY, null, null);
            adapter = new ThingsToDo_MySentosa_ArrayAdapter(this, 0, ThingsToDo_MySentosa_ListItem.getThingsToDoListFromArrays(c, ThingsToDo_MySentosaActivity.this.getResources(), getApplication().getPackageName()));
        } else {
            String[] titles = {getString(R.string.bookmarks), getString(R.string.profile_settings)};
            String[] tags = {MyBookmarksActivity.class.getName(), ProfileAndSettingsActivity.class.getName()};
            Integer[] imgResIds = {R.drawable.mysentosa_icon_bookmarks, R.drawable.mysentosa_icon_profileandsettings};
            adapter = new ThingsToDo_MySentosa_ArrayAdapter(this, 0, ThingsToDo_MySentosa_ListItem.getMySentosaListFromArrays(Arrays.asList(titles), Arrays.asList(tags), Arrays.asList(imgResIds)));
        }

        listView.setAdapter(adapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        new GetEventsPromotionsAsyncTask(this).execute();
    }

    @OnClick(R.id.header_bookmark)
    public void openBookmarks() {
        startActivity(new Intent(ThingsToDo_MySentosaActivity.this, MyBookmarksActivity.class));
    }

    @OnItemClick(R.id.list)
    public void clickThings(View view, int position) {
        if (isThingsToDo) {
            HashMap<String, String> params = new HashMap<>();

            String tag = (String) view.getTag();
            params.put("ItemName", tag);
            FlurryAgent.logEvent(FlurryStrings.ItemInThingsToDo, params);

            if (tag.equals("Events")) {
                Intent intent = new Intent(ThingsToDo_MySentosaActivity.this, EventsAndPromotionsActivity.class);
                intent.putExtra(EventsAndPromotionsActivity.CURRENT_TYPE, EventsAndPromotionsActivity.TYPE_EVENT);
                startActivity(intent);
            } else {
                Intent intent = new Intent(ThingsToDo_MySentosaActivity.this, ThingsToDoCategoryListActivity.class);
                intent.putExtra(ThingsToDoCategoryListActivity.CATEGORY_NAME_FOR_NODES, tag);
                startActivity(intent);
            }
        } else {
            String className = (String) view.getTag();
            if (className.equals(MyBookmarksActivity.class.getName()))
                FlurryAgent.logEvent(FlurryStrings.ShowBookmarksInMySentosa);
            if (className.equals(ProfileAndSettingsActivity.class.getName()))
                FlurryAgent.logEvent(FlurryStrings.ShowProfileAndSettingsInMySentosa);
            if (!className.equals("")) {
                Intent intent = new Intent();
                intent.setClassName(ThingsToDo_MySentosaActivity.this, className);
                startActivity(intent);
            }
        }
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
}