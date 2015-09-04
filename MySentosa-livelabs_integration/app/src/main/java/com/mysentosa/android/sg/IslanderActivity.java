package com.mysentosa.android.sg;

import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.widget.Button;
import android.widget.TextView;

import com.flurry.android.FlurryAgent;
import com.google.analytics.tracking.android.EasyTracker;
import com.google.analytics.tracking.android.MapBuilder;
import com.mysentosa.android.sg.utils.Const;
import com.mysentosa.android.sg.utils.Const.FlurryStrings;
import com.mysentosa.android.sg.utils.SentosaUtils;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

public class IslanderActivity extends BaseActivity {

    public static final String TAG = IslanderActivity.class.getSimpleName();

    @InjectView(R.id.tv_title_islander) TextView title;
    @InjectView(R.id.tv_link_islander) TextView link;
    @InjectView(R.id.bt_login_now) Button login;

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.islander_welcome);
        ButterKnife.inject(this);

        checkAccessTokenAndMoveToUserProfile();
        link.setText(Html.fromHtml("<html><u>" + getString(R.string.islander_title_link) + "</u></html>"));
    }

    @OnClick(R.id.tv_link_islander)
    public void seePrivileges() {
        FlurryAgent.logEvent(FlurryStrings.IslanderPrivileges);

        Intent intent = new Intent(this, WebPageActivity.class);
        intent.putExtra(WebPageActivity.PAGE_TYPE, WebPageActivity.PAGE_TYPE_ISLANDER);

        startActivity(intent);
    }

    @OnClick(R.id.bt_login_now)
    public void islanderLogin() {
        EasyTracker easyTracker = EasyTracker.getInstance(IslanderActivity.this);
        easyTracker.send(MapBuilder.createEvent(Const.GAStrings.ISLANDER_EVENT_CATEGORY, Const.GAStrings.ISLANDER_LOGIN, "", null).build());
        startActivityForResult(new Intent(IslanderActivity.this, IslanderLoginActivity.class), 0);
    }

    @Override
    protected void onActivityResult(int arg0, int arg1, Intent arg2) {
        super.onActivityResult(arg0, arg1, arg2);
        checkAccessTokenAndMoveToUserProfile();
    }

    private void checkAccessTokenAndMoveToUserProfile() {
        if (SentosaUtils.isUserLogined(IslanderActivity.this)) {
            startActivity(new Intent(this, IslanderUserProfileActivity.class));
            finish();
        }
    }
}