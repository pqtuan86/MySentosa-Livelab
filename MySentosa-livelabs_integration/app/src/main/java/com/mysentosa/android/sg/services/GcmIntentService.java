package com.mysentosa.android.sg.services;

import android.app.AlertDialog;
import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.mysentosa.android.sg.PromotionsActivity;
import com.mysentosa.android.sg.R;
import com.mysentosa.android.sg.SplashScreenActivity;
import com.mysentosa.android.sg.receiver.GcmBroadcastReceiver;

import java.util.HashMap;
import java.util.Map;

import sg.edu.smu.livelabs.integration.LiveLabsApi;

/**
 * Created by randiwaranugraha on 6/26/15.
 */
public class GcmIntentService extends IntentService {

    public static final String TAG = GcmIntentService.class.getSimpleName();
    public static final int NOTIFICATION_ID = 1;

    public GcmIntentService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Bundle extras = intent.getExtras();
        GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(this);

        // The getMessageType() intent parameter must be the intent you received in your BroadcastReceiver.
        String messageType = gcm.getMessageType(intent);

        if (!extras.isEmpty()) {  // has effect of unparcelling Bundle
            if (GoogleCloudMessaging.MESSAGE_TYPE_MESSAGE.equals(messageType)) {

                Map<String, String> map  = processNotification(extras);
                if (map != null) {
                    String message = map.get("message");
                    String id = map.get("id");
                    String promotionId = map.get("promotion_id");
                    if(message != null) {
                        Intent notifyIntent = new Intent(this, SplashScreenActivity.class);
                        notifyIntent.putExtra("NOTI_TYPE", "NewLiveLabsPromotion");
                        notifyIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

                        // Creates an explicit intent for an Activity in your app
                        Intent resultIntent = new Intent(this, PromotionsActivity.class);
                        resultIntent.putExtra("Notification", true);
                        resultIntent.putExtra("id", id);
                        resultIntent.putExtra("promotion_id", promotionId);
                        // The stack builder object will contain an artificial back stack for the  started Activity.
                        // This ensures that navigating backward from the Activity leads out of
                        // your application to the Home screen.
                        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
                        stackBuilder.addParentStack(SplashScreenActivity.class);
                        stackBuilder.addNextIntent(notifyIntent);
                        // Adds the Intent that starts the Activity to the top of the stack
                        stackBuilder.addNextIntent(resultIntent);

                        PendingIntent contentIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

                        Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

                        NotificationCompat.Builder mBuilder =
                                new NotificationCompat.Builder(this)
                                        .setSmallIcon(R.mipmap.ic_launcher)
                                        .setStyle(new NotificationCompat.BigTextStyle().bigText(message))
                                        .setContentTitle("Sentosa")
                                        .setSound(alarmSound)
                                        .setVibrate(new long[]{1000, 1000, 1000, 1000, 1000})
                                        .setContentText(message)
                                        .setAutoCancel(true);

                        mBuilder.setContentIntent(contentIntent);

                        NotificationManager notificationManager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
                        notificationManager.notify(NOTIFICATION_ID, mBuilder.build());
                    }
                }
            }
        }

        // Release the wake lock provided by the WakefulBroadcastReceiver.
        GcmBroadcastReceiver.completeWakefulIntent(intent);
    }

    public Map<String, String> processNotification(Bundle extras) {
        if (!LiveLabsApi.getInstance().isInitialized()) {
            throw new RuntimeException("initialize must be invoked first.");
        }

        Map<String, String> params = new HashMap<>();


        String type = extras.getString("type");
        if ("LiveLabs".equals(type)) {
            final String title = extras.getString("title");
            final String message = extras.getString("message");
            final String notificationId = extras.getString("id");
            final String promotionId = extras.getString("promotion_id");

            params.put("message", message);
            params.put("id", notificationId);
            params.put("promotion_id", promotionId);

            if (LiveLabsApi.getInstance().getMainActivity() != null
                    && !LiveLabsApi.getInstance().isMainActivityPaused()) {
                Handler h = new Handler(Looper.getMainLooper());
                h.post(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            new AlertDialog.Builder(LiveLabsApi.getInstance().getMainActivity())
                                    .setTitle(title)
                                    .setMessage(message)
                                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {

                                            LiveLabsApi.getInstance().notificationTracking(notificationId);

                                            Intent intent = new Intent(LiveLabsApi.getInstance().getMainActivity(), PromotionsActivity.class);
                                            LiveLabsApi.getInstance().getMainActivity().startActivity(intent);
                                        }
                                    })
                                    .show();
                        } catch (Exception e) {

                        }
                    }
                });
                return null;
            }
            else if (LiveLabsApi.getInstance().getPromotionActivity() != null
                    && !LiveLabsApi.getInstance().isPromotionActivityPaused()) {
                Handler h = new Handler(Looper.getMainLooper());
                h.post(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            new AlertDialog.Builder(LiveLabsApi.getInstance().getPromotionActivity())
                                    .setTitle(title)
                                    .setMessage(message)
                                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {

                                            LiveLabsApi.getInstance().notificationTracking(notificationId);

                                            Intent intent = new Intent(LiveLabsApi.getInstance().getPromotionActivity(), PromotionsActivity.class);
                                            LiveLabsApi.getInstance().getPromotionActivity().startActivity(intent);
                                        }
                                    })
                                    .show();
                        } catch (Exception e) {

                        }
                    }
                });

                return params;
            }
            return params;
        }
        return null;
    }
}