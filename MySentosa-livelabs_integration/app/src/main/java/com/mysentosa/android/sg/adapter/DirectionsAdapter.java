package com.mysentosa.android.sg.adapter;

import java.text.DecimalFormat;
import java.util.ArrayList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.mysentosa.android.sg.R;
import com.mysentosa.android.sg.map.models.Direction;
import com.mysentosa.android.sg.map.models.Edge;


public class DirectionsAdapter extends BaseAdapter{
	private ArrayList<Direction> directions;

	DecimalFormat df;
	private LayoutInflater mInflater;
	
	public DirectionsAdapter(Context context,
			ArrayList<Direction> directions) {
		super();
		this.directions = directions;
		mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE); 
		df = new DecimalFormat("#.##");
	}

	

	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder;
		if (convertView == null) {
			convertView = mInflater.inflate(R.layout.directions_row, null);
			holder = new ViewHolder();
			holder.direction_title = (TextView) convertView.findViewById(R.id.direction_title);
			holder.direction_description = (TextView) convertView.findViewById(R.id.direction_description);
		    holder.direction_time = (TextView) convertView.findViewById(R.id.direction_time);
		    holder.direction_circle = (TextView) convertView.findViewById(R.id.direction_circle);
		    holder.direction_line = (ImageView) convertView.findViewById(R.id.direction_line);

			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		final ViewHolder holderFinal = holder;
		final Direction direction = directions.get(position);

		
		if(position == (getCount()-1)) { // last element
			holderFinal.direction_title.setText(direction.getTo());
		} else {
			holderFinal.direction_title.setText(direction.getFrom());
		}
		holderFinal.direction_circle.setText((position+1)+"");
		holder.direction_title.setTextColor(direction.getEdge().getEdgeDirectionColor());
		holderFinal.direction_description.setText(direction.getMessage());
		
		if(position != (getCount()-1) ) { // NOT last element and atleast 1 meter distance
			// converting speed from per hour to per min
			double speedPerMin = direction.getEdge().getSpeed() / 60; 
			// time estimate is : our distance / speed of traveling in mins. Then add waiting times (before bording to bus)
			// right now not considering intermediate waiting times
			double time = (direction.getDistance() / speedPerMin) + direction.getEdge().getWaitingTime();
			
			String distance, estimatedTime;
			distance = ((int)Math.ceil(direction.getDistance()))+" meters";
			if(direction.getStops() > 0){
				distance = (direction.getStops())+" stops";
			}
			estimatedTime = (int)Math.ceil(time)+" mins";
			
			holderFinal.direction_time.setText(distance +" | "+ estimatedTime);
			
			if( direction.getDistance() <= 1){
				holderFinal.direction_time.setText("less then a minute...");
			}
			holderFinal.direction_time.setVisibility(View.VISIBLE);
		} else {
			holderFinal.direction_time.setVisibility(View.GONE);
		}
		
		switch(direction.getEdge().getEdgeType()){
		case Edge.TYPE_BUS1:
			holder.direction_circle.setBackgroundResource(R.drawable.directions_bus1_circle);
			holder.direction_line.setImageResource(R.drawable.directions_bus1_line);
			break;
		case Edge.TYPE_BUS2:
			holder.direction_circle.setBackgroundResource(R.drawable.directions_bus2_circle);
			holder.direction_line.setImageResource(R.drawable.directions_bus2_line);
			break;
		case Edge.TYPE_BUS3:
			holder.direction_circle.setBackgroundResource(R.drawable.directions_bus3_circle);
			holder.direction_line.setImageResource(R.drawable.directions_bus3_line);
			break;
		case Edge.TYPE_TRAIN:
			holder.direction_circle.setBackgroundResource(R.drawable.directions_train_circle);
			holder.direction_line.setImageResource(R.drawable.directions_train_line);
			break;
		case Edge.TYPE_TRAM1:
			holder.direction_circle.setBackgroundResource(R.drawable.directions_tram1_circle);
			holder.direction_line.setImageResource(R.drawable.directions_tram1_line);
			break;
		case Edge.TYPE_TRAM2:
			holder.direction_circle.setBackgroundResource(R.drawable.directions_tram2_circle);
			holder.direction_line.setImageResource(R.drawable.directions_tram2_line);
			break;
		case Edge.TYPE_WALK:
			holder.direction_circle.setBackgroundResource(R.drawable.directions_walk_circle);
			holder.direction_line.setImageResource(R.drawable.directions_walk_line);
			break;
		}
		return convertView;
	}
	
	@Override
	public int getCount() {
		return directions.size();
	}



	@Override
	public Object getItem(int position) { 
		// TODO Auto-generated method stub
		return null;
	}



	@Override
	public long getItemId(int position) {
		return position;
	}

	static class ViewHolder {
		TextView direction_title;
		TextView direction_description;
		TextView direction_time;
		TextView direction_circle;
		ImageView direction_line;
	}
}
