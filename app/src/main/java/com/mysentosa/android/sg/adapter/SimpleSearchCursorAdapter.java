package com.mysentosa.android.sg.adapter;

import android.content.Context;
import android.database.Cursor;
import android.text.SpannableString;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.mysentosa.android.sg.R;
import com.mysentosa.android.sg.provider.utils.SentosaDatabaseStructure;
import com.mysentosa.android.sg.utils.RepoTools;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * Created by randiwaranugraha on 5/13/15.
 */
public class SimpleSearchCursorAdapter extends BaseCursorAdapter {

    public static final String TAG = SimpleSearchCursorAdapter.class.getSimpleName();

    public SimpleSearchCursorAdapter(Context context) {
        super(context);
    }

    @Override
    protected View createNewView(LayoutInflater inflater, Cursor cursor, ViewGroup parent) {
        View view = inflater.inflate(R.layout.list_item_simple_search, parent, false);

        ViewHolder holder = new ViewHolder(view);
        view.setTag(holder);

        return view;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        ViewHolder holder = (ViewHolder) view.getTag();

        String title = RepoTools.getString(cursor, SentosaDatabaseStructure.NodeDetailsData.TITLE_COL);
        String description = RepoTools.getString(cursor, SentosaDatabaseStructure.NodeDetailsData.DESCRIPTION_COL);

        int startIndex = indexOfSearchQuery(title);
        if(startIndex == -1) {
            holder.title.setText(title);
        } else {
            SpannableString spannableString = new SpannableString(title);
            spannableString.setSpan(getSearchSpan(), startIndex, startIndex + getSearchTermLength(), 0);
            holder.title.setText(spannableString);
        }

        if(description != null && description.length() > 0 && !description.equals("null")) {
            holder.description.setVisibility(View.VISIBLE);
            holder.description.setText(description);
        } else {
            holder.description.setVisibility(View.GONE);
        }
    }

    static class ViewHolder {
        @InjectView(R.id.title) TextView title;
        @InjectView(R.id.description) TextView description;

        public ViewHolder(View view) {
            ButterKnife.inject(this, view);
        }
    }
}