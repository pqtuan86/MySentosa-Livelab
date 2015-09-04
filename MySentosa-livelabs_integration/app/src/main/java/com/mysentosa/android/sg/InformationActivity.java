package com.mysentosa.android.sg;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.flurry.android.FlurryAgent;
import com.google.analytics.tracking.android.EasyTracker;
import com.google.analytics.tracking.android.Fields;
import com.google.analytics.tracking.android.MapBuilder;
import com.mysentosa.android.sg.fragments.SearchFragment;
import com.mysentosa.android.sg.utils.Const;
import com.mysentosa.android.sg.utils.Const.FlurryStrings;
import com.mysentosa.android.sg.utils.SentosaUtils;

import java.util.HashMap;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

public class InformationActivity extends BaseActivity {

    public static final String TAG = InformationActivity.class.getSimpleName();

    private static final int EMERGENCY_CALL = 0;
    private static final int CONTACT_US = 1;

    @InjectView(R.id.header_title) TextView headerTitle;
    @InjectView(R.id.header_search) ImageView headerSearch;
    @InjectView(R.id.emergency) ImageView emergency;
    @InjectView(R.id.getting_to_sentosa) ImageView toSentosa;
    @InjectView(R.id.getting_around_sentosa) ImageView aroundSentosa;
    @InjectView(R.id.traffic_updates) ImageView trafficUpdates;
    @InjectView(R.id.stay_updated) ImageView stayUpdated;
    @InjectView(R.id.islander_privileges) ImageView islanderPrivileges;
    @InjectView(R.id.contact_us) ImageView contactUs;
    @InjectView(R.id.search_frame) FrameLayout searchFrame;

    private SearchFragment searchFragment;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.information_screen);
        ButterKnife.inject(this);

        headerTitle.setText(getString(R.string.information));
        headerSearch.setVisibility(View.VISIBLE);
        searchFragment = (SearchFragment) getSupportFragmentManager().findFragmentById(R.id.search_fragment);

        EasyTracker easyTracker = EasyTracker.getInstance(this);
        easyTracker.set(Fields.SCREEN_NAME, Const.GAStrings.INFORMATION);
        easyTracker.send(MapBuilder
                        .createAppView()
                        .build()
        );
    }

    @OnClick({R.id.emergency, R.id.getting_to_sentosa, R.id.getting_around_sentosa, R.id.traffic_updates, R.id.stay_updated, R.id.islander_privileges, R.id.contact_us})
    public void clickInformation(View view) {
        Intent intent;

        switch (view.getId()) {
            case R.id.emergency:
                showDialog(EMERGENCY_CALL);
                break;

            case R.id.getting_to_sentosa:
                FlurryAgent.logEvent(FlurryStrings.GettingToSentosa);
                intent = new Intent(this, WebPageActivity.class);
                intent.putExtra(WebPageActivity.PAGE_TYPE, WebPageActivity.PAGE_TYPE_GETTING);
                startActivity(intent);
                break;

            case R.id.getting_around_sentosa:
                FlurryAgent.logEvent(getString(R.string.getting_around_sentosa));
                intent = new Intent(this, WebPageActivity.class);
                intent.putExtra(WebPageActivity.PAGE_TYPE, WebPageActivity.PAGE_TYPE_AROUND);
                startActivity(intent);
                break;

            case R.id.traffic_updates:
                FlurryAgent.logEvent(FlurryStrings.TrafficUpdates);
                intent = new Intent(this, TrafficUpdatesActivity.class);
                startActivity(intent);
                break;

            case R.id.islander_privileges:
                FlurryAgent.logEvent(FlurryStrings.IslanderPrivileges);
                intent = new Intent(this, WebPageActivity.class);
                intent.putExtra(WebPageActivity.PAGE_TYPE, WebPageActivity.PAGE_TYPE_ISLANDER);
                startActivity(intent);
                break;

            case R.id.contact_us:
                showDialog(CONTACT_US, null);
                break;

            case R.id.stay_updated:
                FlurryAgent.logEvent(FlurryStrings.ShowProfileAndSettingsInMySentosa);
                intent = new Intent(InformationActivity.this, ProfileAndSettingsActivity.class);
                startActivity(intent);
                break;
        }
    }

    @OnClick(R.id.header_search)
    public void openSearch() {
        if(searchFrame.getVisibility() == View.GONE) {
            searchFrame.setVisibility(View.VISIBLE);
        } else {
            closeSearch();
        }
    }

    @Override
    public void onBackPressed() {
        if(searchFrame.getVisibility() == View.VISIBLE) {
            closeSearch();
        } else {
            super.onBackPressed();
        }
    }

    private void closeSearch() {
        searchFragment.clearSearch();
        searchFrame.setVisibility(View.GONE);
    }

    @Override
    protected Dialog onCreateDialog(int id, Bundle args) {
        switch (id) {
            case CONTACT_US:
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("Contact us via:");
                builder.setAdapter(new ContactDialogAdapter(this), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        HashMap<String, String> attr = new HashMap<String, String>();

                        switch (which) {
                            case ContactDialogAdapter.CALL:
                                attr.put(FlurryStrings.Type, FlurryStrings.Call);
                                FlurryAgent.logEvent(FlurryStrings.ContactUs, attr);

                                SentosaUtils.startToCall(InformationActivity.this, getString(R.string.contact_number));
                                break;

                            case ContactDialogAdapter.EMAIL:
                                attr.put(FlurryStrings.Type, FlurryStrings.Email);
                                FlurryAgent.logEvent(FlurryStrings.ContactUs, attr);

                                Intent intentEmail = new Intent(Intent.ACTION_SEND);
                                intentEmail.setType("text/plain");
                                intentEmail.putExtra(android.content.Intent.EXTRA_EMAIL, new String[]{getString(R.string.email_address)});
                                intentEmail.putExtra(android.content.Intent.EXTRA_SUBJECT, getString(R.string.email_subject));
                                startActivity(Intent.createChooser(intentEmail, getString(R.string.email_choose)));
                                break;

                        }
                    }
                });
                return builder.create();

            case EMERGENCY_CALL:
                AlertDialog.Builder builderCall = new AlertDialog.Builder(this);
                builderCall.setMessage("Call emergency hotline (" + getString(R.string.emergency_number) + ")?");
                builderCall.setNegativeButton(getString(R.string.cancel), new android.content.DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                builderCall.setPositiveButton(getString(R.string.call), new android.content.DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        FlurryAgent.logEvent(FlurryStrings.EmergencyHotline);
                        SentosaUtils.startToCall(InformationActivity.this, getString(R.string.emergency_number));
                        dialog.dismiss();
                    }
                });
                return builderCall.create();
        }

        return super.onCreateDialog(id, args);
    }

    static class ContactDialogAdapter extends ArrayAdapter<String> {

        private static final String[] SHARE = new String[]{"Call", "Email"};
        private static final int[] SHARE_ICON = new int[]{R.drawable.icon_phone, R.drawable.icon_email};

        private Context context;
        public static final int CALL = 0;
        public static final int EMAIL = 1;

        public ContactDialogAdapter(Context context) {
            super(context, android.R.layout.select_dialog_item, android.R.id.text1, SHARE);
            this.context = context;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            // User super class to create the View
            View v = super.getView(position, convertView, parent);
            TextView tv = (TextView) v.findViewById(android.R.id.text1);
            Drawable icon = context.getResources().getDrawable(SHARE_ICON[position]);
            // Put the icon drawable on the TextView (support various screen densities)
            int dpS = (int) (40 * context.getResources().getDisplayMetrics().density);
            icon.setBounds(0, 0, dpS, dpS);
            tv.setCompoundDrawables(icon, null, null, null);

            // Add margin between image and name (support various screen densities)
            int dp5 = (int) (20 * context.getResources().getDisplayMetrics().density);
            tv.setCompoundDrawablePadding(dp5);

            return v;
        }
    }
}