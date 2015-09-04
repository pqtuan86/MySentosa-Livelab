package sg.edu.smu.livelabs.integration.promotion;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.List;

import sg.edu.smu.livelabs.integration.model.Promotion;
import sg.edu.smu.livelabs.integration.R;

/**
 * This is the adapter class to handle the list of promotion for ListView.
 * Created by Le Gia Hai on 18/5/2015.
 *  Edited by John on 1 July 2015
 */
public class PromotionItemAdapter extends ArrayAdapter<Promotion> {
    private List<Promotion> promotions;

    public PromotionItemAdapter(Context context) {
        super(context, 0);
    }

    public void promotionsUpdated(List<Promotion> promotions) {
        this.promotions = promotions;
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return promotions != null ? promotions.size() : 0;
    }

    @Override
    public Promotion getItem(int position) {
        return promotions.get(position);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater inflater = LayoutInflater.from(getContext());
            convertView = inflater.inflate(R.layout.promotion_item, parent, false);
        }

        Typeface tfSemiBold = Typeface.createFromAsset(getContext().getAssets(), "font/MyriadPro-Semibold.otf");
        Typeface tfRegular = Typeface.createFromAsset(getContext().getAssets(), "font/MyriadPro-Regular.otf");

        final ImageView logoView = (ImageView) convertView.findViewById(R.id.logo_view);
        TextView titleView = (TextView) convertView.findViewById(R.id.title_txt);
        //TextView detailsVIew = (TextView) convertView.findViewById(R.id.details_txt);

        titleView.setTypeface(tfSemiBold);
        //detailsVIew.setTypeface(tfRegular);
        Promotion promotion = getItem(position);



        Picasso.with(getContext()).load(promotion.getImage().toString()).memoryPolicy(MemoryPolicy.NO_CACHE).into(logoView, new com.squareup.picasso.Callback() {
            @Override
            public void onSuccess() {
                Drawable drawable = logoView.getDrawable();
                double bitmapWidth = drawable.getIntrinsicWidth(); //this is the bitmap's width
                double bitmapHeight = drawable.getIntrinsicHeight(); //this is the bitmap's height

                int width = logoView.getMeasuredWidth();
                int height = (int) Math.ceil(((bitmapHeight / bitmapWidth) * (double) width));
                logoView.getLayoutParams().height = height;
            }

            @Override
            public void onError() {

            }
        });


        titleView.setText(promotion.getTitle());
        //detailsVIew.setText(promotion.getTitle());
        return convertView;
    }

}
