package com.mysentosa.android.sg;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TableLayout;
import android.widget.TextView;

import com.mysentosa.android.sg.asynctask.MyPurchaseDetailAsyncTask;
import com.mysentosa.android.sg.imageloader.ImageFetcher;
import com.mysentosa.android.sg.models.MyPurchasesDetailItem;
import com.mysentosa.android.sg.models.MyPurchasesDetailItem.TicketDetailsEntry;
import com.mysentosa.android.sg.models.MyPurchasesDetailSplitItem;
import com.mysentosa.android.sg.utils.Const;
import com.mysentosa.android.sg.utils.CustomCallback;
import com.mysentosa.android.sg.utils.HttpHelper;
import com.mysentosa.android.sg.utils.SentosaUtils;

public class TicketPurchaseDetailActivity extends Activity {

	int pos;
	private ImageFetcher mImageWorker;
	LazyAdapterAll adapter;
	ListView list;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.ticket_purchase_detail);
		Const.mPurchaseDetailSplitItems.clear();
		list = (ListView) findViewById(R.id.lv_table);
		adapter = new LazyAdapterAll(this);		
		list.setAdapter(adapter);
		
		
		mImageWorker = ((SentosaApplication) this.getApplication()).mImageFetcher;
		pos = getIntent().getIntExtra("Position", 0);
		MyPurchaseDetailAsyncTask myPurchaseDeatil = new MyPurchaseDetailAsyncTask(
				this, new CustomCallback() {

					@Override
					public void isFnished(boolean isSucceed) {
						if (isSucceed) {
							fillTable();
						}
					}
				}, Const.mPurchaseItems.get(pos).getReservationId());
		myPurchaseDeatil.execute();
	}

	private void fillTable() {
		Const.mPurchaseDetailSplitItems.clear();

		MyPurchasesDetailSplitItem myPurchaseSplit;

		for (int i = 0, purchaseLen = Const.mPurchaseDetailItems.size(); i < purchaseLen; i++) {
			MyPurchasesDetailItem myItem = Const.mPurchaseDetailItems.get(i);

			for (int j = 0, ticketLen = myItem.getTicketDetails().size(); j < ticketLen; j++) {
				TicketDetailsEntry ticketDetail = myItem.getTicketDetails().get(j);
				myPurchaseSplit = new MyPurchasesDetailSplitItem();
				// 1
				myPurchaseSplit.setPurchaseDate(myItem.getPurchaseDate());
				myPurchaseSplit.setPinCode(myItem.getPinCode());
				// 2
				myPurchaseSplit.setGroup_Id(myItem.getGroup().getId());
				myPurchaseSplit.setDate(myItem.getGroup().getDate());
				myPurchaseSplit.setTicketType(myItem.getGroup().getTicketType());
				// 3
				myPurchaseSplit.setId(ticketDetail.getId());
				myPurchaseSplit.setName(ticketDetail.getName());
				myPurchaseSplit.setDescription(ticketDetail.getDescription());
				myPurchaseSplit.setPrice(ticketDetail.getPrice());
				myPurchaseSplit.setGroupCode(ticketDetail.getGroupCode());
				myPurchaseSplit.setQuantity(ticketDetail.getQuantity());
				myPurchaseSplit.setDiscountType(ticketDetail.getTicketType());
				myPurchaseSplit.setContent(ticketDetail.getContent());
				//4
				myPurchaseSplit.setText(myItem.getBarcode().getText());
				myPurchaseSplit.setImageLink(myItem.getBarcode().getImageLink());
				
				Const.mPurchaseDetailSplitItems.add(myPurchaseSplit);
			}
		}

		adapter.notifyDataSetChanged();
	}

	public class LazyAdapterAll extends BaseAdapter {

		private Activity activity;
		public LayoutInflater inflater = null;
		private MyPurchasesDetailSplitItem mySplitItem;

		public LazyAdapterAll(Activity a) {
			activity = a;
			inflater = (LayoutInflater) activity
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		}

		public int getCount() {
			return Const.mPurchaseDetailSplitItems.size();
		}

		public Object getItem(int position) {
			return position;
		}

		public long getItemId(int position) {
			return position;
		}

		public class ViewHolder {
			public TextView tv_name, tv_purchase_date, tv_sub_name,
					tv_sub_price;
			public TextView tv_event_date, tv_total_sum, tv_pin_code, tv_pin;
			public ImageView iv_barcode;
			public TableLayout tl_row;

		}

		public View getView(int position, View convertView, ViewGroup parent) {
			mySplitItem = Const.mPurchaseDetailSplitItems.get(position);
			float totalPrice = 0;
			View vi = convertView;
			ViewHolder holder;
			if (convertView == null) {

				vi = inflater.inflate(R.layout.item_purchases_detail_tickets,
						null);
				holder = new ViewHolder();

				holder.tv_name = (TextView) vi.findViewById(R.id.tv_items_name);
				holder.tv_purchase_date = (TextView) vi
						.findViewById(R.id.tv_items_purchase_date);
				holder.tv_sub_name = (TextView) vi
						.findViewById(R.id.tv_subitem);
				holder.tv_sub_price = (TextView) vi
						.findViewById(R.id.tv_subprice);
				holder.tv_event_date = (TextView) vi
						.findViewById(R.id.tv_items_event_date);
				holder.tv_total_sum = (TextView) vi
						.findViewById(R.id.tv_items_total_sum);
				holder.tv_pin_code = (TextView) vi
						.findViewById(R.id.tv_items_pincode);
				holder.tv_pin = (TextView) vi.findViewById(R.id.tv_items_pin);
				holder.iv_barcode = (ImageView) vi
						.findViewById(R.id.iv_items_barcode);

				vi.setTag(holder);
			} else
				holder = (ViewHolder) vi.getTag();

			holder.tv_name.setText(mySplitItem.getName());

			holder.tv_purchase_date.setText(getString(R.string.purchase_date)
					+ " "
					+ Const.sdf.format(SentosaUtils.getDate(String
							.valueOf(mySplitItem.getPurchaseDate()))));

			holder.tv_event_date.setVisibility(View.GONE);
			if (mySplitItem.getTicketType().equalsIgnoreCase("Event")) {
				holder.tv_event_date.setVisibility(View.VISIBLE);
				holder.tv_event_date.setText(getString(R.string.event_date)
						+ " "
						+ Const.sdf.format(SentosaUtils.getDate(String
								.valueOf(mySplitItem.getDate()))));
			}

			if (mySplitItem.getTicketType().equalsIgnoreCase("Event")) {

				holder.tv_sub_name.setText(SentosaUtils.getShopingFormatValues(
						mySplitItem.getPrice(), mySplitItem.getDescription(),
						mySplitItem.getQuantity()));

			} else {
//				holder.tv_sub_name.setText(SentosaUtils
//						.getHeadingAllFormatValues(
//								String.valueOf(mySplitItem.getQuantity()),mySplitItem.getDiscountType(),
//								mySplitItem.getPrice()));
				holder.tv_sub_name.setText(
						SentosaUtils.getHeadingAllFormatValues(
								String.valueOf(mySplitItem.getQuantity()), mySplitItem.getContent(), mySplitItem.getDiscountType(),
								mySplitItem.getPrice()));
			}

			totalPrice += mySplitItem.getQuantity() * mySplitItem.getPrice();

			holder.tv_sub_price.setText(SentosaUtils
					.getBookingFormatValues(mySplitItem.getQuantity()
							* mySplitItem.getPrice()));

			holder.tv_total_sum.setText(SentosaUtils
					.getBookingFormatValues(totalPrice));

			holder.tv_pin_code.setText(getString(R.string.pincode) + ""
					+ mySplitItem.getPinCode());

			mImageWorker.loadImage(
					HttpHelper.BASE_HOST + mySplitItem.getImageLink(),
					holder.iv_barcode, null, R.drawable.bg_gradient_img, false,
					null);

			holder.tv_pin.setText(mySplitItem.getText());

			return vi;

		}
	}	
	
}
