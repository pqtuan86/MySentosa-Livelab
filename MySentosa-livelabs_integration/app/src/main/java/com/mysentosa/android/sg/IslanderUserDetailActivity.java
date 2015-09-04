package com.mysentosa.android.sg;

import com.mysentosa.android.sg.models.IslanderUser;
import com.mysentosa.android.sg.utils.SentosaUtils;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

public class IslanderUserDetailActivity extends BaseActivity implements OnClickListener {

    private TextView tvName, tvNRIC, tvDOB, tvEmail, tvMobile, tvAddress, btEdit, tvTitle;
    private IslanderUser user;

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.islander_user_detail);
        initializeViews();
    }

    private void initializeViews() {

        tvTitle = (TextView) findViewById(R.id.header_title);
        tvTitle.setText(R.string.islander_personal_details);

        tvName = (TextView) findViewById(R.id.tv_detail_name);
        tvNRIC = (TextView) findViewById(R.id.tv_detail_account_id);
        tvDOB = (TextView) findViewById(R.id.tv_detail_dob);
        tvEmail = (TextView) findViewById(R.id.tv_detail_email);
        tvMobile = (TextView) findViewById(R.id.tv_detail_mobile);
        tvAddress = (TextView) findViewById(R.id.tv_detail_address);

        btEdit = (TextView) findViewById(R.id.bt_text_right);
        btEdit.setVisibility(TextView.VISIBLE);
        btEdit.setText(R.string.islander_edit);

        btEdit.setOnClickListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        initData();
    }

    @Override
    public void onClick(View v) {
        Intent intent;
        switch (v.getId()) {
        case R.id.bt_text_right:
            // Edit Button
            intent = new Intent(IslanderUserDetailActivity.this, IslanderEditProfileActivity.class);
            startActivity(intent);
            break;

        default:
            break;
        }

    }

    private void initData() {
        if (SentosaApplication.mCurrentIslanderUser != null) {
            user = SentosaApplication.mCurrentIslanderUser;

            tvName.setText(user.getFullName());
            tvNRIC.setText(user.getNRICNumber()+"");
            tvDOB.setText(SentosaUtils.getUserDOB(this));
            tvEmail.setText(user.getEmail());
            tvMobile.setText(user.getMobilePhone());
            tvAddress.setText(user.getAddress());
        } else {
            // if can not get data, move back to IslanderActivity, it'll re-load
            // data
            finish();
        }
    }
}
