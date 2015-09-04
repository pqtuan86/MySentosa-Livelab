package com.mysentosa.android.sg;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.mysentosa.android.sg.asynctask.GetTicketDetailAsyncTask;
import com.mysentosa.android.sg.models.TicketDetailItem;
import com.mysentosa.android.sg.models.TicketDetailItemWrapper;
import com.mysentosa.android.sg.utils.AlertHelper;
import com.mysentosa.android.sg.utils.Const;
import com.mysentosa.android.sg.utils.CustomCallback;
import com.mysentosa.android.sg.utils.SentosaUtils;

public class TicketSelectionActivity extends Activity {

	private TextView mTextNotes;
	private Button btnNext;
	int pos, TicketsCode;
	Integer[] items = new Integer[] { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10 };
	private RelativeLayout mRel_Note;
	private ImageView mImageArrow;
	int REQUEST_CODE = 1;
	boolean isStandard = false;
	ListView list;
//	TicketTypeAdapter adapter;
	TicketWrapperAdapter adapterWrapper;
	ArrayList<TicketDetailItemWrapper> mListWrapper;
	
	private boolean mIsAlive;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.tickets_selection);
		mIsAlive = true;
		pos = getIntent().getIntExtra("Position", 0);
		TicketsCode = getIntent().getIntExtra("TicketsCode", 0);
		initializeViews();
		Const.mTicketsdetailItems.clear();
		GetTicketDetailAsyncTask getAsync = new GetTicketDetailAsyncTask(this,
				new CustomCallback() {
					@Override
					public void isFnished(boolean isSucceed) {
						if (isSucceed) {
//							Collections.sort(Const.mTicketsdetailItems,
//									new CustomComparator());
//							mTextNotes.setText(Const.mTicketsItems.get(pos)
//									.getNotes());
//							adapter.notifyDataSetChanged();
							finishGettingTicketDetailItems();
						}
					}
				}, TicketsCode, Const.mTicketsItems.get(pos).getId());
		getAsync.execute();
	}
	
	@Override
	protected void onResume() {
		mIsAlive = true;
		super.onResume();
	}
	
	@Override
	protected void onPause() {
		mIsAlive = false;
		super.onPause();
	}
	
	public Boolean isAlive() {
		return mIsAlive;
	}

	private void initializeViews() {

		((TextView) findViewById(R.id.header_title))
				.setText("Ticket Selection");

		list = (ListView) findViewById(R.id.lv_selectiontype);
//		adapter = new TicketTypeAdapter(this);
//		list.setAdapter(adapter);
		
		adapterWrapper = new TicketWrapperAdapter(this);
		list.setAdapter(adapterWrapper);

		mTextNotes = (TextView) findViewById(R.id.tv_note);
		mTextNotes.setMovementMethod(new ScrollingMovementMethod());
		mImageArrow = (ImageView) findViewById(R.id.iv_arrow);
		mImageArrow.setBackgroundResource(R.drawable.accordian_arrow_down);
		mRel_Note = (RelativeLayout) findViewById(R.id.rl_note);
		mRel_Note.setTag("down");
		mRel_Note.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (v.getTag().toString().equalsIgnoreCase("down")) {
					v.setTag("up");
					mTextNotes.setVisibility(View.VISIBLE);
					mImageArrow
							.setBackgroundResource(R.drawable.accordian_arrow_up);
				} else {
					v.setTag("down");
					mTextNotes.setVisibility(View.GONE);
					mImageArrow
							.setBackgroundResource(R.drawable.accordian_arrow_down);
				}
			}
		});

		btnNext = (Button) findViewById(R.id.btn_next);
		btnNext.setTypeface(((SentosaApplication) this.getApplication()).myridTypeFace);
		btnNext.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				int value = 0;
				for (TicketDetailItem item : Const.mTicketsdetailItems) {
					if (item.getQuantity() == 0) {
						value++;
					}
				}
				if (value == Const.mTicketsdetailItems.size()) {
					AlertHelper.showErrorPopup(TicketSelectionActivity.this,
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									dialog.dismiss();
								}
							});
				} else {
					addToCart();
				}
			}
		});

	}
	
	private void finishGettingTicketDetailItems() {
		Collections.sort(Const.mTicketsdetailItems, new CustomComparator());
		mTextNotes.setText(Const.mTicketsItems.get(pos).getNotes());
//		adapter.notifyDataSetChanged();
		
		mListWrapper = new ArrayList<TicketDetailItemWrapper>();
		TicketDetailItemWrapper firstHeader = new TicketDetailItemWrapper();
		String currentHeaderContent = Const.mTicketsdetailItems.get(0).getContent();
		
		firstHeader.setType(TicketDetailItemWrapper.TYPE_HEADER);
		firstHeader.setHeaderContent(currentHeaderContent);
		firstHeader.setItem(null);
		mListWrapper.add(firstHeader);
		
		for (int i = 0; i < Const.mTicketsdetailItems.size(); i++) {
			TicketDetailItem item = Const.mTicketsdetailItems.get(i);
			item.setName(Const.mTicketsItems.get(pos).getName());
			TicketDetailItemWrapper wrapper = new TicketDetailItemWrapper();
			
			wrapper.setType(TicketDetailItemWrapper.TYPE_ITEM);
			wrapper.setItem(item);
			wrapper.setHeaderContent(null);
			
			String content = item.getContent();
			if (!content.equals(currentHeaderContent)) {
				currentHeaderContent = content;
				TicketDetailItemWrapper header = new TicketDetailItemWrapper();
				header.setType(TicketDetailItemWrapper.TYPE_HEADER);
				header.setHeaderContent(currentHeaderContent);
				header.setItem(null);
				mListWrapper.add(header);
			}
			
			mListWrapper.add(wrapper);
		}
		adapterWrapper.notifyDataSetChanged();
	}

	private void addToCart() {
		startActivityForResult(new Intent(TicketSelectionActivity.this,
				TicketAddCartActivity.class).putExtra("Position", pos)
				.putExtra("TicketsCode", TicketsCode), REQUEST_CODE);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == REQUEST_CODE && resultCode == RESULT_OK) {
			setResult(RESULT_OK);
			finish();
		}
	}
	
//	public class TicketTypeAdapter extends BaseAdapter {
//
//		private Activity activity;
//		public LayoutInflater inflater = null;
//
//		public TicketTypeAdapter(Activity a) {
//			activity = a;
//			inflater = (LayoutInflater) activity
//					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
//		}
//
//		public int getCount() {
//			return Const.mTicketsdetailItems.size();
//		}
//
//		public Object getItem(int position) {
//			return position;
//		}
//
//		public long getItemId(int position) {
//			return position;
//		}
//
//		public class ViewHolder {
//			public TextView txtTicketType, txtTicketPrice;
//			public Spinner sp_qty;
//		}
//
//		public View getView(final int position, View convertView,
//				ViewGroup parent) {
//
//			View vi = convertView;
//			ViewHolder holder;
//			if (convertView == null) {
//
//				vi = inflater.inflate(R.layout.item_selection, null);
//				holder = new ViewHolder();
//
//				holder.txtTicketType = (TextView) vi
//						.findViewById(R.id.tv_type_name);
//				holder.txtTicketPrice = (TextView) vi
//						.findViewById(R.id.tv_type_price);
//				holder.sp_qty = (Spinner) vi.findViewById(R.id.sn_type);
//
//				vi.setTag(holder);
//			} else
//				holder = (ViewHolder) vi.getTag();
//
//			holder.txtTicketType.setText(Const.mTicketsdetailItems
//					.get(position).getTicketType());
//			holder.txtTicketPrice.setText("($"
//					+ SentosaUtils.DoFormat(String
//							.valueOf(Const.mTicketsdetailItems.get(position)
//									.getPrice())) + ")");
//			holder.txtTicketPrice.setTag(Const.mTicketsdetailItems
//					.get(position).getPrice());
//			ArrayAdapter<Integer> adapterQty = new ArrayAdapter<Integer>(
//					activity, R.layout.item_spinner, items);
//			holder.sp_qty.setAdapter(adapterQty);
//			holder.sp_qty.setSelection((int) Const.mTicketsdetailItems.get(
//					position).getQuantity());
//			holder.sp_qty
//					.setOnItemSelectedListener(new OnItemSelectedListener() {
//						@Override
//						public void onItemSelected(AdapterView<?> parent,
//								View view, int pos, long id) {
//							String selected = parent.getItemAtPosition(pos)
//									.toString();
//							Const.mTicketsdetailItems.get(position)
//									.setQuantity(Long.valueOf(selected));
//						}
//
//						@Override
//						public void onNothingSelected(AdapterView<?> arg0) {
//
//						}
//					});
//			return vi;
//
//		}
//	}

	public class TicketWrapperAdapter extends BaseAdapter {
		
		public static final int TYPE_ITEM = 0;
		public static final int TYPE_HEADER = 1;

		private Activity activity;
		public LayoutInflater inflater = null;

		public TicketWrapperAdapter(Activity a) {
			activity = a;
			inflater = (LayoutInflater) activity
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		}

		public int getCount() {
			if (mListWrapper == null) {
				return 0;
			}
			return mListWrapper.size();
		}

		public Object getItem(int position) {
			return position;
		}

		public long getItemId(int position) {
			return position;
		}

		public class ViewHolder {
			public TextView txtTicketType, txtTicketPrice;
			public Spinner sp_qty;
		}
		
		public class ViewHolderHeader {
			public TextView tvHeader;
		}

		public View getView(final int position, View convertView,
				ViewGroup parent) {
			TicketDetailItemWrapper wrapper = mListWrapper.get(position);
			View vi = convertView;
			if (convertView == null) {

				if (wrapper.getType() == TicketDetailItemWrapper.TYPE_ITEM) {
					vi = inflater.inflate(R.layout.item_selection, null);
					ViewHolder holder = new ViewHolder();
					
					holder.txtTicketType = (TextView) vi
							.findViewById(R.id.tv_type_name);
					holder.txtTicketPrice = (TextView) vi
							.findViewById(R.id.tv_type_price);
					holder.sp_qty = (Spinner) vi.findViewById(R.id.sn_type);
					
					vi.setTag(holder);
				} else {
					vi = inflater.inflate(R.layout.item_selection_header, null);
					ViewHolderHeader header = new ViewHolderHeader();
					header.tvHeader = (TextView) vi.findViewById(R.id.item_selection_header_tv_content);
					vi.setTag(header);
				}
			} 
			
			if (wrapper.type == TicketDetailItemWrapper.TYPE_ITEM) {
				final TicketDetailItem item = wrapper.getItem();
				if (!(vi.getTag() instanceof ViewHolder)) {
					vi = inflater.inflate(R.layout.item_selection, null);
					ViewHolder holder = new ViewHolder();
					
					holder.txtTicketType = (TextView) vi
							.findViewById(R.id.tv_type_name);
					holder.txtTicketPrice = (TextView) vi
							.findViewById(R.id.tv_type_price);
					holder.sp_qty = (Spinner) vi.findViewById(R.id.sn_type);
					
					vi.setTag(holder);
				} 
				ViewHolder holder = (ViewHolder) vi.getTag();
				
				holder.txtTicketType.setText(item.getTicketType());
				holder.txtTicketPrice.setText("($"
						+ SentosaUtils.DoFormat(String.valueOf(item.getPrice())) + ")");
				holder.txtTicketPrice.setTag(item.getPrice());
				ArrayAdapter<Integer> adapterQty = new ArrayAdapter<Integer>(
						activity, R.layout.item_spinner, items);
				holder.sp_qty.setAdapter(adapterQty);
				holder.sp_qty.setSelection((int) item.getQuantity());
				holder.sp_qty.setOnItemSelectedListener(new OnItemSelectedListener() {
					@Override
					public void onItemSelected(AdapterView<?> parent,
							View view, int pos, long id) {
						String selected = parent.getItemAtPosition(pos).toString();
						item.setQuantity(Long.valueOf(selected));
					}
					
					@Override
					public void onNothingSelected(AdapterView<?> arg0) {
						
					}
				});
			} else {
				if (!(vi.getTag() instanceof ViewHolderHeader) || vi.getTag() == null) {
					vi = inflater.inflate(R.layout.item_selection_header, null);
					ViewHolderHeader header = new ViewHolderHeader();
					header.tvHeader = (TextView) vi.findViewById(R.id.item_selection_header_tv_content);
					vi.setTag(header);
				}
				ViewHolderHeader header = (ViewHolderHeader) vi.getTag();
				
				header.tvHeader.setText(wrapper.getHeaderContent());
			}

			return vi;

		}
	}

	public class CustomComparator implements Comparator<TicketDetailItem> {
		@Override
		public int compare(TicketDetailItem o1, TicketDetailItem o2) {
			String content1 = o1.getContent();
			String content2 = o2.getContent();
			if (!content1.equals(content2)) {
				return content1.compareTo(content2);
			}
			return o1.getTicketType().compareTo(o2.getTicketType());
		}
	}

}
