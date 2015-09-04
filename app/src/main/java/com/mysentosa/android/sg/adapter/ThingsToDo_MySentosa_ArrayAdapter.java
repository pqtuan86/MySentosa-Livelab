package com.mysentosa.android.sg.adapter;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.mysentosa.android.sg.R;
import com.mysentosa.android.sg.adapter.ThingsToDo_MySentosa_ArrayAdapter.ThingsToDo_MySentosa_ListItem;
import com.mysentosa.android.sg.provider.SentosaContentProvider;

public class ThingsToDo_MySentosa_ArrayAdapter extends ArrayAdapter<ThingsToDo_MySentosa_ListItem> {

	private ArrayList<ThingsToDo_MySentosa_ListItem> items;
	private Context context;

	public ThingsToDo_MySentosa_ArrayAdapter(Context context, int textViewResourceId, ArrayList<ThingsToDo_MySentosa_ListItem> items) {
		super(context, textViewResourceId, items);
		this.context = context;
		this.items = items;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View v = convertView;
		if (v == null) {
			v = ((LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.item_thingstodo_mysentosa, null);
		}
		ThingsToDo_MySentosa_ListItem item = items.get(position);
		if (item != null) {
			TextView title = (TextView) v.findViewById(R.id.tv_title);
			title.setText(item.getTite());
			ImageView img = (ImageView) v.findViewById(R.id.iv_icon);
			img.setImageResource(item.getImgResId());
			v.setTag(item.getTag());
		}
		return v;
	}


	public static final class ThingsToDo_MySentosa_ListItem {

		private String title;
		private String tag;
		private int imgResId;
		
		public ThingsToDo_MySentosa_ListItem(String title, int imgResId, String tag) {
			this.title = title;
			this.imgResId = imgResId;
			this.tag = tag;
		}

		public int getImgResId() {
			return imgResId;
		}
		public String getTite() {
			return this.title;
		}
		public String getTag() {
			return this.tag;
		}
		
		public static ArrayList<ThingsToDo_MySentosa_ListItem> getMySentosaListFromArrays(List<String> titles, List<String> tags, List<Integer> imgResIds) {
			ArrayList<ThingsToDo_MySentosa_ListItem> items = new ArrayList<ThingsToDo_MySentosa_ListItem>();
			for(int i=0;i<titles.size();i++) {
				String tag = tags==null?titles.get(i):tags.get(i);
				items.add(new ThingsToDo_MySentosa_ListItem(titles.get(i),imgResIds.get(i), tag));
			}
			return items;
		}
		public static ArrayList<ThingsToDo_MySentosa_ListItem> getThingsToDoListFromArrays(Cursor c,Resources r, String packageName) {
			ArrayList<ThingsToDo_MySentosa_ListItem> items = new ArrayList<ThingsToDo_MySentosa_ListItem>();
			items.add(new ThingsToDo_MySentosa_ListItem("Events",R.drawable.thingstodo_icon_events, "Events"));
			if(c!=null && c.moveToFirst()){ 
				int name_col = c.getColumnIndex(SentosaContentProvider.ThingsToDoData.NAME_COL);
				int icon_id_col = c.getColumnIndex(SentosaContentProvider.ThingsToDoData.ICON_ID_COL);

				while(!c.isAfterLast()){
					String title = c.getString(name_col);
					String drawableName = c.getString(icon_id_col);
					if(!drawableName.startsWith("thingstodo_icon_")) drawableName = "thingstodo_icon_"+drawableName;
					int drawableId = r.getIdentifier(drawableName, "drawable", packageName);
					items.add(new ThingsToDo_MySentosa_ListItem(title,drawableId, title));
					c.moveToNext();
				}
			}
			return items;
		}
	}
}