package com.mysentosa.android.sg;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.mysentosa.android.sg.asynctask.GetLocationIdAsyncTask;
import com.mysentosa.android.sg.custom_views.AspectRatioImageView;
import com.mysentosa.android.sg.imageloader.ImageFetcher;
import com.mysentosa.android.sg.utils.Const;
import com.mysentosa.android.sg.utils.HttpHelper;
import com.mysentosa.android.sg.utils.SentosaUtils;

public class TicketDetailActivity extends Activity {

	private AspectRatioImageView imageView;
	private TextView mTxtTilte, mTxtDetail;
	Button btnPurchase;
	int pos, TicketsCode;
	private ImageFetcher mImageWorker;
	private ProgressBar mProgress;
	int REQUEST_CODE = 1;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.tickets_detail);
		mImageWorker = ((SentosaApplication) this.getApplication()).mImageFetcher;
		pos = getIntent().getIntExtra("Position", 0);
		TicketsCode = getIntent().getIntExtra("TicketsCode", 0);		
		initializeViews();	
		
		if (TicketsCode == Const.ATTRACTION_TICKET_TYPE_CODE) {
			GetLocationIdAsyncTask getLocation = new GetLocationIdAsyncTask(this,
					Const.mTicketsItems.get(pos).getName());
			getLocation.execute();
		}		
	}

	private void initializeViews() {

	    if (Const.mTicketsItems.get(pos).isIslanderExclusive()) {
	        LinearLayout titleLayout = (LinearLayout) findViewById(R.id.layout_title_exclusive);
	        titleLayout.setVisibility(LinearLayout.VISIBLE);
	        TextView tvIslanderTitle = (TextView) findViewById(R.id.tv_islander_exclusive);
	        tvIslanderTitle.setTypeface(((SentosaApplication) this.getApplication()).myridTypeFace);
	    }
	    
		mProgress = (ProgressBar) findViewById(R.id.pb_loading);
		imageView = (AspectRatioImageView) findViewById(R.id.iv_detail_item);

		String imageURL = Const.mTicketsItems.get(pos).getImage();
		if (SentosaUtils.isValidString(imageURL)) {
		    mImageWorker.loadImage(
	                HttpHelper.BASE_HOST + imageURL,
	                imageView, mProgress, R.drawable.bg_gradient_img, false, null);
		}
		

		mTxtTilte = (TextView) findViewById(R.id.tv_title);
		mTxtTilte.setText(Const.mTicketsItems.get(pos).getName());

		mTxtDetail = (TextView) findViewById(R.id.tv_description);
		mTxtDetail.setText(Const.mTicketsItems.get(pos).getDescription());
		mTxtDetail.setMovementMethod(new ScrollingMovementMethod());

		btnPurchase = (Button) findViewById(R.id.btn_purchase);
		btnPurchase
				.setTypeface(((SentosaApplication) this.getApplication()).myridTypeFace);
		btnPurchase.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (TicketsCode == Const.EVENT_TICKET_TYPE_CODE) {
					startActivityForResult(
							new Intent(TicketDetailActivity.this,
									TicketSelectionEventActivity.class)
									.putExtra("Position", pos).putExtra(
											"TicketsCode", TicketsCode),
							REQUEST_CODE);
				} else {
					startActivityForResult(
							new Intent(TicketDetailActivity.this,
									TicketSelectionActivity.class).putExtra(
									"Position", pos).putExtra("TicketsCode",
									TicketsCode), REQUEST_CODE);
				}
			}
		});

	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == REQUEST_CODE && resultCode == RESULT_OK) {
			setResult(RESULT_OK);
			finish();
		}
	}

}
