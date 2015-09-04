package com.mysentosa.android.sg;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.ContentResolver;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.mysentosa.android.sg.asynctask.BookingFeeAsyncTask;
import com.mysentosa.android.sg.asynctask.GetRegisterTokenAsyncTask;
import com.mysentosa.android.sg.asynctask.TicketCheckoutAsyncTask;
import com.mysentosa.android.sg.models.ShoppingCartItem;
import com.mysentosa.android.sg.provider.utils.SentosaDatabaseStructure.CartData;
import com.mysentosa.android.sg.provider.utils.SentosaDatabaseStructure.ContentURIs;
import com.mysentosa.android.sg.provider.utils.SentosaDatabaseStructure.Queries;
import com.mysentosa.android.sg.utils.Const;
import com.mysentosa.android.sg.utils.CustomCallback;
import com.mysentosa.android.sg.utils.LogHelper;
import com.mysentosa.android.sg.utils.SentosaUtils;

@SuppressLint("NewApi")
public class TicketShopingCartActivity extends Activity implements
		LoaderCallbacks<Cursor> {

	private TextView tvBkAmt;
	private TextView tvGrandTotalCount;
	private TextView tvBookingText;
	private TextView tvGrandTotalText;

	private TextView mTxtNoItems;
	private RelativeLayout mRelItem;
	private Button btn_checkout;
	ArrayList<String> mTotlIDs = new ArrayList<String>();
	ArrayList<Integer> mTypes = new ArrayList<Integer>();
	LinkedHashMap<Integer, ArrayList<SaveID>> myIDs = new LinkedHashMap<Integer, ArrayList<SaveID>>();
	ArrayList<ShoppingCartItem> mShopingsItem;
	LinkedHashMap<String, ArrayList<ShoppingCartItem>> hmData = new LinkedHashMap<String, ArrayList<ShoppingCartItem>>();
	float grandTotalAmt, mTotalPrice;
	private ContentResolver mResolver;
	int REQUEST_SHOPING_CODE = 2;
	SharedPreferences sharePref;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.tickets_shoping_cart);
		sharePref = PreferenceManager.getDefaultSharedPreferences(this);
		mResolver = getContentResolver();
		initializeViews();
		String token = sharePref.getString("Token", null);
		String device_id = sharePref.getString("Device_Id", null);		
		if (token == null || device_id == null)
			new GetRegisterTokenAsyncTask(TicketShopingCartActivity.this)
					.execute();
	}

	private void initializeViews() {

		tvBkAmt = (TextView) findViewById(R.id.tv_bk_amt);
		tvBkAmt.setVisibility(View.INVISIBLE);

		tvGrandTotalCount = (TextView) findViewById(R.id.tv_total_count);
		tvGrandTotalCount.setVisibility(View.INVISIBLE);

		tvBookingText = (TextView) findViewById(R.id.tv_bkfee);
		tvBookingText.setVisibility(View.INVISIBLE);

		tvGrandTotalText = (TextView) findViewById(R.id.tv_total_text);
		tvGrandTotalText.setVisibility(View.INVISIBLE);

		mTxtNoItems = (TextView) findViewById(R.id.emptyItems);
		mTxtNoItems.setVisibility(View.GONE);

		mRelItem = (RelativeLayout) findViewById(R.id.rl_middle);
		mRelItem.setVisibility(View.GONE);

		btn_checkout = (Button) findViewById(R.id.btn_checkout);
		btn_checkout.setVisibility(View.INVISIBLE);
		btn_checkout.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (Const.mBookingFees != -1) {

					TicketCheckoutAsyncTask ticketCheckout = new TicketCheckoutAsyncTask(
							TicketShopingCartActivity.this,
							new CustomCallback() {
								@Override
								public void isFnished(boolean isSucceed) {
									if (isSucceed) {
										startActivityForResult(new Intent(
												TicketShopingCartActivity.this,
												TicketCheckoutActivity.class),
												REQUEST_SHOPING_CODE);
									} else {
										startActivityForResult(new Intent(
												TicketShopingCartActivity.this,
												TicketFailedCheckout.class),
												REQUEST_SHOPING_CODE);
									}
								}
							});
					ticketCheckout.execute();
				}
			}
		});
		this.getLoaderManager().restartLoader(0, null, this);
	}
	
	private class ShoppingCartItemWrapper {
		public String header;
		public List<ShoppingCartItem> listItem;
		
		public ShoppingCartItemWrapper() {
			header = null;
			listItem = new ArrayList<ShoppingCartItem>();
		}
	}

	private void fillTable() {

		float totalPrice = 0;
		mTotalPrice = 0;
		TableLayout table = (TableLayout) findViewById(R.id.tb_table);
		table.removeAllViews();
		LogHelper.i("table", "remove");
		LayoutInflater inflater = getLayoutInflater();
		
		

		if (mTotlIDs.size() > 0) {

			tvBkAmt.setVisibility(View.VISIBLE);
			tvBookingText.setVisibility(View.VISIBLE);
			tvGrandTotalCount.setVisibility(View.VISIBLE);
			tvGrandTotalText.setVisibility(View.VISIBLE);
			btn_checkout.setVisibility(View.VISIBLE);

			ArrayList<Integer> listOfTicketID = new ArrayList<Integer>();
			
			for (int i = 0; i < mTotlIDs.size(); i++) {
				totalPrice = 0;
				ArrayList<ShoppingCartItem> mShopingsItemData = hmData.get(mTotlIDs.get(i));
				
				//create groups. Each group has different description
				
				ArrayList<ShoppingCartItemWrapper> mSplitItem = new ArrayList<ShoppingCartItemWrapper>();
				String currentDesc = mShopingsItemData.get(0).getCart_desc();
				mSplitItem.add(new ShoppingCartItemWrapper());
				mSplitItem.get(0).header = mShopingsItemData.get(0).getCart_desc();
				
				for (ShoppingCartItem item : mShopingsItemData) {
					if (item.getCart_desc().equals(currentDesc)) {
						mSplitItem.get(mSplitItem.size() - 1).listItem.add(item);
					} else {
						currentDesc = item.getCart_desc();
						ShoppingCartItemWrapper newWrapper = new ShoppingCartItemWrapper();
						newWrapper.header = item.getCart_desc();
						mSplitItem.add(newWrapper);
						mSplitItem.get(mSplitItem.size() - 1).listItem.add(item);
					}
				}
				

				listOfTicketID.add(mShopingsItemData.get(0).getCart_id());
				
				for(final ShoppingCartItemWrapper wrapper : mSplitItem) {
					
					// First: Heading row
					TableRow row = (TableRow) inflater.inflate(
							R.layout.item_shoppingcart, table, false);
					row.setPadding(0, 10, 0, 10);
					TextView tv_desc = (TextView) row.findViewById(R.id.tv_items_desc);
					tv_desc.setText(wrapper.header);
					TextView tv_price = (TextView) row.findViewById(R.id.tv_price);
					tv_price.setTag(mTotlIDs.get(i));
					tv_price.setOnClickListener(new OnClickListener() {
						@Override
						public void onClick(View v) {
							//Keep this comment to check
							//will delete after make sure that every thing is ok
//							customAlert(v.getTag().toString());
//							SentosaUtils.showToast(TicketShopingCartActivity.this, "Not Implemented");
							showDeleteDialog(v.getTag().toString(), wrapper);
						}
					});
					
					TextView tv_date = (TextView) row
							.findViewById(R.id.tv_items_date);
					
					if (wrapper.listItem.get(0).getCart_date() != null
							&& wrapper.listItem.get(0).getCart_date() != ""
							&& wrapper.listItem.get(0).getCart_date().length() > 0)
						tv_date.setText("for "
								+ wrapper.listItem.get(0).getCart_date());
					
					TableRow.LayoutParams params = new TableRow.LayoutParams(
							TableRow.LayoutParams.MATCH_PARENT,
							TableRow.LayoutParams.WRAP_CONTENT);
					table.addView(row, params);
					
					// Middle row
					for (int j = 0; j < wrapper.listItem.size(); j++) {
						
						ShoppingCartItem item = wrapper.listItem.get(j);
						LogHelper.i("cart", "desc " + item.getCart_desc() + " type: " + item.getCart_type() + " name:" + item.getCart_name() );
						
						TableRow row_sub = (TableRow) inflater.inflate(
								R.layout.item_shopping_runtime, table, false);
						TextView tv_sub_name = (TextView) row_sub
								.findViewById(R.id.tv_subitem);
						
						if (item.getCart_type()
								!= Const.EVENT_TICKET_TYPE_CODE) {
							tv_sub_name
							.setText(SentosaUtils
									.getHeadingAllFormatValues(String.valueOf(
											item.getCart_Qty()),
											item.getCart_name(),
											item.getCart_Price()));
							
						} else {
							tv_sub_name
							.setText(SentosaUtils
									.getShopingFormatValues(
											item.getCart_Price(),
											item.getCart_name(),
											item.getCart_Qty()));
						}
						
						TextView tv_sub_price = (TextView) row_sub.findViewById(R.id.tv_subprice);
						tv_sub_price.setText(SentosaUtils
								.getBookingFormatValues(item.getCart_Amount()));
						
						TableRow.LayoutParams paramsLine = new TableRow.LayoutParams(
								TableRow.LayoutParams.MATCH_PARENT,
								TableRow.LayoutParams.WRAP_CONTENT);
						table.addView(row_sub, paramsLine);
						
						totalPrice += item.getCart_Amount();
						
					}
					mTotalPrice += totalPrice;
					tv_price.setText(SentosaUtils.getFormatValues(totalPrice));
					
					// Last: Horizontal line row
					TableRow myRow = new TableRow(this);
					Resources res = getResources();
					Drawable line = res
							.getDrawable(R.drawable.horizontal_doted_line);
					myRow.setBackgroundDrawable(line);
					TableRow.LayoutParams paramsLine = new TableRow.LayoutParams(
							TableRow.LayoutParams.MATCH_PARENT, 2);
					table.addView(myRow, paramsLine);
				}
				
			}

			if (SentosaUtils.isUserLogined(this)) {
			  //Save the ID of Bought ticket, we'll use it to claim the Exclusive deals
	            SentosaUtils.saveListOfBoughtTicketID(this, listOfTicketID);
			}
			
			
			// Grand Total

			grandTotalAmt = Const.mBookingFees + mTotalPrice;
			tvGrandTotalCount.setText(SentosaUtils
					.getBookingFormatValues(grandTotalAmt));

		} else {
			mTxtNoItems.setVisibility(View.VISIBLE);
			mRelItem.setVisibility(View.GONE);
			tvBkAmt.setVisibility(View.GONE);
			tvBookingText.setVisibility(View.GONE);
			tvGrandTotalCount.setVisibility(View.GONE);
			tvGrandTotalText.setVisibility(View.GONE);
			btn_checkout.setVisibility(View.GONE);
		}
	}

	@Override
	public Loader<Cursor> onCreateLoader(int arg0, Bundle arg1) {
		CursorLoader loader = null;
		loader = new CursorLoader(this, ContentURIs.CART_URI, null,
				Queries.GET_SHOPPING_CART_TYPE_QUERY, null, Const.MANUAL);
		return loader;
	}

	@Override
	public void onLoadFinished(Loader<Cursor> arg0, Cursor arg1) {

		if (arg1.getCount() > 0) {
			mTxtNoItems.setVisibility(View.GONE);
			mRelItem.setVisibility(View.VISIBLE);
			makeID(arg1);
			bookingFeed();
		} else {
			mTxtNoItems.setVisibility(View.VISIBLE);
			mRelItem.setVisibility(View.GONE);
		}
	}

	@Override
	public void onLoaderReset(Loader<Cursor> arg0) {
	}

	private void bookingFeed() {
		BookingFeeAsyncTask booking = new BookingFeeAsyncTask(this,
				new CustomCallback() {
					@Override
					public void isFnished(boolean isSucceed) {
						if (isSucceed) {
							tvBkAmt.setText(SentosaUtils
									.getBookingFormatValues(Const.mBookingFees));
						}
						if (hmData.size() > 0)
							fillTable();
					}
				});
		booking.execute();
	}

	//Truc Tran: write new method deleteSingleRow
	//keep here to check. 
	//delete later, after make sure that everything is ok
	
//	private void deleteRecord(String total_id) {
//		String additionalClause = null;
//		if (Integer.parseInt(total_id.split("_")[0]) == Const.EVENT_TICKET_TYPE_CODE) {
//			additionalClause = 	CartData.CART_DATE_COL + " = '" + total_id.split("_")[2] + "' AND " 
//					+ 			CartData.CART_ID_COL + " = " + Integer.parseInt(total_id.split("_")[1]) + " AND " 
//					+ 			CartData.CART_TYPE_COL + " = " + Integer.parseInt(total_id.split("_")[0]);
//		} else {
//			additionalClause = 	CartData.CART_ID_COL + " = " + Integer.parseInt(total_id.split("_")[1]) + " AND "
//					+ 			CartData.CART_TYPE_COL + " = " + Integer.parseInt(total_id.split("_")[0]);
//		}
//		LogHelper.i("query", additionalClause);
//
//		mResolver.delete(ContentURIs.CART_URI, additionalClause, null);
//		Iterator itr = mTotlIDs.iterator();
//		String strElement;
//		while (itr.hasNext()) {
//			strElement = (String) itr.next();
//			if (strElement == total_id) {
//				itr.remove();
//			}
//		}
//
//		fillTable();
//	}

	private void makeID(final Cursor mCursor) {
		hmData.clear();
		mTypes.clear();
		int size = mCursor.getCount();
		if (size > 0) {
			for (int i = 0; i < mCursor.getCount(); i++) {
				mCursor.moveToPosition(i);
				mTypes.add(mCursor.getInt(0));
			}
		}
		mCursor.close();

		if (size > 0) {
			myIDs.clear();
			for (int i = 0; i < mTypes.size(); i++) {
				getID(mTypes.get(i));
			}

			mTotlIDs.clear();
			for (int i = 0; i < mTypes.size(); i++) {
				ArrayList<SaveID> entry = myIDs.get(mTypes.get(i));
				for (int j = 0; j < entry.size(); j++) {
					getIdividualRecords(entry.get(j).get_id(), mTypes.get(i),
							entry.get(j).get_date());
				}
			}
		}
	}

	private void getID(int type) {
		ArrayList<SaveID> mSaveIDs = new ArrayList<SaveID>();
		SaveID saveID;
		Cursor cursor = getContentResolver().query(ContentURIs.CART_URI, null,
				Queries.GET_SHOPPING_CART_ID_QUERY(type), null, Const.MANUAL);
		if (cursor.getCount() > 0) {
			while (cursor.moveToNext()) {
				saveID = new SaveID();
				saveID.set_id(cursor.getInt(cursor
						.getColumnIndex(CartData.CART_ID_COL)));
				saveID.set_date(cursor.getString(cursor
						.getColumnIndex(CartData.CART_DATE_COL)));
				mSaveIDs.add(saveID);
			}
			myIDs.put(type, mSaveIDs);
		}
		cursor.close();
	}

	private void getIdividualRecords(int _id, int type, String date) {

		String myID = type + "_" + _id + "_" + date;

		mShopingsItem = new ArrayList<ShoppingCartItem>();
		ShoppingCartItem shopingItem;
		Cursor cursor = getContentResolver().query(ContentURIs.CART_URI, null,
				Queries.GET_SHOPPING_CART_QUERY(_id, type, date), null,
				Const.MANUAL);

		if (cursor.getCount() > 0) {
			while (cursor.moveToNext()) {
				shopingItem = new ShoppingCartItem();
				shopingItem.set_id(cursor.getInt(cursor
						.getColumnIndex(CartData.ID_COL)));
				shopingItem.setCart_id(cursor.getInt(cursor
						.getColumnIndex(CartData.CART_ID_COL)));
				shopingItem.setCart_desc(cursor.getString(cursor
						.getColumnIndex(CartData.CART_DESC_COL)));
				shopingItem.setCart_Qty(cursor.getInt(cursor
						.getColumnIndex(CartData.CART_QTY_COL)));
				shopingItem.setCart_Price(cursor.getFloat(cursor
						.getColumnIndex(CartData.CART_PRICE_COL)));
				shopingItem.setCart_Amount(cursor.getFloat(cursor
						.getColumnIndex(CartData.CART_AMT_COL)));
				shopingItem.setCart_type(cursor.getInt(cursor
						.getColumnIndex(CartData.CART_TYPE_COL)));
				shopingItem.setCart_name(cursor.getString(cursor
						.getColumnIndex(CartData.CART_NAME_COL)));
				// shopingItem.setCart_selection_type(cursor.getInt(cursor
				// .getColumnIndex(CartData.CART_STYPE_COL)));
				shopingItem.setCart_detail_id(cursor.getInt(cursor
						.getColumnIndex(CartData.CART_DETAIL_ID_COL)));
				shopingItem.setCart_date(cursor.getString(cursor
						.getColumnIndex(CartData.CART_DATE_COL)));
				mShopingsItem.add(shopingItem);
			}
			hmData.put(myID, mShopingsItem);
		}
		cursor.close();
		mTotlIDs.add(myID);
	}
	
	//Truc Tran: write new method showDeleteDialog
	//keep here to check. 
	//delete later, after make sure that everything is ok

//	private void customAlert(final String id) {
//
//		LinearLayout llView = new LinearLayout(this);
//		llView.setGravity(Gravity.CENTER);
//		llView.setOrientation(LinearLayout.VERTICAL);
//
//		TextView txtMsg = new TextView(this);
//		txtMsg.setText(getString(R.string.item_remove_message));
//
//		txtMsg.setGravity(Gravity.CENTER_HORIZONTAL);
//		txtMsg.setPadding(5, 10, 5, 0);
//		txtMsg.setTextSize(21);
//
//		TextView txtTickets = new TextView(this);
//		txtTickets.setText(getString(R.string.item_remove_confirmation));
//		txtTickets.setGravity(Gravity.CENTER_HORIZONTAL);
//		txtTickets.setPadding(5, 10, 5, 10);
//		txtTickets.setTextSize(18);
//
//		llView.addView(txtMsg);
//		llView.addView(txtTickets);
//
//		AlertDialog.Builder builderCall = new AlertDialog.Builder(this);
//		builderCall.setView(llView);
//		builderCall.setPositiveButton(getString(R.string.yes),
//				new android.content.DialogInterface.OnClickListener() {
//					@Override
//					public void onClick(DialogInterface dialog, int which) {
//						deleteRecord(id);
//					}
//				}).setNegativeButton(getString(R.string.no),
//				new android.content.DialogInterface.OnClickListener() {
//					@Override
//					public void onClick(DialogInterface dialog, int which) {
//						dialog.dismiss();
//					}
//				});
//		builderCall.show();
//	}
	
	private void showDeleteDialog (final String totalID, final ShoppingCartItemWrapper wrapper) {
		LinearLayout llView = new LinearLayout(this);
		llView.setGravity(Gravity.CENTER);
		llView.setOrientation(LinearLayout.VERTICAL);

		TextView txtMsg = new TextView(this);
		txtMsg.setText(getString(R.string.item_remove_message));

		txtMsg.setGravity(Gravity.CENTER_HORIZONTAL);
		txtMsg.setPadding(5, 10, 5, 0);
		txtMsg.setTextSize(21);

		TextView txtTickets = new TextView(this);
		txtTickets.setText(getString(R.string.item_remove_confirmation));
		txtTickets.setGravity(Gravity.CENTER_HORIZONTAL);
		txtTickets.setPadding(5, 10, 5, 10);
		txtTickets.setTextSize(18);

		llView.addView(txtMsg);
		llView.addView(txtTickets);

		AlertDialog.Builder builderCall = new AlertDialog.Builder(this);
		builderCall.setView(llView);
		builderCall.setPositiveButton(getString(R.string.yes),
				new android.content.DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						deleteSingleRow(totalID, wrapper);
						dialog.dismiss();
					}
				}).setNegativeButton(getString(R.string.no),
				new android.content.DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}
				});
		builderCall.show();
	}
	
	private void deleteSingleRow(final String totalID, ShoppingCartItemWrapper wrapper) {
		//TODO
		int size = wrapper.listItem.size();
		if (size > 0) {
			ArrayList<ShoppingCartItem> shopingsItemData = hmData.get(totalID);
			for (int i = 0; i < size; i++) {
				ShoppingCartItem item = wrapper.listItem.get(i);
				String whereClause =  CartData.ID_COL + " = '" + item.get_id() + "'";
				mResolver.delete(ContentURIs.CART_URI, whereClause, null);
				shopingsItemData.remove(item);
			}
			
			Iterator itr = mTotlIDs.iterator();
			String strElement;
			while (itr.hasNext()) {
				strElement = (String) itr.next();
				if (strElement == totalID && (shopingsItemData == null || shopingsItemData.size() == 0)) {
					itr.remove();
				}
			}
		}
		fillTable();
	}

	class SaveID {
		int _id;
		String _date;

		public int get_id() {
			return _id;
		}

		public void set_id(int _id) {
			this._id = _id;
		}

		public String get_date() {
			return _date;
		}

		public void set_date(String _date) {
			this._date = _date;
		}

	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == REQUEST_SHOPING_CODE && resultCode == RESULT_OK) {
			setResult(RESULT_OK);
			finish();
		} else if (resultCode == Const.RESULT_CANCEL_CODE) {
			setResult(Const.RESULT_CANCEL_CODE);
			finish();
		}
	}

}