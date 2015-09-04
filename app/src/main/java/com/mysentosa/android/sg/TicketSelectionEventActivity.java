package com.mysentosa.android.sg;

import java.util.ArrayList;
import java.util.LinkedHashMap;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.mysentosa.android.sg.asynctask.AvailableTiketsAsyncTask;
import com.mysentosa.android.sg.asynctask.GetPromocodeAsyncTask;
import com.mysentosa.android.sg.asynctask.GetTicketEventsDetailAsyncTask;
import com.mysentosa.android.sg.models.CartItems;
import com.mysentosa.android.sg.models.ItemPopup;
import com.mysentosa.android.sg.models.TicketEventDetailItem;
import com.mysentosa.android.sg.models.TicketEventDetailItem.EventDetailsEntry;
import com.mysentosa.android.sg.utils.AlertHelper;
import com.mysentosa.android.sg.utils.Const;
import com.mysentosa.android.sg.utils.CustomCallback;
import com.mysentosa.android.sg.utils.SentosaUtils;

public class TicketSelectionEventActivity extends Activity {

	private EditText edPromoCode;
	private Button btnNext;
	int pos, TicketsCode;
	Spinner spDate, spTicket, spQty;
	Integer[] items = new Integer[] { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10 };
	ArrayList<Integer> mItemID;
	ArrayList<String> mItemDates, mItemSelection;// , mItemTopup;;
	ArrayList<ItemPopup> mItemPTopup, mItemPSelections,
			mItemSelectedSelections;
	private RelativeLayout mRel_Note, mRel_Top;
	private ImageView mNoteArrow, mTopArrow;
	int REQUEST_CODE = 1;
	TextView mTxtNotes;
	TableLayout table;
	LinkedHashMap<String, EventDetailsEntry> hmData = new LinkedHashMap<String, EventDetailsEntry>();
	String EventId, EventDate, EventName;
	int ItemEventID;
	CartItems mCartItem;
	ArrayList<Integer> EvetnItemids;
	String notes;
	StringBuilder sb;// = new StringBuilder();


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.tickets_selection_event);
		pos = getIntent().getIntExtra("Position", 0);
		TicketsCode = getIntent().getIntExtra("TicketsCode", 0);
		initializeViews();

		Const.mTicketsEventdetailItems.clear();
		GetTicketEventsDetailAsyncTask getAsync = new GetTicketEventsDetailAsyncTask(
				this, new CustomCallback() {
					@Override
					public void isFnished(boolean isSucceed) {
						if (isSucceed) {
							fillData();
						}
					}
				}, TicketsCode, Const.mTicketsItems.get(pos).getId());
		getAsync.execute();
	}

	private void initializeViews() {
		((TextView) findViewById(R.id.header_title))
				.setText("Ticket Selection");

		spDate = (Spinner) findViewById(R.id.sn_date);
		spQty = (Spinner) findViewById(R.id.sn_qty);

		// ARROWS
		mTopArrow = (ImageView) findViewById(R.id.iv_arrow_top);
		mTopArrow.setBackgroundResource(R.drawable.accordian_arrow_down);
		table = (TableLayout) findViewById(R.id.tb_top_table);
		mRel_Top = (RelativeLayout) findViewById(R.id.rl_topup);
		mRel_Top.setTag("down");
		mRel_Top.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (v.getTag().toString().equalsIgnoreCase("down")) {
					v.setTag("up");
					table.setVisibility(View.VISIBLE);
					mTopArrow
							.setBackgroundResource(R.drawable.accordian_arrow_up);
				} else {
					v.setTag("down");
					table.setVisibility(View.GONE);
					mTopArrow
							.setBackgroundResource(R.drawable.accordian_arrow_down);
				}
			}
		});

		// Promo
		edPromoCode = (EditText) findViewById(R.id.ed_promo);

		mTxtNotes = (TextView) findViewById(R.id.tv_notes);
		mTxtNotes.setMovementMethod(new ScrollingMovementMethod());
		
		mNoteArrow = (ImageView) findViewById(R.id.iv_arrow_note);
		mNoteArrow.setBackgroundResource(R.drawable.accordian_arrow_right);
		mRel_Note = (RelativeLayout) findViewById(R.id.rl_note);
		mRel_Note.setTag("down");
		mRel_Note.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(TicketSelectionEventActivity.this, ImpNotesActivity.class);
				intent.putExtra(ImpNotesActivity.IMPORTANT_NOTES, notes);
				startActivity(intent);
				/*if (v.getTag().toString().equalsIgnoreCase("down")) {
					v.setTag("up");
					mTxtNotes.setVisibility(View.VISIBLE);
					mNoteArrow
							.setBackgroundResource(R.drawable.accordian_arrow_up);
				} else {
					v.setTag("down");
					mTxtNotes.setVisibility(View.GONE);
					mNoteArrow
							.setBackgroundResource(R.drawable.accordian_arrow_down);
				}*/
			}
		});

		btnNext = (Button) findViewById(R.id.btn_next);
		btnNext.setTypeface(((SentosaApplication) this.getApplication()).myridTypeFace);
		btnNext.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Const.mEventCartItems.clear();
				String qtyValue = spQty.getSelectedItem().toString();
				String promoValue = edPromoCode.getText().toString().trim();

				if (spTicket == null) {
					AlertHelper.showPopup(TicketSelectionEventActivity.this,
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									dialog.dismiss();
								}
							}, getString(R.string.ticket_add_message));
				} else if (Integer.parseInt(qtyValue.trim()) == 0) {
					AlertHelper.showPopup(TicketSelectionEventActivity.this,
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									dialog.dismiss();
								}
							}, getString(R.string.ticket_event_error_message));
				} else {

					if (promoValue != null && !promoValue.equalsIgnoreCase("")) {
						GetPromocodeAsyncTask getPromo = new GetPromocodeAsyncTask(
								getApplicationContext(), new CustomCallback() {
									@Override
									public void isFnished(boolean isSucceed) {
										if (isSucceed) {
											checkAvailability();
										} else {
											AlertHelper
													.showPopup(
															TicketSelectionEventActivity.this,
															new DialogInterface.OnClickListener() {
																@Override
																public void onClick(
																		DialogInterface dialog,
																		int which) {
																	dialog.dismiss();
																}
															},
															getString(R.string.ticket_promo_error_message));
										}
									}
								}, EventId, promoValue);
						getPromo.execute();
					} else {
						checkAvailability();
					}
				}
			}
		});
	}

	private void checkAvailability() {

		for (int j = 0; j < mItemSelectedSelections.size(); j++) {
			mCartItem = new CartItems();
			mCartItem.setId(mItemSelectedSelections.get(j).getId());
			mCartItem.setName(mItemSelectedSelections.get(j).getType());
			mCartItem.setPrice(mItemSelectedSelections.get(j).getPrice());
			mCartItem.setDesc(mItemSelectedSelections.get(j).getDesc());
			mCartItem.setQty(mItemSelectedSelections.get(j).getQty());
			Const.mEventCartItems.add(mCartItem);
		}

		for (int j = 0; j < mItemPTopup.size(); j++) {
			if (mItemPTopup.get(j).getQty() != 0) {
				mCartItem = new CartItems();
				mCartItem.setId(mItemPTopup.get(j).getId());
				mCartItem.setName(mItemPTopup.get(j).getType());
				mCartItem.setPrice(mItemPTopup.get(j).getPrice());
				mCartItem.setDesc(mItemPTopup.get(j).getDesc());
				mCartItem.setQty(mItemPTopup.get(j).getQty());
				Const.mEventCartItems.add(mCartItem);
			}
		}

		EvetnItemids = new ArrayList<Integer>();
		for (int j = 0; j < Const.mEventCartItems.size(); j++) {
			EvetnItemids.add(Const.mEventCartItems.get(j).getId());
		}

	
		// Real code
		AvailableTiketsAsyncTask availableTicket = new AvailableTiketsAsyncTask(
				TicketSelectionEventActivity.this, new CustomCallback() {
					@Override
					public void isFnished(boolean isSucceed) {
						if (isSucceed) {
							startActivityForResult(
									new Intent(
											TicketSelectionEventActivity.this,
											TicketEventAddCartActivity.class)
											.putExtra("EventName", EventName)
											.putExtra("EventDate", EventDate)
											.putExtra("EventId", EventId),
									REQUEST_CODE);
						} else {
							customAlert();
						}
					}
				}, EvetnItemids);
		availableTicket.execute();

		// // Testing code
		// startActivityForResult(
		// new Intent(TicketSelectionEventActivity.this,
		// TicketEventAddCartActivity.class)
		// .putExtra("EventName", EventName)
		// .putExtra("EventDate", EventDate)
		// .putExtra("EventId", EventId)
		// .putExtra("ItemEventId", ItemEventID), REQUEST_CODE);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == REQUEST_CODE && resultCode == RESULT_OK) {
			setResult(RESULT_OK);
			finish();
		}
	}

	private void fillData() {

		mItemDates = new ArrayList<String>();
		mItemID = new ArrayList<Integer>();
		hmData.clear();

		TicketEventDetailItem ticketEventDetail = Const.mTicketsEventdetailItems
				.get(0);
		mTxtNotes.setText(ticketEventDetail.getNotes());
		notes = ticketEventDetail.getNotes();

		for (int i = 0; i < ticketEventDetail.getEventDetail().size(); i++) {

			EventDate = Const.sdf.format(SentosaUtils.getDate(ticketEventDetail
					.getEventDetail().get(i).getEventDate()));
			EventName = ticketEventDetail.getEventDetail().get(i).getName();

			mItemDates.add(EventDate);
			mItemID.add(ticketEventDetail.getEventDetail().get(i).getId());

			hmData.put(
					Const.sdf.format(SentosaUtils.getDate(ticketEventDetail
							.getEventDetail().get(i).getEventDate())),
					ticketEventDetail.getEventDetail().get(i));
		}

		ArrayAdapter<String> adapterDate = new ArrayAdapter<String>(this,
				R.layout.item_spinner, mItemDates);
		spDate.setAdapter(adapterDate);
		spDate.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> parent, View arg1,
					int position, long arg3) {
				EventId = String.valueOf(mItemID.get(position));
				EventDate = parent.getItemAtPosition(position).toString();
				fillValueFromDate(EventDate);
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {

			}
		});
		// fillValueFromDate(spDate.getSelectedItem().toString().trim());
	}
	
	private void setQty(){
		ArrayAdapter<Integer> adapterQty = new ArrayAdapter<Integer>(this,
				R.layout.item_spinner, items);
		spQty.setAdapter(adapterQty);
		spQty.setOnItemSelectedListener(new OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> arg0, View arg1,
					int arg2, long arg3) {
				mItemPSelections.get(spTicket.getSelectedItemPosition())
						.setQty(Integer.parseInt(arg0.getSelectedItem()
								.toString()));
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
			}
		});
	}

	private void fillValueFromDate(String date) {

		setQty();
		ItemPopup itemPop;
		mItemSelection = new ArrayList<String>();
		// mItemTopup = new ArrayList<String>();

		mItemPSelections = new ArrayList<ItemPopup>();
		mItemSelectedSelections = new ArrayList<ItemPopup>();
		mItemPTopup = new ArrayList<ItemPopup>();

		EventDetailsEntry entry = hmData.get(date);
		for (int j = 0; j < entry.getItems().size(); j++) {
			if (entry.getItems().get(j).getDiscountType().contains("Early")
					|| entry.getItems().get(j).getDiscountType()
							.contains("Normal")) {
				ItemEventID = entry.getItems().get(j).getId();
				itemPop = new ItemPopup();
				itemPop.setId(entry.getItems().get(j).getId());
				itemPop.setDesc(entry.getItems().get(j).getDescription());
				itemPop.setPrice(entry.getItems().get(j).getPrice());
				itemPop.setType(entry.getItems().get(j).getDiscountType());
				itemPop.setQty(Integer.parseInt(spQty.getSelectedItem()
						.toString().trim()));
				mItemPSelections.add(itemPop);
				mItemSelection.add(entry.getItems().get(j).getDescription()
						+ " - "
						+ getString(
								R.string.ticket_selection_event_select_ticket,
								SentosaUtils.DoFormat(String.valueOf(entry
										.getItems().get(j).getPrice()))));
			} else {
				itemPop = new ItemPopup();
				itemPop.setId(entry.getItems().get(j).getId());
				itemPop.setDesc(entry.getItems().get(j).getDescription());
				itemPop.setPrice(entry.getItems().get(j).getPrice());
				itemPop.setType(entry.getItems().get(j).getDiscountType());
				itemPop.setQty(entry.getItems().get(j).getQuantity());
				mItemPTopup.add(itemPop);
			}
		}

		
		if (mItemSelection.size() > 0) {
			spTicket = (Spinner) findViewById(R.id.sn_selection);
			ArrayAdapter<String> adapterSelection = new ArrayAdapter<String>(
					this, R.layout.item_spinner, mItemSelection);
			spTicket.setAdapter(adapterSelection);
			spTicket.setOnItemSelectedListener(new OnItemSelectedListener() {
				@Override
				public void onItemSelected(AdapterView<?> arg0, View arg1,
						int arg2, long arg3) {
					mItemSelectedSelections.clear();
					mItemSelectedSelections.add(mItemPSelections.get(arg2));
					setQty();
				}

				@Override
				public void onNothingSelected(AdapterView<?> arg0) {
				}
			});
		}

		addTopup();
	}

	private void addTopup() {

		table.removeAllViews();
		LayoutInflater inflater = getLayoutInflater();

		for (int i = 0; i < mItemPTopup.size(); i++) {
			final int j = i;
			TableRow row = (TableRow) inflater.inflate(
					R.layout.item_event_selection, table, false);
			row.setPadding(0, 10, 0, 10);

			TextView name = (TextView) row.findViewById(R.id.tv_topup_item);
			name.setText(mItemPTopup.get(i).getDesc()
					+ " - "
					+ getString(R.string.ticket_selection_event_select_ticket,
							SentosaUtils.DoFormat(String.valueOf(mItemPTopup
									.get(i).getPrice()))));

			TextView cross = (TextView) row.findViewById(R.id.item_tv_cross);

			Spinner spitemQty = (Spinner) row.findViewById(R.id.item_sn_qty);
			ArrayAdapter<Integer> adapterQty = new ArrayAdapter<Integer>(this,
					R.layout.item_spinner, items);
			spitemQty.setAdapter(adapterQty);
			spitemQty.setOnItemSelectedListener(new OnItemSelectedListener() {
				@Override
				public void onItemSelected(AdapterView<?> arg0, View arg1,
						int arg2, long arg3) {
					mItemPTopup.get(j)
							.setQty(Integer.parseInt(arg0.getSelectedItem()
									.toString()));
				}

				@Override
				public void onNothingSelected(AdapterView<?> arg0) {
				}
			});

			TableRow.LayoutParams params = new TableRow.LayoutParams(
					TableRow.LayoutParams.MATCH_PARENT,
					TableRow.LayoutParams.WRAP_CONTENT);
			table.addView(row, params);
		}
	}

	private void customAlert() {
		sb = new StringBuilder();
		for (int i = 0; i < Const.mEventCartItems.size(); i++) {
			for (int j = 0; j < Const.mFailedTicketsItems.size(); j++) {
				if (Const.mEventCartItems.get(i).getId() == Const.mFailedTicketsItems
						.get(j))
					sb.append(Const.mEventCartItems.get(i).getDesc() + "\n");
			}
		}

		LinearLayout llView = new LinearLayout(this);
		llView.setGravity(Gravity.CENTER);
		llView.setOrientation(LinearLayout.VERTICAL);

		TextView txtMsg = new TextView(this);
		if (Const.mFailedTicketsItems.size() > 1)
			txtMsg.setText(getString(R.string.ticket_are_not_available));
		else
			txtMsg.setText(getString(R.string.ticket_is_not_available));

		txtMsg.setGravity(Gravity.CENTER_HORIZONTAL);
		txtMsg.setPadding(5, 0, 5, 0);
		txtMsg.setTextSize(21);

		TextView txtTickets = new TextView(this);
		txtTickets.setText(sb.toString());
		txtTickets.setGravity(Gravity.CENTER_HORIZONTAL);
		txtTickets.setPadding(5, 0, 5, 0);
		txtTickets.setTextSize(18);

		llView.addView(txtMsg);
		llView.addView(txtTickets);

		AlertDialog.Builder builderCall = new AlertDialog.Builder(this);
		builderCall.setView(llView);
		builderCall.setPositiveButton(getString(R.string.ok),
				new android.content.DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}
				});
		builderCall.show();
	}
}
