package com.mysentosa.android.sg;

import java.text.DecimalFormat;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.mysentosa.android.sg.provider.utils.JSONParseUtil;
import com.mysentosa.android.sg.provider.utils.SentosaDatabaseStructure.CartData;
import com.mysentosa.android.sg.provider.utils.SentosaDatabaseStructure.ContentURIs;
import com.mysentosa.android.sg.provider.utils.SentosaDatabaseStructure.Queries;
import com.mysentosa.android.sg.utils.Const;
import com.mysentosa.android.sg.utils.SentosaUtils;

public class TicketEventAddCartActivity extends Activity {

	private TextView mTextHeading, mTxtDate, mTotalSum;
	private Button btnAddCart;
	int pos;
	float TotalPrice;
	DecimalFormat df = new DecimalFormat("#.##");
	TableLayout table;
	String EventId, EventDate, EventName;
	int ItemEventID;
	ContentResolver mResolver;
	private JSONParseUtil jsonParser;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.tickets_event_add_cart);

		mResolver = getContentResolver();
		jsonParser = new JSONParseUtil(this);

		EventId = getIntent().getStringExtra("EventId");
		EventDate = getIntent().getStringExtra("EventDate");
		EventName = getIntent().getStringExtra("EventName");

		ItemEventID = getIntent().getIntExtra("ItemEventId", 0);

		initializeViews();
	}

	private void initializeViews() {

		((TextView) findViewById(R.id.header_title)).setText("Add To Cart");

		mTextHeading = (TextView) findViewById(R.id.tv_title);
		mTextHeading.setText(EventName);

		mTxtDate = (TextView) findViewById(R.id.tv_date);
		mTxtDate.setText(getString(R.string.ticket_event_date, EventDate));

		mTotalSum = (TextView) findViewById(R.id.tv_total_count);
		table = (TableLayout) findViewById(R.id.tb_table);

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

		TotalPrice = addTables();
		fillData();
	}

	private void fillData() {
		mTotalSum.setText(getString(R.string.ticket_cart_total_value,
				SentosaUtils.DoFormat(df.format(TotalPrice))));
	}

	private float addTables() {
		float addedPrices = 0;
		table.removeAllViews();
		LayoutInflater inflater = getLayoutInflater();
		for (int i = 0; i < Const.mEventCartItems.size(); i++) {
			TableRow row = (TableRow) inflater.inflate(
					R.layout.item_event_cart, table, false);
			row.setPadding(0, 10, 0, 10);

			TextView name = (TextView) row.findViewById(R.id.tv_items);
			name.setText(getString(R.string.ticket_cart_event,
					Const.mEventCartItems.get(i).getQty(),
					Const.mEventCartItems.get(i).getDesc(), SentosaUtils
							.DoFormat(String.valueOf(Const.mEventCartItems.get(
									i).getPrice()))));

			float multiAdd = Const.mEventCartItems.get(i).getPrice()
					* Const.mEventCartItems.get(i).getQty();
			addedPrices += multiAdd;

			TextView price = (TextView) row.findViewById(R.id.tv_price);
			price.setText(getString(R.string.ticket_cart_adult_value,
					SentosaUtils.DoFormat(String.valueOf(multiAdd))));
			TableRow.LayoutParams params = new TableRow.LayoutParams(
					TableRow.LayoutParams.MATCH_PARENT,
					TableRow.LayoutParams.WRAP_CONTENT);
			table.addView(row, params);
		}
		return addedPrices;
	}

	private void saveLocally() {

		int id = Const.mTicketsItems.get(pos).getId();
		for (int i = 0; i < Const.mEventCartItems.size(); i++) {
			ContentValues content = null;
			String[] selectionArgs = null;
			Cursor cursor = mResolver.query(ContentURIs.CART_URI, null, Queries
					.IS_EVENT_SHOPPING_CART_EXIST_QUERY(id,
							Const.EVENT_TICKET_TYPE_CODE, Const.mEventCartItems
									.get(i).getId()), selectionArgs,
					Const.MANUAL);
			if (cursor.getCount() > 0) {
				cursor.moveToPosition(0);

				String selection = CartData.CART_ID_COL + " = '"
						+ String.valueOf(id) + "' AND "
						+ CartData.CART_TYPE_COL + " = '"
						+ String.valueOf(Const.EVENT_TICKET_TYPE_CODE)
						+ "' AND " + CartData.CART_DETAIL_ID_COL + " = '"
						+ String.valueOf(Const.mEventCartItems.get(i).getId())
						+ "'";

				int qtyVal = Const.mEventCartItems.get(i).getQty()
						+ cursor.getInt(cursor
								.getColumnIndex(CartData.CART_QTY_COL));

				content = jsonParser.addCartItem(id, EventName, qtyVal,
						Const.mEventCartItems.get(i).getPrice(), qtyVal
								* Const.mEventCartItems.get(i).getPrice(),
						Const.EVENT_TICKET_TYPE_CODE, Const.mEventCartItems
								.get(i).getDesc(), Const.mEventCartItems
								.get(i).getId(), EventDate);
				mResolver.update(ContentURIs.CART_URI, content, selection,
						selectionArgs);

			} else {

				content = jsonParser.addCartItem(id, EventName,
						Const.mEventCartItems.get(i).getQty(),
						Const.mEventCartItems.get(i).getPrice(),
						Const.mEventCartItems.get(i).getQty()
								* Const.mEventCartItems.get(i).getPrice(),
						Const.EVENT_TICKET_TYPE_CODE, Const.mEventCartItems
								.get(i).getDesc(),  Const.mEventCartItems
								.get(i).getId(), EventDate);
				mResolver.insert(ContentURIs.CART_URI, content);

			}
			cursor.close();
		}
	}

}
