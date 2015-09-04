package com.mysentosa.android.sg;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Response.Listener;
import com.mysentosa.android.sg.models.IslanderUser;
import com.mysentosa.android.sg.request.UpdateIslanderUserDetailRequest;
import com.mysentosa.android.sg.utils.SentosaUtils;

public class IslanderEditProfileActivity extends BaseActivity implements OnClickListener {

    private EditText etEmail, etMobile, etAddress;
    private TextView btSave, tvTitle;
    private IslanderUser user;

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.islander_edit_profile);
        initializeViews();
    }

    private void initializeViews() {

        tvTitle = (TextView) findViewById(R.id.header_title);
        tvTitle.setText(R.string.islander_edit_profile);

        etEmail = (EditText) findViewById(R.id.et_email);
        etMobile = (EditText) findViewById(R.id.et_mobile);
        etAddress = (EditText) findViewById(R.id.et_address);

        btSave = (TextView) findViewById(R.id.bt_text_right);
        btSave.setVisibility(TextView.VISIBLE);
        btSave.setText(R.string.islander_save);

        btSave.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        Intent intent;
        switch (v.getId()) {
        case R.id.bt_text_right:
            // Save Button
            mPBLoading.show();
            UpdateIslanderUserDetailRequest request = new UpdateIslanderUserDetailRequest(
                    IslanderEditProfileActivity.this, SentosaUtils.getMemberID(IslanderEditProfileActivity.this),
                    SentosaUtils.getAccessToken(IslanderEditProfileActivity.this), etEmail.getText().toString(),
                    etAddress.getText().toString(), etMobile.getText().toString(), mResponseListener, mErrorListener);
            SentosaApplication.mRequestQueue.add(request);
            break;

        default:
            break;
        }

    }

    private void initData() {
        if (SentosaApplication.mCurrentIslanderUser != null) {
            user = SentosaApplication.mCurrentIslanderUser;

            etEmail.setText(user.getEmail());
            etMobile.setText(user.getMobilePhone());
            etAddress.setText(user.getAddress());
        } else {
            // if can not get data, move back to IslanderActivity, it'll re-load
            // data
            finish();
        }
    }

    Listener<IslanderUser> mResponseListener = new Listener<IslanderUser>() {

        @Override
        public void onResponse(IslanderUser user) {
            dismissProgressDialog();
            if (user != null) {
                Toast.makeText(IslanderEditProfileActivity.this, "Update Successfull", Toast.LENGTH_LONG).show();
                SentosaApplication.mCurrentIslanderUser = user;
                finish();
            } else {
                String message;
                if (SentosaUtils.isValidString(SentosaUtils.errorMessage)) {
                    message = SentosaUtils.errorMessage;
                    SentosaUtils.errorMessage = "";
                } else {
                    // In case we can not get Error message from server
                    message = "Please recheck your information.";
                }
                showErrorDialog(message);
            }

        }
    };
    
    @Override
    protected void onResume() {
        super.onResume();
        initData();
    }
}
