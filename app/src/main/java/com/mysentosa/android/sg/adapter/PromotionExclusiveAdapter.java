package com.mysentosa.android.sg.adapter;

import java.util.ArrayList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.mysentosa.android.sg.BaseActivity;
import com.mysentosa.android.sg.R;
import com.mysentosa.android.sg.SentosaApplication;
import com.mysentosa.android.sg.imageloader.ImageFetcher;
import com.mysentosa.android.sg.models.Promotion;
import com.mysentosa.android.sg.utils.HttpHelper;
import com.mysentosa.android.sg.utils.SentosaUtils;

public class PromotionExclusiveAdapter extends BaseAdapter {
    private Context mContext;
    private LayoutInflater mLayoutInflater;
    private ImageFetcher mImageWorker;
    private ArrayList<Promotion> promotionList;

    public PromotionExclusiveAdapter(Context context, ArrayList<Promotion> promotionList) {
        this.mContext = context;
        this.promotionList = promotionList;
        this.mLayoutInflater = LayoutInflater.from(mContext);
        mImageWorker = ((BaseActivity) context).mImageFetcher;
    }

    public class ViewHolder {
    public TextView tvDescription;
    public ImageView ivThumbnail, ivClaimed;
    public long id;
    }

    @Override
    public int getCount() {
        return promotionList.size();
    }

    @Override
    public Object getItem(int position) {
        return promotionList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = mLayoutInflater.inflate(R.layout.item_event_promo_list_large, null);
            holder = new ViewHolder();
            holder.tvDescription = (TextView) convertView.findViewById(R.id.tv_event_promo_item_large_description);
            holder.ivThumbnail = (ImageView) convertView.findViewById(R.id.iv_event_promo_item_large_thumbnail);
            holder.ivClaimed = (ImageView) convertView.findViewById(R.id.iv_claimed);
            convertView.setTag(holder);
        }
        holder = (ViewHolder) convertView.getTag();
        String imageUrl = null;
        String description = promotionList.get(position).getTitle();
        holder.tvDescription.setText(description);

        imageUrl = promotionList.get(position).getImageURL();
        holder.ivThumbnail.setImageResource(R.drawable.stub_thumb);
        if (SentosaUtils.isValidString(imageUrl))
            mImageWorker.loadImage(HttpHelper.BASE_HOST + imageUrl, holder.ivThumbnail, null, R.drawable.stub_thumb,
                    true, null);
        holder.id = promotionList.get(position).getId();
        holder.ivClaimed.setVisibility(ImageView.INVISIBLE);
        if (SentosaApplication.mClaimedDeals != null) {
            for (Promotion promotion : SentosaApplication.mClaimedDeals) {
                if (promotion.getId() == promotionList.get(position).getId()) {
                    holder.ivClaimed.setVisibility(ImageView.VISIBLE);
                    break;
                }
            }
        }
        
        return convertView;
    }
}
