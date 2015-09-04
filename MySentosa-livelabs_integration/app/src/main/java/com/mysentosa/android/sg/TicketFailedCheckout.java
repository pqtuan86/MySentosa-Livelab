package com.mysentosa.android.sg;

import java.util.ArrayList;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.mysentosa.android.sg.models.CartIFailedtems;
import com.mysentosa.android.sg.provider.utils.SentosaDatabaseStructure.CartData;
import com.mysentosa.android.sg.provider.utils.SentosaDatabaseStructure.ContentURIs;
import com.mysentosa.android.sg.provider.utils.SentosaDatabaseStructure.Queries;
import com.mysentosa.android.sg.utils.AlertHelper;
import com.mysentosa.android.sg.utils.Const;
import com.mysentosa.android.sg.utils.SentosaUtils;

public class TicketFailedCheckout extends Activity implements OnClickListener {

	private Button btnRemoveItems;
	private Button btnCancel;
	private TableLayout tbTable;
	ArrayList<CartIFailedtems> mFaildItem = new ArrayList<CartIFailedtems>();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.tickets_shoping_cart_failed_checkout);
		findViews();
		getData();

		AlertHelper.showPopup(TicketFailedCheckout.this,
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
						if (Const.mFailedTicketsCheckoutItems.size() <= 0) {
							getContentResolver().delete(ContentURIs.CART_URI,
									null, null);

							setResult(Const.RESULT_CANCEL_CODE);
							finish();
						}
					}
				}, getString(R.string.sold_out));
	}

	private void findViews() {
		((TextView) findViewById(R.id.header_title))
				.setText(getString(R.string.ticket_fail_for_checkout));

		tbTable = (TableLayout) findViewById(R.id.tb_table);
		btnRemoveItems = (Button) findViewById(R.id.btn_remove_items);
		btnCancel = (Button) findViewById(R.id.btn_cancel);
		btnRemoveItems.setOnClickListener(this);
		btnCancel.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		if (v == btnRemoveItems) {
			for (int i = 0; i < Const.mFailedTicketsCheckoutItems.size(); i++) {
				deleteRecord(Const.mFailedTicketsCheckoutItems.get(i).getId());
			}
			setResult(Const.RESULT_CANCEL_CODE);
			finish();

		} else if (v == btnCancel) {
			getContentResolver().delete(ContentURIs.CART_URI, null, null);

			setResult(Const.RESULT_CANCEL_CODE);
			finish();
		}
	}

	private void getData() {
		mFaildItem.clear();
		CartIFailedtems cartItem;
		for (int i = 0; i < Const.mFailedTicketsCheckoutItems.size(); i++) {				
			Cursor cursor = getContentResolver()
					.query(ContentURIs.CART_URI,
							null,
							Queries.GET_FAILED_ITEM_QUERY(Const.mFailedTicketsCheckoutItems
									.get(i).getId()), null, Const.MANUAL);
			if (cursor.getCount() > 0) {
				while (cursor.moveToNext()) {
					cartItem = new CartIFailedtems();
					cartItem.setId(cursor.getInt(cursor
							.getColumnIndex(CartData.CART_ID_COL)));
					cartItem.setName(cursor.getString(cursor
							.getColumnIndex(CartData.CART_NAME_COL)));
					cartItem.setDesc(cursor.getString(cursor
							.getColumnIndex(CartData.CART_DESC_COL)));
					cartItem.setPrice(cursor.getFloat(cursor
							.getColumnIndex(CartData.CART_PRICE_COL)));
					cartItem.setQty(cursor.getInt(cursor
							.getColumnIndex(CartData.CART_QTY_COL)));
					cartItem.setAmt(cursor.getFloat(cursor
							.getColumnIndex(CartData.CART_AMT_COL)));
					cartItem.setDate(cursor.getString(cursor
							.getColumnIndex(CartData.CART_DATE_COL)));
					cartItem.setType(Const.mFailedTicketsCheckoutItems.get(i).getTicketType());
					mFaildItem.add(cartItem);
				}
			}
			cursor.close();
		}
		fillTable();
	}

	private void fillTable() {

		tbTable.removeAllViews();
		LayoutInflater inflater = getLayoutInflater();

		if (mFaildItem.size() > 0) {
			btnRemoveItems.setVisibility(View.VISIBLE);
			btnCancel.setVisibility(View.VISIBLE);

			for (int i = 0; i < mFaildItem.size(); i++) {

				// First: Heading row
				TableRow row = (TableRow) inflater.inflate(
						R.layout.item_failed_shoppingcart, tbTable, false);
				row.setPadding(0, 10, 0, 10);

				TextView tv_desc = (TextView) row
						.findViewById(R.id.tv_items_desc);

				tv_desc.setText(mFaildItem.get(i).getDesc());

				TextView tv_price = (TextView) row.findViewById(R.id.tv_price);
				tv_price.setText(SentosaUtils.getFormatValues(mFaildItem.get(i)
						.getAmt()));

				TextView tv_date = (TextView) row
						.findViewById(R.id.tv_items_date);

				if (mFaildItem.get(i).getDate() != null
						&& mFaildItem.get(i).getDate() != ""
						&& mFaildItem.get(i).getDate().length() > 0)
					tv_date.setText("for " + mFaildItem.get(i).getDate());

				TableRow.LayoutParams params = new TableRow.LayoutParams(
						TableRow.LayoutParams.MATCH_PARENT,
						TableRow.LayoutParams.WRAP_CONTENT);
				tbTable.addView(row, params);

				// Second: Middle row

				TableRow row_sub = (TableRow) inflater.inflate(
						R.layout.item_failed_runtime, tbTable, false);

				TextView tv_sub_name = (TextView) row_sub
						.findViewById(R.id.tv_items_desc_text);

			
				
				if (!mFaildItem.get(i).getType().equalsIgnoreCase("Event")) {

					tv_sub_name.setText(SentosaUtils.getHeadingAllFormatValues(
							String.valueOf(mFaildItem.get(i).getQty()),
							mFaildItem.get(i).getName(), mFaildItem.get(i)
									.getPrice()));

				} else {
					tv_sub_name.setText(SentosaUtils.getShopingFormatValues(
							mFaildItem.get(i).getPrice(), mFaildItem.get(i)
									.getName(), mFaildItem.get(i).getQty()));
				}

				TextView tv_sub_price = (TextView) row_sub
						.findViewById(R.id.tv_total_price);
				tv_sub_price.setText(SentosaUtils
						.getBookingFormatValues(mFaildItem.get(i).getAmt()));

				TableRow.LayoutParams paramsMiddle = new TableRow.LayoutParams(
						TableRow.LayoutParams.MATCH_PARENT,
						TableRow.LayoutParams.WRAP_CONTENT);
				tbTable.addView(row_sub, paramsMiddle);

				// Last: Horizontal line row
				TableRow myRow = new TableRow(this);
				Resources res = getResources();
				Drawable line = res
						.getDrawable(R.drawable.horizontal_normal_line);
				myRow.setBackgroundDrawable(line);
				TableRow.LayoutParams paramsLine = new TableRow.LayoutParams(
						TableRow.LayoutParams.MATCH_PARENT, 2);
				tbTable.addView(myRow, paramsLine);
			}
		}
	}

	private void deleteRecord(int total_id) {
		String additionalClause = CartData.CART_DETAIL_ID_COL + " = "
				+ total_id;
		getContentResolver().delete(ContentURIs.CART_URI, additionalClause,
				null);
	}
}
