package com.mysentosa.android.sg.adapter;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.mysentosa.android.sg.R;
import com.mysentosa.android.sg.provider.utils.SentosaDatabaseStructure.ContentURIs;
import com.mysentosa.android.sg.provider.utils.SentosaDatabaseStructure.Queries;

public class DirectionsAutoCompleteAdapter extends CursorAdapter {
	// private StationDBAdapter dbAdapter = null;
	Context context;
	String args = "";

	public DirectionsAutoCompleteAdapter(Context context, Cursor c) {
		super(context, c);
		this.context = context;
		// dbAdapter = new StationDBAdapter(context);
		// dbAdapter.open();
	}

	@Override
	public void bindView(View view, Context context, Cursor cursor) {
		String item = createItem(cursor);
		TextView tv = (TextView) view.findViewById(R.id.text_view);
		view.setTag(item);
		if(args.length() > 0 && item != null && item.length() > 0){
			item = replaceString(item);
		}
		tv.setText(Html.fromHtml(item));
	}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
		final LayoutInflater inflater = LayoutInflater.from(context);
		final View view = inflater.inflate(
				R.layout.directions_autocomplete_row, parent, false);

//		String item = createItem(cursor);
//		view.setTag(item);
//		if(args.length() > 0 && item != null && item.length() > 0){
//			item = replaceString(item);
//		}
//		view.setText(Html.fromHtml(item));
		return view;
	}

	@Override
	public Cursor runQueryOnBackgroundThread(CharSequence constraint) {
		Cursor currentCursor = null;

		if (getFilterQueryProvider() != null) {
			return getFilterQueryProvider().runQuery(constraint);
		}

		
		String query = null;

		if (constraint != null) {
			args = constraint.toString();
		} else 
		{
			args = "";
		}
		
		if(args.trim().length() > 0) {
			query = Queries.DIRECTIONS_AUTOCOMPLETE_QUERY.replace("--ARGS--", args);
		} else {
			query = Queries.DIRECTIONS_AUTOCOMPLETE_DEFAULT_QUERY;
		}
		currentCursor = context.getContentResolver().query(
				ContentURIs.SENTOSA_URI, null,
				query, null,
				null);

		// = dbAdapter.getStationCursor(args);

		return currentCursor;
	}

	private String createItem(Cursor cursor) {
		String item = cursor.getString(1);
		return item;
	}
	
	private String replaceString(String item){
		try{
			item = item.replaceAll("(?i)" + args , "<b>" + "$0" +  "</b>");
		}catch(Exception e){
			item = item.replace(args, "<b>"+args+"</b>");
		}
		
		return item;
	}

	public void close() {
		// dbAdapter.close();
	}
}