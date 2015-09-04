package com.mysentosa.android.sg;

import java.util.ArrayList;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.mysentosa.android.sg.models.TicketDetailItem;
import com.mysentosa.android.sg.provider.utils.JSONParseUtil;
import com.mysentosa.android.sg.provider.utils.SentosaDatabaseStructure.CartData;
import com.mysentosa.android.sg.provider.utils.SentosaDatabaseStructure.ContentURIs;
import com.mysentosa.android.sg.provider.utils.SentosaDatabaseStructure.Queries;
import com.mysentosa.android.sg.utils.Const;
import com.mysentosa.android.sg.utils.LogHelper;
import com.mysentosa.android.sg.utils.SentosaUtils;

public class TicketAddCartActivity extends Activity {

	private TextView mTotalSum, mTextHeading;
	private Button btnAddCart;
	int pos, TicketsCode;
	float TotalPrice = 0;
	private JSONParseUtil jsonParser;
	private ContentResolver mResolver;
	ListView list;
	TicketAddCartAdapter adapter;
	public ArrayList<TicketDetailItem> mTempArray = new ArrayList<TicketDetailItem>();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.tickets_add_cart);
		mResolver = getContentResolver();
		jsonParser = new JSONParseUtil(this);
		pos = getIntent().getIntExtra("Position", 0);
		TicketsCode = getIntent().getIntExtra("TicketsCode", 0);

		initializeViews();

		final Handler mHandler = new Handler();
		new Thread(new Runnable() {
			@Override
			public void run() {
				operationData();
				mHandler.post(new Runnable() {
					@Override
					public void run() {
						fillData();
					}
				});
			}
		}).start();

	}

	private void initializeViews() {

		((TextView) findViewById(R.id.header_title)).setText("Add To Cart");

		mTextHeading = (TextView) findViewById(R.id.tv_title);
		mTextHeading.setText(Const.mTicketsItems.get(pos).getName());

		btnAddCart = (Button) findViewById(R.id.btn_add_cart);
		btnAddCart
				.setTypeface(((SentosaApplication) this.getApplication()).myridTypeFace);
		btnAddCart.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				saveLocally();
				setResult(RESULT_OK);
				finish();
			}
		});

		list = (ListView) findViewById(R.id.lv_cartitem);
		adapter = new TicketAddCartAdapter(this);
		list.setAdapter(adapter);

		mTotalSum = (TextView) findViewById(R.id.tv_total_count);
	}

	private void operationData() {
		for (TicketDetailItem item : Const.mTicketsdetailItems) {
			if (item.getQuantity() != 0) {
				mTempArray.add(item);
				TotalPrice += item.getQuantity() * item.getPrice();
			}
		}
	}

	private void fillData() {
		adapter.notifyDataSetChanged();
		mTotalSum.setText(SentosaUtils.getFormatValues(TotalPrice));
	}

	private void saveLocally() {

		int id = Const.mTicketsItems.get(pos).getId();
		
		for (int i = 0; i < mTempArray.size(); i++) {			
			ContentValues content;
			TicketDetailItem item = mTempArray.get(i);
//			String desc = item.getDescription();
			String desc = item.getName() + " (" + item.getContent() + ")"
					+ "";
			LogHelper.i("tructran", desc);
			String selection = CartData.CART_ID_COL + " = '"
					+ String.valueOf(id) + "' AND " + CartData.CART_TYPE_COL
					+ " = '" + String.valueOf(TicketsCode) + "' AND "
					+ CartData.CART_DETAIL_ID_COL + " = '"
					+ String.valueOf(item.getId()) + "'";
			String[] selectionArgs = null;

			Cursor cursor = mResolver.query(ContentURIs.CART_URI, null, Queries
					.IS_SHOPPING_CART_EXIST_QUERY(id, TicketsCode, item.getId()), selectionArgs, Const.MANUAL);
			if (cursor.getCount() > 0) {
				cursor.moveToPosition(0);

				int adVal = (int) item.getQuantity() + cursor.getInt(cursor.getColumnIndex(CartData.CART_QTY_COL));
				String info = (item.getContent() + " (" + item.getTicketType() + ")");
				content = jsonParser.addCartItem(
						Const.mTicketsItems.get(pos).getId(), 
						desc, 
						adVal, 
						item.getPrice(),
						(item.getPrice() * adVal), 
						TicketsCode,
						info, 
						item.getId(), 
						"");
				mResolver.update(ContentURIs.CART_URI, content, selection,selectionArgs);
			} else {
				String info = (item.getContent() + " (" + item.getTicketType() + ")");
				content = jsonParser.addCartItem(
						Const.mTicketsItems.get(pos).getId(), 
						desc, 
						(int) item.getQuantity(),
						item.getPrice(), 
						(item.getQuantity() * item.getPrice()),
						TicketsCode, 
						info,
						item.getId(), 
						"");
				mResolver.insert(ContentURIs.CART_URI, content);
			}
			cursor.close();
		}
	}

	// ADAPTER FOR ADD TO CART
	public class TicketAddCartAdapter extends BaseAdapter {

		private Activity activity;
		public LayoutInflater inflater = null;

		public TicketAddCartAdapter(Activity a) {
			activity = a;
			inflater = (LayoutInflater) activity
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		}

		public int getCount() {
			if (mTempArray == null) {
				return 0;
			}
			return mTempArray.size();
		}

		public Object getItem(int position) {
			return position;
		}

		public long getItemId(int position) {
			return position;
		}

		public class ViewHolder {
			public TextView txtTicketText, txtTicketPrice;
		}

		public View getView(final int position, View convertView,
				ViewGroup parent) {

			View vi = convertView;
			ViewHolder holder;
			if (convertView == null) {

				vi = inflater.inflate(R.layout.item_add_to_cart, null);
				holder = new ViewHolder();

				holder.txtTicketText = (TextView) vi.findViewById(R.id.tv_text);
				holder.txtTicketPrice = (TextView) vi
						.findViewById(R.id.tv_count);

				vi.setTag(holder);
			} else
				holder = (ViewHolder) vi.getTag();

			TicketDetailItem item = mTempArray.get(position);
			holder.txtTicketText.setText(
					SentosaUtils.getHeadingAllFormatValues(
							String.valueOf(
									item.getQuantity()), 
									item.getContent(), 
									item.getTicketType(), 
									item.getPrice()));

			holder.txtTicketPrice.setText(SentosaUtils.getFormatValues((int) item.getQuantity() * item.getPrice()));

			return vi;
		}
	}
}