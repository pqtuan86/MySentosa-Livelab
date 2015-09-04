package com.mysentosa.android.sg.custom_dialog;

import com.flurry.android.FlurryAgent;
import com.google.analytics.tracking.android.EasyTracker;
import com.google.analytics.tracking.android.MapBuilder;
import com.mysentosa.android.sg.IslanderActivity;
import com.mysentosa.android.sg.IslanderLoginActivity;
import com.mysentosa.android.sg.R;
import com.mysentosa.android.sg.SentosaApplication;
import com.mysentosa.android.sg.WebPageActivity;
import com.mysentosa.android.sg.utils.Const;
import com.mysentosa.android.sg.utils.Const.FlurryStrings;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

public class LoginDialog {
    public static final int REQUEST_LOGIN_CODE = 2359;
    private Context context;
    private Dialog loginDialog;
    private OnClickListener onClick;

    public LoginDialog(Context context) {
        this.context = context;
        loginDialog = new Dialog(context, R.style.CustomDialog);
        loginDialog.setContentView(R.layout.dialog_islander_login);

        ImageView ivClose = (ImageView) loginDialog.findViewById(R.id.iv_close);
        ivClose.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
        initOnClick();
        TextView tvLogin = (TextView) loginDialog.findViewById(R.id.tv_login);
        tvLogin.setTypeface(((SentosaApplication) ((Activity) context).getApplication()).myridTypeFace);
        Button btLogin = (Button) loginDialog.findViewById(R.id.bt_login);
        btLogin.setTypeface(((SentosaApplication) ((Activity) context).getApplication()).myridTypeFace);
        btLogin.setOnClickListener(onClick);
        Button btSignUp = (Button) loginDialog.findViewById(R.id.bt_signup);
        btSignUp.setTypeface(((SentosaApplication) ((Activity) context).getApplication()).myridTypeFace);
        btSignUp.setOnClickListener(onClick);
    }

    public void show() {
        loginDialog.show();
    }
    
    public void dismiss() {
        if (loginDialog != null) {
            loginDialog.dismiss();
        }
    }
    
    private void initOnClick() {
        onClick = new OnClickListener() {
            
            @Override
            public void onClick(View v) {
                Intent intent;
                EasyTracker easyTracker = EasyTracker.getInstance(context);
                switch (v.getId()) {
                case R.id.bt_login:
                    loginDialog.dismiss();
                    
                    easyTracker.send(MapBuilder.createEvent(Const.GAStrings.ISLANDER_EVENT_CATEGORY,
                            Const.GAStrings.ISLANDER_LOGIN, "", null).build());
                    
                    intent = new Intent(context, IslanderLoginActivity.class);
                    ((Activity)context).startActivityForResult(intent, REQUEST_LOGIN_CODE);
                    break;
                case R.id.bt_signup:
                    loginDialog.dismiss();
                    
                    easyTracker.send(MapBuilder.createEvent(Const.GAStrings.ISLANDER_EVENT_CATEGORY,
                            Const.GAStrings.ISLANDER_REGISTER, "", null).build());
                    
                    FlurryAgent.logEvent(FlurryStrings.IslanderRegister);
                    intent = new Intent(context, WebPageActivity.class);
                    intent.putExtra(WebPageActivity.PAGE_TYPE, WebPageActivity.PAGE_TYPE_ISLANDER_REGISTER);
                    context.startActivity(intent);
                    break;

                default:
                    break;
                }
            }
        };
    }
}
