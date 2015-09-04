package com.mysentosa.android.sg.share;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.share.Sharer;
import com.facebook.share.model.ShareLinkContent;
import com.facebook.share.widget.ShareDialog;
import com.mysentosa.android.sg.R;
import com.mysentosa.android.sg.utils.HttpHelper;
import com.mysentosa.android.sg.utils.SentosaUtils;

import java.util.Arrays;

public class FacebookShare {

    private static final String TAG = FacebookShare.class.getSimpleName();

    private Activity mActivity;
    private final int GENERAL_ERROR = 0, OAUTHERROR = 1, SUCCESSFUL_POST = 2;

    private CallbackManager callbackManager;
    private LoginManager loginManager;
    private ShareDialog shareDialog;

    public FacebookShare(Activity activity) {
        this.mActivity = activity;
        callbackManager = CallbackManager.Factory.create();
        loginManager = LoginManager.getInstance();
    }

    public void share(FacebookSharedItem sharedItem) {
        AccessToken accessToken = AccessToken.getCurrentAccessToken();
        if (accessToken != null) {
            buildFacebookDialog(sharedItem);
        } else {
            authrizeFacebook(sharedItem);
        }
    }

    public void authrizeFacebook(final FacebookSharedItem sharedItem) {
        loginManager.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                Log.d(TAG, "FacebookLogin success " + loginResult.toString());
                buildFacebookDialog(sharedItem);
            }

            @Override
            public void onCancel() {
                Log.e(TAG, "FacebookLogin cancel");
            }

            @Override
            public void onError(FacebookException e) {
                Log.e(TAG, "FacebookException " + e.getMessage());
                e.printStackTrace();
            }
        });
        loginManager.logInWithPublishPermissions(mActivity, Arrays.asList("publish_actions"));
    }

    public void buildFacebookDialog(FacebookSharedItem sharedItem) {
        shareDialog = new ShareDialog(mActivity);
        shareDialog.registerCallback(callbackManager, new FacebookCallback<Sharer.Result>() {
            @Override
            public void onSuccess(Sharer.Result result) {
                displayToast(SUCCESSFUL_POST);
            }

            @Override
            public void onCancel() {

            }

            @Override
            public void onError(FacebookException e) {
                displayToast(GENERAL_ERROR);
            }
        });

        if (ShareDialog.canShow(ShareLinkContent.class)) {
            if (!SentosaUtils.isValidString(sharedItem.getLink())) {
                sharedItem.setLink("http://sentosa.com");
            }
            String img_url = sharedItem.getImgUrl();
            if (SentosaUtils.isValidString(img_url)) {
                img_url = HttpHelper.BASE_HOST + img_url;
            }

            ShareLinkContent linkContent = new ShareLinkContent.Builder()
                    .setContentTitle(sharedItem.getTitle())
                    .setContentDescription(sharedItem.getDescription())
                    .setContentUrl(Uri.parse(sharedItem.getLink()))
                    .setImageUrl(Uri.parse(img_url))
                    .build();

            shareDialog.show(linkContent);
        }
    }

    public void displayToast(int TOAST_ID) {
        final String message;
        switch (TOAST_ID) {
            case GENERAL_ERROR:
                message = mActivity.getText(R.string.facebook_check_server_error).toString();
                break;
            case OAUTHERROR:
                message = mActivity.getText(R.string.facebook_oauth_error).toString();
                break;
            case SUCCESSFUL_POST:
                message = mActivity.getText(R.string.facebook_successful_post).toString();
                break;
            default:
                message = mActivity.getText(R.string.facebook_check_server_error).toString();
                break;
        }
        if (mActivity != null) {
            mActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(mActivity.getApplicationContext(), message, Toast.LENGTH_SHORT).show();
                }

            });
        }

    }


    public void authorizeCallback(int requestCode, int resultCode, Intent data) {
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }

    public static class FacebookSharedItem {

        private String title;
        private String description;
        private String link;
        private String imgUrl;

        public FacebookSharedItem(String title, String description, String link, String imgUrl) {
            this.title = title;
            this.description = description;
            this.link = link;
            this.imgUrl = imgUrl;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public String getLink() {
            return link;
        }

        public void setLink(String link) {
            this.link = link;
        }

        public String getImgUrl() {
            return imgUrl;
        }

        public void setImgUrl(String imgUrl) {
            this.imgUrl = imgUrl;
        }
    }
}