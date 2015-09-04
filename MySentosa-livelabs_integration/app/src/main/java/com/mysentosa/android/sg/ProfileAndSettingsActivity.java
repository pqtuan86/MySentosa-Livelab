package com.mysentosa.android.sg;


import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.DatePickerDialog.OnDateSetListener;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.URLSpan;
import android.text.style.UnderlineSpan;
import android.text.util.Linkify;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.flurry.android.FlurryAgent;
import com.mysentosa.android.sg.custom_views.ToggleGroup;
import com.mysentosa.android.sg.utils.Const.FlurryStrings;
import com.mysentosa.android.sg.utils.AlertHelper;
import com.mysentosa.android.sg.utils.HttpHelper;
import com.mysentosa.android.sg.utils.SentosaUtils;

import butterknife.ButterKnife;
import sg.edu.smu.livelabs.integration.LiveLabsApi;


public class ProfileAndSettingsActivity extends BaseActivity implements OnClickListener {

    public static final String TAG = ProfileAndSettingsActivity.class.getSimpleName();

	public static final String USER_DETAILS_PREFS = "com.mysentosa.android.sg.user.prefs";
	public static final String USER_DETAILS_PREFS_ENTRY_CREATED = "ENTRY_CREATED";
	public static final String USER_DETAILS_PREFS_EMAIL = "EMAIL";
	public static final String USER_DETAILS_PREFS_MOBILE = "MOBILE";
	public static final String USER_DETAILS_PREFS_POSTAL_CODE = "POSTAL_CODE";
	public static final String USER_DETAILS_PREFS_PASSPORT_NO = "PASSPORT_CODE";
	public static final String USER_DETAILS_PREFS_GENDER = "GENDER", USER_DETAILS_PREFS_GENDER_MALE = "male", USER_DETAILS_PREFS_GENDER_FEMALE = "female";
	public static final String USER_DETAILS_PREFS_NAME = "NAME";
	public static final String USER_DETAILS_PREFS_DOB = "DDMMYYY";
	public static final String USER_DETAILS_PREFS_IS_REGISTERED = "IS_REGISTERED";
	
	public static final String FROM_FIRST_LAUNCH = "from_first_launch";
	private boolean fromSplash;
	private int eventId = -1;
	public static final String SECRET_KEY = "2b63ead0-98d5-11e1-a8b0-0800200c9a66";
	private static final String VISITORS = "Visitors";
	private static final int REGISTER_SUCCESS = 0;	
	private SharedPreferences mPrefs;
	
	private EditText etName,etEmail,etMobile,etPostalCode, etDOB;
	private ToggleGroup tgGender;
	private DatePickerDialog datePickerDialog;
	
	@Override
	public void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.user_details_screen);
		fromSplash = this.getIntent().getBooleanExtra(FROM_FIRST_LAUNCH, false);
        Log.d(TAG, "fromSplash: " + fromSplash);

		eventId = this.getIntent().getIntExtra(EventsAndPromotionsDetailActivity.ID, -1);
		initializeViews();
		setContent();
	}
	
	@Override
	protected void onStart() {
		super.onStart();
		if(fromSplash) {
			customMenu.getMenu().setVisibility(View.GONE);
			showNotificationAlertDialog();
		} else {
			findViewById(R.id.btn_skip).setVisibility(View.GONE);
		}

	}
	
	private void initializeViews() {
		((TextView) findViewById(R.id.header_title)).setText("My Profile");
		etName = (EditText) findViewById(R.id.tv_uds_name);
		etEmail = (EditText) findViewById(R.id.tv_uds_email);
		etMobile = (EditText) findViewById(R.id.tv_uds_mobile);
		etPostalCode = (EditText) findViewById(R.id.tv_uds_postalcode);
		tgGender = (ToggleGroup) findViewById(R.id.tg_uds_gender);
		
		etDOB = (EditText) findViewById(R.id.et_dob);
		etDOB.setOnClickListener(this);
		
		initDatePicker(1990, 1, 1);
		
		findViewById(R.id.btn_skip).setOnClickListener(this);
		
		findViewById(R.id.btn_next).setOnClickListener(this);
		
		mPrefs = getSharedPreferences(USER_DETAILS_PREFS, MODE_PRIVATE);
	}
	
	
	private void setContent() {
		String name  = mPrefs.getString(ProfileAndSettingsActivity.USER_DETAILS_PREFS_NAME, null);
		if(SentosaUtils.isValidString(name))
			etName.setText(name);
		
		String email = mPrefs.getString(ProfileAndSettingsActivity.USER_DETAILS_PREFS_EMAIL, null);
		if(SentosaUtils.isValidString(email))
			etEmail.setText(email);
		
		String mobile = mPrefs.getString(ProfileAndSettingsActivity.USER_DETAILS_PREFS_MOBILE, null);
		if(SentosaUtils.isValidString(mobile))
			etMobile.setText(mobile);		
		
		String gender = mPrefs.getString(ProfileAndSettingsActivity.USER_DETAILS_PREFS_GENDER, null);
		if(SentosaUtils.isValidString(gender)) {
			if(gender.equals(USER_DETAILS_PREFS_GENDER_MALE))
				((ToggleButton)findViewById(R.id.tb_uds_male)).setChecked(true);
			else	
				((ToggleButton)findViewById(R.id.tb_uds_female)).setChecked(true);
		}
		
		String dob = mPrefs.getString(ProfileAndSettingsActivity.USER_DETAILS_PREFS_DOB, null);
		if(SentosaUtils.isValidString(dob)) {
		    String[] date = dob.split("/");
		    initDatePicker(Integer.parseInt(date[2]), Integer.parseInt(date[1]), Integer.parseInt(date[0]));
		    etDOB.setText(dob);
		}
			
		String postalCode = mPrefs.getString(ProfileAndSettingsActivity.USER_DETAILS_PREFS_POSTAL_CODE, null);
		if(SentosaUtils.isValidString(postalCode))
			etPostalCode.setText(postalCode);
	}
	
	private void evaluateFormAndSubmit() {
	    
	    if (checkRequiredField()) {
	        String name = etName.getText().toString();
	        String email = etEmail.getText().toString();
	        String mobile = etMobile.getText().toString();
	        String postalCode = etPostalCode.getText().toString();
	        String gender = tgGender.getCheckedRadioButtonId()==R.id.tb_uds_male?USER_DETAILS_PREFS_GENDER_MALE:USER_DETAILS_PREFS_GENDER_FEMALE;
	        
	        String birthDate = etDOB.getText().toString();
	        
	        ArrayList<String> errorFields = new ArrayList<String>();

	        Log.d("test","test name: "+name+" "+SentosaUtils.validateString(name, "^[a-z -']+$")+" "+SentosaUtils.validateString(name, "^(\\w[-._+\\w]*\\w@\\w[-._\\w]*\\w\\.\\w{2,3})$"));
	        if(!name.equals("") && !SentosaUtils.validateString(name, "^[A-Za-z -']+$")) {
	            errorFields.add("Name");
	        }
	        if(!email.equals("") && !SentosaUtils.validateString(email, "^(\\w[-._+\\w]*\\w@\\w[-._\\w]*\\w\\.\\w{2,3})$")) {
	            errorFields.add("Email");
	        }
	        if(!mobile.equals("") && !SentosaUtils.validateString(mobile, "^[689]\\d{7}$")) {
	            errorFields.add("Mobile");
	        }
	        if(!postalCode.equals("") && !SentosaUtils.validateString(postalCode, "^[0-9]{6}")) {
	            errorFields.add("Postal Code");
	        }
	        if(!birthDate.equals("\\") && !SentosaUtils.validateString(birthDate, "^(((0[1-9]|[12]\\d|3[01])\\/(0[13578]|1[02])\\/((19|[2-9]\\d)\\d{2}))|((0[1-9]|[12]\\d|30)\\/(0[13456789]|1[012])\\/((19|[2-9]\\d)\\d{2}))|((0[1-9]|1\\d|2[0-8])\\/02\\/((19|[2-9]\\d)\\d{2}))|(29\\/02\\/((1[6-9]|[2-9]\\d)(0[48]|[2468][048]|[13579][26])|((16|[2468][048]|[3579][26])00))))$")) {
	            errorFields.add("Birth Date");
	        }
	        
	        if(errorFields.size()==0) {
	            writeDataToSharedPrefs(name, email, mobile, gender, postalCode, birthDate);
	            registerVisitor(this,name, email, mobile, gender, postalCode, birthDate.replace('/', '-'));
	            if(fromSplash) {
	                launchActivity();
	            }
	            finish();
	        } else {
	            String errorMsg = "Invalid inputs! Please ensure that the following are entered correctly: "+errorFields.get(0);
	            errorFields.remove(0);
	            for(String badField:errorFields) 
	                errorMsg+=", "+badField;
	            Toast.makeText(this, errorMsg, Toast.LENGTH_LONG).show();
	        }
	    } else {
	        Toast.makeText(this, getString(R.string.profile_fill_required), Toast.LENGTH_SHORT).show();
	    }
	}
	
	private void launchActivity() {
		Intent mIntent = new Intent(ProfileAndSettingsActivity.this, NavigationManagerActivity.class);
		if(eventId==-1) {
			mIntent.putExtra(NavigationManagerActivity.ACTIVITY_TO_START, HomeActivity.class.getName());
		} else {
			mIntent.putExtra(NavigationManagerActivity.ACTIVITY_TO_START, EventsAndPromotionsActivity.class.getName());
			mIntent.putExtra(EventsAndPromotionsDetailActivity.ID, eventId);
		}
		ProfileAndSettingsActivity.this.startActivity(mIntent);
	}
	
	public static void registerVisitor(final Activity context,final String name, final String email, final String mobile, final String gender, final String postalCode, final String birthDate) {
		LiveLabsApi.getInstance().userInfoUpdated(name, gender, email, mobile, birthDate, postalCode);

		SharedPreferences mPrefs = context.getSharedPreferences(USER_DETAILS_PREFS, MODE_PRIVATE);
		final SharedPreferences.Editor edit = mPrefs.edit();
		edit.putBoolean(USER_DETAILS_PREFS_IS_REGISTERED, false);
		edit.commit();
		
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				try {
					ArrayList<NameValuePair> params = new ArrayList<NameValuePair> ();
					params.add(new BasicNameValuePair("SecretKey", SECRET_KEY));
					params.add(new BasicNameValuePair("Name", name));
					params.add(new BasicNameValuePair("Email", email));
					params.add(new BasicNameValuePair("Mobile", mobile));
					params.add(new BasicNameValuePair("Gender", gender));
					params.add(new BasicNameValuePair("Birthdate", birthDate));
					params.add(new BasicNameValuePair("Address", postalCode));
					
					String result =  HttpHelper.sendRequestUsingPost(HttpHelper.BASE_ADDRESS+VISITORS, params);
					int statusCode = new JSONObject(result).optInt("StatusCode",-1);
					if(statusCode == REGISTER_SUCCESS) {
						edit.putBoolean(USER_DETAILS_PREFS_IS_REGISTERED, true);
						edit.commit();
						context.runOnUiThread(new Runnable() {
							
							@Override
							public void run() {
								Toast.makeText(SentosaApplication.appInstance, "Information saved successfully!", Toast.LENGTH_SHORT).show();
							}
						});
					}
					else {
						context.runOnUiThread(new Runnable() {
							
							@Override
							public void run() {
								Toast.makeText(SentosaApplication.appInstance, "Sorry there was a problem saving information!", Toast.LENGTH_SHORT).show();
							}
						});
					}
				} catch (Exception e) {
					e.printStackTrace();
					context.runOnUiThread(new Runnable() {
						
						@Override
						public void run() {
							Toast.makeText(SentosaApplication.appInstance, "Sorry there was a problem saving information!", Toast.LENGTH_SHORT).show();
						}
					});
				}	
			}
		}).start();		
	}

	private void writeDataToSharedPrefs(String name, String email, String mobile, String gender, String postalCode, String ddMMyyyy) {
		final SharedPreferences.Editor edit = mPrefs.edit();
		edit.putString(USER_DETAILS_PREFS_NAME, name);
		edit.putString(USER_DETAILS_PREFS_EMAIL, email);
		edit.putString(USER_DETAILS_PREFS_MOBILE, mobile);
		edit.putString(USER_DETAILS_PREFS_POSTAL_CODE, postalCode);
		edit.putString(USER_DETAILS_PREFS_DOB, ddMMyyyy); 
		edit.putString(USER_DETAILS_PREFS_GENDER, gender);
		edit.putBoolean(USER_DETAILS_PREFS_ENTRY_CREATED, true);
		edit.commit();

	}
	
    private void showNotificationAlertDialog() {
        String content = getString(R.string.notification_alert_detail);
        String here = getString(R.string.here);

        SpannableString spannableString = new SpannableString(content);
        int indexOfSearchQuery = indexOfSearchQuery(content, here);
        if(indexOfSearchQuery > -1) {
            ClickableSpan clickableSpan = new ClickableSpan() {
                @Override
                public void onClick(View widget) {
                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.protection_policy_url)));
                    startActivity(browserIntent);
                }
            };
            spannableString.setSpan(clickableSpan, indexOfSearchQuery, indexOfSearchQuery + here.length(), 0);

            UnderlineSpan underlinedSpan = new UnderlineSpan();
            spannableString.setSpan(underlinedSpan, indexOfSearchQuery, indexOfSearchQuery + here.length(), 0);
        }

        View view = LayoutInflater.from(getApplicationContext()).inflate(R.layout.dialog_text, null);
        TextView textView = ButterKnife.findById(view, R.id.text);
        textView.setText(spannableString);
        textView.setMovementMethod(LinkMovementMethod.getInstance());

        AlertDialog alertDialog = new AlertDialog.Builder(ProfileAndSettingsActivity.this).create();
        alertDialog.setTitle(getString(R.string.notification_alert_title));
        alertDialog.setView(view);
        alertDialog.setButton(DialogInterface.BUTTON_POSITIVE, getString(R.string.ok), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        alertDialog.show();
    }
    
    private OnDateSetListener dateSetListener = new OnDateSetListener() {
        
        @Override
        public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy",Locale.US);
            Calendar calendar = Calendar.getInstance();
            calendar.set(year, monthOfYear, dayOfMonth);
            String pickedDate = dateFormat.format(calendar.getTime());
            etDOB.setText(pickedDate);
            etDOB.setError(null);
            Log.d("2359","Picked date: "+pickedDate);
        }
    };

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
        case R.id.btn_skip:
            if(fromSplash) {
                FlurryAgent.logEvent(FlurryStrings.HomePageLaunch);
                launchActivity();
            }
            ProfileAndSettingsActivity.this.finish();
            break;

        case R.id.btn_next:
            evaluateFormAndSubmit();
            break;
            
        case R.id.et_dob:
            datePickerDialog.show();
            break;
        default:
            break;
        }
    }

    private void initDatePicker(int year, int monthOfYear, int dayOfMonth) {
        datePickerDialog = new DatePickerDialog(this, dateSetListener, year, monthOfYear-1, dayOfMonth);
        datePickerDialog.setTitle(R.string.islander_dob);
    }
    
    private boolean checkRequiredField() {
        String errorMessage = "Please fill your ";
        boolean result = true;
        
        if (etName.getText().toString().length() == 0) {
            etName.setError(errorMessage + "Name");
            result = false;
        } else {
            etName.setError(null);
        }
        
        if (etEmail.getText().toString().length() == 0) {
            etEmail.setError(errorMessage + "Email");
            result = false;
        } else {
            etEmail.setError(null);
        }
        
        if (etMobile.getText().toString().length() == 0) {
            etMobile.setError(errorMessage + "Mobile");
            result = false;
        } else {
            etMobile.setError(null);
        }
        
        if (etDOB.getText().toString().length() == 0) {
            etDOB.setError(errorMessage + "Birthdate");
            result = false;
        } else {
            etDOB.setError(null);
        }
        
        if (etPostalCode.getText().toString().length() == 0) {
            etPostalCode.setError(errorMessage + "Postal Code");
            result = false;
        } else {
            etPostalCode.setError(null);
        }
        return result;
    }

    private int indexOfSearchQuery(String content, String contains) {
        if (!TextUtils.isEmpty(contains)) {
            return content.toLowerCase(Locale.getDefault()).indexOf(contains.toLowerCase(Locale.getDefault()));
        }
        return -1;
    }
}
