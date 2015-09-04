package com.mysentosa.android.sg;

import java.io.IOException;
import java.io.InputStream;
import java.util.Calendar;
import java.util.regex.Pattern;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.flurry.android.FlurryAgent;
import com.google.analytics.tracking.android.EasyTracker;
import com.google.analytics.tracking.android.Fields;
import com.google.analytics.tracking.android.MapBuilder;
import com.mysentosa.android.sg.utils.Const;
import com.mysentosa.android.sg.utils.Const.FlurryStrings;

public class TicketCheckoutActivity extends Activity implements OnClickListener {

	private EditText edName;
	private EditText edPassport;
	private Button btBirth;
	private EditText edEamil;
	private Spinner snNation;
	private EditText edMobile;
	private ImageView ivTerms;
	private TextView tvTerms;
	private ImageView ivLetter;
	private Button btnNext;
	public static Pattern EMAIL_ADDRESS_PATTERN = Pattern
			.compile("[a-zA-Z0-9\\+\\.\\_\\%\\-\\+]{1,256}" + "\\@"
					+ "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,64}" + "(" + "\\."
					+ "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,25}" + ")+");
	// DATE OF BIRTH
	private Calendar mCalen;
	private int day;
	private int month;
	private int year;
	private static final int DATE_PICKER_ID = 0;
	MyAdapter adapter;
	int REQUEST_SHOPING_CODE = 2;
	SharedPreferences mPrefs;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		FlurryAgent.logEvent(FlurryStrings.CheckoutPage);
		setContentView(R.layout.ticket_checkout);
		mPrefs = getSharedPreferences(
				ProfileAndSettingsActivity.USER_DETAILS_PREFS, MODE_PRIVATE);
		findViews();
		
		EasyTracker easyTracker = EasyTracker.getInstance(this);
		easyTracker.set(Fields.SCREEN_NAME, Const.GAStrings.CHECKOUT);
	    easyTracker.send(MapBuilder
		      .createAppView()
		      .build()
	    );
	}

	private void findViews() {
		edName = (EditText) findViewById(R.id.ed_name);
		edName.setText(mPrefs.getString(
				ProfileAndSettingsActivity.USER_DETAILS_PREFS_NAME, ""));
		edPassport = (EditText) findViewById(R.id.ed_passport);
		edPassport.setText(mPrefs.getString(
				ProfileAndSettingsActivity.USER_DETAILS_PREFS_PASSPORT_NO, ""));
		// DATE OF BIRHT
		btBirth = (Button) findViewById(R.id.bt_birth);
		if (mPrefs.getString(ProfileAndSettingsActivity.USER_DETAILS_PREFS_DOB,
				"") != "") {
			btBirth.setText(mPrefs.getString(
					ProfileAndSettingsActivity.USER_DETAILS_PREFS_DOB, "").replace("/", "-"));
		}
		btBirth.setOnClickListener(this);

		mCalen = Calendar.getInstance();
		day = mCalen.get(Calendar.DAY_OF_MONTH);
		month = mCalen.get(Calendar.MONTH);
		year = mCalen.get(Calendar.YEAR);

		edEamil = (EditText) findViewById(R.id.ed_eamil);
		edEamil.setText(mPrefs.getString(
				ProfileAndSettingsActivity.USER_DETAILS_PREFS_EMAIL, ""));

		snNation = (Spinner) findViewById(R.id.sn_nation);
		adapter = new MyAdapter(this, getResources().getStringArray(
				R.array.nationality));
		snNation.setAdapter(adapter);

		edMobile = (EditText) findViewById(R.id.ed_mobile);
		edMobile.setText(mPrefs.getString(
				ProfileAndSettingsActivity.USER_DETAILS_PREFS_MOBILE, ""));

		ivTerms = (ImageView) findViewById(R.id.iv_terms);
		ivTerms.setTag("false");
		ivTerms.setOnClickListener(this);
		setBackground(ivTerms);
		tvTerms = (TextView) findViewById(R.id.tv_terms);
		tvTerms.setOnClickListener(this);
		ivLetter = (ImageView) findViewById(R.id.iv_letter);
		ivLetter.setOnClickListener(this);
		ivLetter.setTag("false");
		setBackground(ivLetter);

		btnNext = (Button) findViewById(R.id.btn_next);
		btnNext.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		if (v == btnNext) {
			if (getTextNull(edName)) {
				if (getTextNull(edPassport)) {
					if (getTextNull(edEamil)
							&& emailValidation(getText(edEamil))) {
						if (getTextNull(edMobile) && edMobile.getText().toString().length() > 4) {
							if (ivTerms.getTag().equals("true")) {
								storeData();
								startActivityForResult(new Intent(
										TicketCheckoutActivity.this,
										TicketPaymentActivity.class),
										REQUEST_SHOPING_CODE);
							} else {
								displayToast(getString(R.string.val_terms));
							}
						} else {
							displayToast(getString(R.string.val_mob));
						}
					} else {
						displayToast(getString(R.string.val_email));
					}
				} else {
					displayToast(getString(R.string.val_passport));
				}
			} else {
				displayToast(getString(R.string.val_name));
			}
		} else if (v == ivTerms) {
			if (ivTerms.getTag().equals("true")) {
				ivTerms.setTag("false");
			} else {
				ivTerms.setTag("true");
			}
			setBackground(ivTerms);
		} else if (v == tvTerms) {
			startActivity(new Intent(TicketCheckoutActivity.this,
					TicketTermsActivity.class));
		} else if (v == ivLetter) {
			if (ivLetter.getTag().equals("true")) {
				ivLetter.setTag("false");
			} else {
				ivLetter.setTag("true");
			}
			setBackground(ivLetter);
		} else if (v == btBirth) {
			showDialog(DATE_PICKER_ID);
		}
	}

	private String getText(EditText ed) {
		return ed.getText().toString().trim();
	}

	private String getButtonText(Button btn) {
		return btn.getText().toString().trim();
	}

	private boolean getTextNull(EditText ed) {
		if (getText(ed) != null && getText(ed) != ""
				&& getText(ed).length() > 0)
			return true;
		else
			return false;
	}

	private void displayToast(String text) {
		Toast.makeText(getApplicationContext(), text, Toast.LENGTH_SHORT)
				.show();
	}

	private boolean emailValidation(String text) {
		return EMAIL_ADDRESS_PATTERN.matcher(text).matches();
	}

	private void setBackground(ImageView iv) {
		iv.setBackgroundResource(Boolean.parseBoolean(iv.getTag().toString()) ? R.drawable.checkbox_yes_checked
				: R.drawable.checkbox_not_checked);
	}

	// date picker
	private DatePickerDialog.OnDateSetListener datePickerListener = new DatePickerDialog.OnDateSetListener() {

		// while dialog box is closed, below method is called.
		public void onDateSet(DatePicker view, int selectedYear,
				int selectedMonth, int selectedDay) {
			year = selectedYear;
			month = selectedMonth;
			day = selectedDay;

			// Set the Date String in Button
			btBirth.setText(day + "-" + (month + 1) + "-" + year);
		}
	};

	@Override
	@Deprecated
	protected Dialog onCreateDialog(int id) {

		switch (id) {
		case DATE_PICKER_ID:
			return new DatePickerDialog(this, datePickerListener, year, month,
					day);
		}
		return null;
	}

	public class MyAdapter extends ArrayAdapter<String> {

		String myCountry[];

		public MyAdapter(Context context, String[] objects) {
			super(context, 0, objects);
			myCountry = objects;
		}

		@Override
		public View getDropDownView(int position, View convertView,
				ViewGroup parent) {
			return getCustomView(position, convertView, parent);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			return getCustomView(position, convertView, parent);
		}

		public View getCustomView(int position, View convertView,
				ViewGroup parent) {

			LayoutInflater inflater = getLayoutInflater();
			View row = inflater.inflate(R.layout.item_spinner_checkout, parent,
					false);
			TextView label = (TextView) row.findViewById(R.id.tv_spinner_text);
			label.setText(myCountry[position]);
			ImageView icon = (ImageView) row.findViewById(R.id.iv_icon);
			icon.setImageBitmap(getBitmapFromAsset(
					myCountry[position] + ".png", "nationality"));
			return row;
		}
	}

	private Bitmap getBitmapFromAsset(String strName, String folder) {
		AssetManager assetManager = getAssets();
		InputStream istr = null;
		String path = folder + "/" + strName.trim().replaceAll(" ", "_");
		try {
			istr = assetManager.open(path);
		} catch (IOException e) {
			e.printStackTrace();
		}
		Bitmap bitmap = BitmapFactory.decodeStream(istr);
		return bitmap;
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

	private void storeData() {
		SharedPreferences sharedPref = PreferenceManager
				.getDefaultSharedPreferences(this);
		Editor edit1 = sharedPref.edit();
		edit1.putString("NRIC", getText(edPassport));
		edit1.putString("NATIONALITY", snNation.getSelectedItem().toString());
		edit1.putString("NEWS", ivLetter.getTag().toString());
		edit1.commit();

		final SharedPreferences.Editor edit = mPrefs.edit();
		edit.putString(ProfileAndSettingsActivity.USER_DETAILS_PREFS_NAME,
				getText(edName));
		edit.putString(ProfileAndSettingsActivity.USER_DETAILS_PREFS_EMAIL,
				getText(edEamil));
		edit.putString(ProfileAndSettingsActivity.USER_DETAILS_PREFS_MOBILE,
				getText(edMobile));
		edit.putString(ProfileAndSettingsActivity.USER_DETAILS_PREFS_PASSPORT_NO,
				getText(edPassport));
		
		if (getButtonText(btBirth) != "" && getButtonText(btBirth) != null
				&& !getButtonText(btBirth).equals("")) {
		    edit.putString(ProfileAndSettingsActivity.USER_DETAILS_PREFS_DOB,
                    getButtonText(btBirth).toString().replace("-", "/"));
		}
		edit.putBoolean(
				ProfileAndSettingsActivity.USER_DETAILS_PREFS_ENTRY_CREATED,
				true);
		edit.commit();

	}
}
