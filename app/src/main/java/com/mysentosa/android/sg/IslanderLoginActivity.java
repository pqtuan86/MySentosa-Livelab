package com.mysentosa.android.sg;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.DatePickerDialog.OnDateSetListener;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Html;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Response.Listener;
import com.flurry.android.FlurryAgent;
import com.mysentosa.android.sg.models.IslanderUser;
import com.mysentosa.android.sg.request.LoginRequest;
import com.mysentosa.android.sg.utils.Const.FlurryStrings;
import com.mysentosa.android.sg.utils.Const;
import com.mysentosa.android.sg.utils.SentosaUtils;

public class IslanderLoginActivity extends BaseActivity implements OnClickListener {

    private TextView tvID, tvDOB;
    private EditText etID, etDOB;
    private Button btLogin;
    private DatePickerDialog datePickerDialog;
    private SharedPreferences mPrefs;

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.islander_login);
        initializeViews();
    }

    private void initializeViews() {
        tvID = (TextView) findViewById(R.id.tv_id);
        tvID.setTypeface(((SentosaApplication) this.getApplication()).myridTypeFace);

        tvDOB = (TextView) findViewById(R.id.tv_dob);
        tvDOB.setTypeface(((SentosaApplication) this.getApplication()).myridTypeFace);


        btLogin = (Button) findViewById(R.id.bt_login);

        etID = (EditText) findViewById(R.id.et_id);
        etDOB = (EditText) findViewById(R.id.et_dob);

        btLogin.setOnClickListener(this);
        etDOB.setOnClickListener(this);
        
        datePickerDialog = new DatePickerDialog(this, dateSetListener, 1990, 0, 1);
        datePickerDialog.setTitle(R.string.islander_dob);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
        case R.id.bt_login:
            String id = etID.getText().toString().trim();
            String dob = etDOB.getText().toString().trim();
            boolean validateSuccess = validate(id, dob);
            if (validateSuccess) {
                login(id, dob);
            }
            
            break;
            
        case R.id.et_dob:
            datePickerDialog.show();
            break;
        default:
            break;
        }

    }

    private boolean validate(String id, String dob) {
        if (!SentosaUtils.isValidString(id)) {
            showErrorDialog("MembershipID is invalid");
            return false;
        }
        if (!SentosaUtils.isValidString(dob)) {
            showErrorDialog("Date of birth is invalid");
            return false;
        }
        return true;
    }
    
    private void login(String accountNumber, String birthday) {
        mPBLoading.show();
        LoginRequest request = new LoginRequest(this, accountNumber, birthday, mResponseListener, mErrorListener);
        SentosaApplication.mRequestQueue.add(request);
    }
    
    private OnDateSetListener dateSetListener = new OnDateSetListener() {
        
        @Override
        public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yy",Locale.US);
            Calendar calendar = Calendar.getInstance();
            calendar.set(year, monthOfYear, dayOfMonth);
            String pickedDate = dateFormat.format(calendar.getTime());
            etDOB.setText(pickedDate);
            Log.d("2359","Picked date: "+pickedDate);
        }
    };
    
    Listener<IslanderUser> mResponseListener = new Listener<IslanderUser>() {

        @Override
        public void onResponse(IslanderUser user) {
            dismissProgressDialog();
            if (user != null) {
                Toast.makeText(IslanderLoginActivity.this, "Login Successful", Toast.LENGTH_SHORT).show();
                SentosaApplication.mCurrentIslanderUser = user;
                
                SentosaUtils.saveAccessToken(IslanderLoginActivity.this, user.getToken());
                Log.d("2359","Token: "+user.getToken());
                SentosaUtils.saveMemberID(IslanderLoginActivity.this, user.getId());
                Log.d("2359","UserID: "+user.getId());
                SentosaUtils.saveUserDOB(IslanderLoginActivity.this, etDOB.getText().toString());
                Log.d("2359","DOB: "+etDOB.getText().toString());
                
                //Add the Islander tag to Urban Airship Push Notification
                SentosaUtils.addIslanderTag();
                
                mPrefs = getSharedPreferences(Const.SHARED_PREFERENCE, Context.MODE_PRIVATE);
                boolean firstLogin = mPrefs.getBoolean(Const.FIRST_TIME_LOGIN, true);
                
                if (firstLogin) {
                    mPrefs.edit().putBoolean(Const.FIRST_TIME_LOGIN, false).commit();
                    showDialog();
                } else {
                    finish();
                }
                
            } else {
                String message;
                if (SentosaUtils.isValidString(SentosaUtils.errorMessage)) {
                    message = SentosaUtils.errorMessage;
                    SentosaUtils.errorMessage = "";
                } else {
                    //In case we can not get Error message from server
                    message = "Please recheck Membership ID and Date of birth.";
                }
                showErrorDialog(message);
            }
            
        }
    };

    private void showDialog() {
        final AlertDialog ad = new AlertDialog.Builder(this).create();
        ad.setCancelable(false);
        android.content.DialogInterface.OnClickListener buttonClick = new android.content.DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                ad.dismiss();
                finish();
            }
        };
        ad.setButton(AlertDialog.BUTTON_POSITIVE, "Agree", buttonClick);
        ad.setTitle("");
        ad.setMessage("");
        ad.show();
        TextView messageText = (TextView) ad.findViewById(android.R.id.message);
        messageText.setGravity(Gravity.CENTER);
        
        String message = getString(R.string.islander_first_time_login);
        int startPositionOfLink = message.indexOf(getString(R.string.terms_conditions));
        int endPositionOfLink = startPositionOfLink + getString(R.string.terms_conditions).length();
        
        SpannableString ss = new SpannableString(message);
        
        ClickableSpan clickableSpan = new ClickableSpan() {
            @Override
            public void onClick(View textView) {
                Intent intent = new Intent(IslanderLoginActivity.this, WebPageActivity.class);
                intent.putExtra(WebPageActivity.PAGE_TYPE, WebPageActivity.PAGE_TERMS_CONDITIONS);
                startActivity(intent);
            }
        };
        ss.setSpan(clickableSpan, startPositionOfLink, endPositionOfLink, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        
        messageText.setText(ss);
        messageText.setMovementMethod(LinkMovementMethod.getInstance());
    }
}
