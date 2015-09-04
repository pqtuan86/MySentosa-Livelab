package com.mysentosa.android.sg.adapter;

import java.util.ArrayList;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.TextView;

import com.mysentosa.android.sg.R;
import com.mysentosa.android.sg.provider.utils.SentosaDatabaseStructure.NodeDetailsData;
import com.mysentosa.android.sg.utils.LogHelper;
import com.mysentosa.android.sg.utils.SentosaUtils;

public class MapFilterCursorAdapter extends CursorAdapter {
	private final LayoutInflater mLayoutInflater;
	private final int mLayout = R.layout.item_map_filter;
	private Context mContext;
	private ArrayList<String> filteredCategory;
	private int categoryColIndex = -1;
	
	public MapFilterCursorAdapter(Context context, Cursor c, ArrayList<String> currentCategories) {
		super(context, c, 0);
		mLayoutInflater = (LayoutInflater)
		context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		mContext = context;
		filteredCategory = currentCategories;
	}
	
	public class ViewHolder {;
		public TextView tvTitle;
		public ImageView ivIcon;
		public CheckBox cbFilter;
	}


	@Override
	public void bindView(View view, Context context, Cursor cursor) {
		ViewHolder holder = (ViewHolder) view.getTag();
		String category_name = cursor.getString(categoryColIndex);
		holder.tvTitle.setText(category_name);
		int iconId = SentosaUtils.getResourceId(mContext, "icon_"+category_name.replace(" ", "").replace("'", "").toLowerCase());
		if(iconId == 0) iconId = R.drawable.stub_thumb;
		holder.ivIcon.setImageResource(iconId);
		holder.cbFilter.setTag(category_name);
		holder.cbFilter.setChecked(filteredCategory.contains(category_name));
	}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
		View convertView =  mLayoutInflater.inflate(mLayout, null);
		ViewHolder holder =new ViewHolder();
		convertView.findViewById(R.id.iv_right_arrow).setVisibility(View.GONE);
        holder.tvTitle=(TextView) convertView.findViewById(R.id.tv_category_list_title);
        holder.ivIcon=(ImageView) convertView.findViewById(R.id.iv_category_icon);                
        holder.cbFilter = (CheckBox) convertView.findViewById(R.id.cb_select_category);      
        holder.cbFilter.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				checkAction(buttonView,isChecked);
			}
		});
        convertView.setTag(holder);
        if(categoryColIndex==-1) categoryColIndex = cursor.getColumnIndex(NodeDetailsData.CATEGORY_COL);
        convertView.setOnClickListener(onItemClickListener);
		return convertView;
	}
	
	private OnClickListener onItemClickListener = new OnClickListener(){
		@Override
		public void onClick(View convertView) {
			LogHelper.d("test","test item clicked: ");
			ViewHolder holder = (ViewHolder) convertView.getTag();
			CompoundButton b = holder.cbFilter;
			b.setChecked(!b.isChecked());
		}
	};
	
	private void checkAction(CompoundButton buttonView, boolean isChecked) {
		String category = (String)buttonView.getTag();
		if(isChecked){
			if(!filteredCategory.contains(category))
			filteredCategory.add(category);
		}
		else {
			if(filteredCategory.contains(category)) {
				filteredCategory.remove(category);
			}
		}
	}
	
	public ArrayList<String> getFilteredCategory() {
		return filteredCategory;
	}
	
	public void setFilteredCategory(ArrayList<String> filteredCategory) {
		this.filteredCategory = filteredCategory;
	}
}
