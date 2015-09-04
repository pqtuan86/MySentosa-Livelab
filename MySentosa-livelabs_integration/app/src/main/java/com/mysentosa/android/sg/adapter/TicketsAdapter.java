package com.mysentosa.android.sg.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TableLayout;
import android.widget.TextView;

import com.mysentosa.android.sg.R;
import com.mysentosa.android.sg.SentosaApplication;
import com.mysentosa.android.sg.imageloader.ImageFetcher;
import com.mysentosa.android.sg.utils.Const;
import com.mysentosa.android.sg.utils.HttpHelper;
import com.mysentosa.android.sg.utils.SentosaUtils;

public class TicketsAdapter extends BaseAdapter {
	private Context mContext;
	private LayoutInflater mLayoutInflater;
	// TicketItem mItems;
	private ImageFetcher mImageWorker;
	int codes;

	public TicketsAdapter(Context context, int code) {
		this.mContext = context;
		this.mLayoutInflater = LayoutInflater.from(mContext);
		this.codes = code;
		mImageWorker = ((SentosaApplication) context.getApplicationContext()).mImageFetcher;
	}

	@Override
	public int getCount() {
		if (codes != Const.PURCHASE_TICKET_TYPE_CODE)
			return Const.mTicketsItems.size();
		else
			return Const.mPurchaseItems.size();
	}

	static class ViewHolder {
		TextView tvTitle;
		TextView tvDate;
		ImageView ivItemimage;
		LinearLayout layoutIslander;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		final ViewHolder holder;

		if (convertView == null) {
			// convertView = mLayoutInflater.inflate(R.layout.item_tickets,
			// null);
//			convertView = mLayoutInflater.inflate(
//					R.layout.item_purchases_tickets, null);
		    convertView = mLayoutInflater.inflate(R.layout.item_purchases_tickets, parent, false);
			holder = new ViewHolder();
			holder.tvTitle = (TextView) convertView
					.findViewById(R.id.tv_items_name);
			holder.tvDate = (TextView) convertView
					.findViewById(R.id.tv_items_date);
			holder.ivItemimage = (ImageView)  convertView
					.findViewById(R.id.iv_item_img);
			holder.layoutIslander = (LinearLayout) convertView.findViewById(R.id.layout_title_exclusive);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}

		// mItems = Const.mTicketsItems.get(position);
		holder.tvTitle.setText(getName(position));
		if (codes == Const.PURCHASE_TICKET_TYPE_CODE) {
		    LayoutParams param = (LayoutParams) holder.tvTitle.getLayoutParams();
            param.weight = 0f;
            param.height = LayoutParams.WRAP_CONTENT;
            holder.tvTitle.setLayoutParams(param);
            
            convertView.findViewById(R.id.layout_text).getLayoutParams().height = LayoutParams.WRAP_CONTENT;
		} else {
		    LayoutParams param = (LayoutParams) holder.tvTitle.getLayoutParams();
		    param.weight = 1f;
		    param.height = 0;
		    holder.tvTitle.setLayoutParams(param);
		    
		    convertView.findViewById(R.id.layout_text).getLayoutParams().height = LayoutParams.MATCH_PARENT;
		}
		
		if (codes == Const.PURCHASE_TICKET_TYPE_CODE) {
			holder.ivItemimage.setVisibility(View.GONE);
			holder.tvDate.setText(Const.sdf.format(SentosaUtils.getDate(String
					.valueOf(Const.mPurchaseItems.get(position)
							.getReserveTime()))));
		}else{
		    String imageUrl = Const.mTicketsItems.get(position).getImage();
		    if (SentosaUtils.isValidString(imageUrl)) {
		        mImageWorker.loadImage(
	                    HttpHelper.BASE_HOST + imageUrl,
	                    holder.ivItemimage, null, R.drawable.stub_thumb, false, null);
		    }
			
			if (Const.mTicketsItems.get(position).isIslanderExclusive()) {
			    holder.layoutIslander.setVisibility(LinearLayout.VISIBLE);
			} else {
			    holder.layoutIslander.setVisibility(LinearLayout.GONE);
			}
		}
		
		
		return convertView;
	}

	@Override
	public Object getItem(int position) {
		return position;
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	private String getName(int pos) {
		if (codes != Const.PURCHASE_TICKET_TYPE_CODE) {
			return Const.mTicketsItems.get(pos).getName() == null ? "Test"
					: Const.mTicketsItems.get(pos).getName();
		} else {
			StringBuilder sb = new StringBuilder();
			for (int i = 0; i < Const.mPurchaseItems.get(pos).getPurchases()
					.size(); i++) {
				for (int j = 0; j < Const.mPurchaseItems.get(pos)
						.getPurchases().get(i).getGroup().size(); j++) {
					sb.append(Const.mPurchaseItems.get(pos).getPurchases()
							.get(i).getGroup().get(j).getName()+"\n\n");
				}
			}
			return sb.toString().trim();
		}
	}
}
